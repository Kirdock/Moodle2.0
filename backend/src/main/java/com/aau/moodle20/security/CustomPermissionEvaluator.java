package com.aau.moodle20.security;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExampleRepository exampleRepository;

    @Autowired
    private ExerciseSheetRepository exerciseSheetRepository;


    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        if ((authentication == null) || (s == null) || !(o instanceof String)) {
            return false;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if(userDetails.getAdmin())
            return true;

        boolean hasPermission = false;
        String permission = (String)o;
        if("Course".equals(s))
            hasPermission = handleCoursePermission(authentication,serializable, permission);
        else if("Example".equals(s))
            hasPermission = handleExamplePermission(authentication,serializable,permission);
        else if("User".equals(s))
            hasPermission = handleUserPermission(authentication,serializable,permission);
        else if("ExerciseSheet".equals(s))
            hasPermission = handleExerciseSheetPermission(authentication,serializable,permission);

        return hasPermission;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object o, Object o1) {
        return false;
    }

    protected boolean handleExamplePermission(Authentication authentication, Serializable targetId, String permission)
    {
        boolean hasPermission = false;
        Long exampleId = null;
        Long exerciseSheetId = null;
        Course course =null;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if ("update".equals(permission) && targetId instanceof Long) {
            exampleId = (Long) targetId;
        }else if("update".equals(permission) && targetId instanceof List){
            List<ExampleOrderRequest> exampleOrderRequests = (List<ExampleOrderRequest>) targetId;
            hasPermission = handleExampleOrderRequests(exampleOrderRequests,userDetails);
        }  else if ("get".equals(permission) && targetId instanceof Long) {
            exampleId = (Long) targetId;
        } else if ("create".equals(permission) && targetId instanceof Long) {
            exerciseSheetId = (Long) targetId;
        }
        else if ("delete".equals(permission) && targetId instanceof Long) {
            exampleId = (Long) targetId;
        }

        if (exampleId != null)
            course = getCourseFromExample(exampleId);
        else if (exerciseSheetId != null)
            course = getCourseFromExerciseSheet(exerciseSheetId);

        if (course != null)
            hasPermission = isOwnerOfCourse(course.getId(), userDetails);

        return hasPermission;
    }


    protected boolean handleExerciseSheetPermission(Authentication authentication, Serializable targetId, String permission)
    {
        boolean hasPermission = false;
        Long exerciseSheetId = null;
        Long courseId = null;
        Course course =null;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if ("update".equals(permission) && targetId instanceof Long)
            exerciseSheetId = (Long) targetId;
        else if ("get".equals(permission) && targetId instanceof Long)
            exerciseSheetId = (Long) targetId;
        else if ("create".equals(permission) && targetId instanceof Long)
            courseId = (Long) targetId;
        else if ("delete".equals(permission) && targetId instanceof Long)
            exerciseSheetId = (Long) targetId;


        if (exerciseSheetId != null) {
            course = getCourseFromExerciseSheet(exerciseSheetId);
            courseId = course.getId();
        }

        if (courseId != null)
            hasPermission = isOwnerOfCourse(courseId, userDetails);

        return hasPermission;
    }


    protected boolean handleCoursePermission(Authentication authentication, Serializable targetId, String permission)
    {
        boolean hasPermission = false;
        Long courseId = null;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if ("update".equals(permission) && targetId instanceof Long) {
            courseId = (Long) targetId;
        }else if("get".equals(permission) && targetId instanceof Long)
            courseId = (Long) targetId;

        if(courseId!=null)
            hasPermission = isOwnerOfCourse(courseId,userDetails);


        return hasPermission;
    }

    protected boolean handleUserPermission(Authentication authentication, Serializable targetId, String permission)
    {
        boolean hasPermission = false;
        String matriculationNumber = null;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if ("update".equals(permission)) {
            if (targetId instanceof String)
                matriculationNumber = (String) targetId;
            else
                matriculationNumber = userDetails.getMatriculationNumber();
        } else if ("get".equals(permission) && targetId instanceof String)
            matriculationNumber = (String) targetId;

        if (matriculationNumber != null)
            hasPermission = matriculationNumber.equals(userDetails.getMatriculationNumber());

        return hasPermission;
    }


    protected boolean handleExampleOrderRequests(List<ExampleOrderRequest> exampleOrderRequests, UserDetailsImpl userDetails)
    {
        List<Long> exampleIds = exampleOrderRequests.stream().map(ExampleOrderRequest::getId).collect(Collectors.toList());
        for(Long exampleId: exampleIds)
        {
            Course course = getCourseFromExample(exampleId);
            if(!isOwnerOfCourse(course.getId(),userDetails))
                return false;
        }
        return true;
    }

    protected boolean isOwnerOfCourse(Long courseId, UserDetailsImpl userDetails) {
        Course course = readCourse(courseId);
        return course.getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber());
    }

    protected Course readCourse(Long courseId) throws ServiceException {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent())
            throw new ServiceException("Error: Course not found!", HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }

    protected Example readExample(Long exampleId) throws ServiceException {
        Optional<Example> optionalCourse = exampleRepository.findById(exampleId);
        if (!optionalCourse.isPresent())
            throw new ServiceException("Error: Example not found!", HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }

    protected ExerciseSheet readExerciseSheet(Long exampleId) throws ServiceException {
        Optional<ExerciseSheet> optionalCourse = exerciseSheetRepository.findById(exampleId);
        if (!optionalCourse.isPresent())
            throw new ServiceException("Error: ExerciseSheet not found!", HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }

    public Course getCourseFromExample(Long exampleId)
    {
        Example example = readExample(exampleId);
        return example.getExerciseSheet().getCourse();
    }

    public Course getCourseFromExerciseSheet(Long exerciseSheetId)
    {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        return exerciseSheet.getCourse();
    }
}
