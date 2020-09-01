package com.aau.moodle20.security;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalInt;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private CourseRepository courseRepository;


    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        if ((authentication == null) || (s == null) || !(o instanceof String)) {
            return false;
        }
        boolean hasPermission = false;
        String permission = (String)o;
        if("Example".equals(s))
            hasPermission = handleCoursePermission(authentication,serializable, permission);

//        ConfidentialDocument confidentialDocument =
//                confidentialDocumentsRepository.findOne((Integer) targetId);
//        String documentOwner = confidentialDocument.getOwner();
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        String principalLogin = userDetails.getUsername();
//        // if current user is owner of document permission is granted
//        if (Objects.equals(documentOwner, principalLogin)) {
//            return true;
//        }
        return hasPermission;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object o, Object o1) {
        return false;
    }



    protected boolean handleCoursePermission(Authentication authentication, Serializable targetId, String permission)
    {
        boolean hasPermission = false;
        if ("update".equals(permission) && targetId instanceof UpdateCourseRequest) {
            UpdateCourseRequest updateCourseRequest = (UpdateCourseRequest) targetId;
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            hasPermission = isAdminOrOwner(updateCourseRequest.getId(),userDetails);
        }
        return hasPermission;
    }


    protected boolean isAdminOrOwner(Long courseId, UserDetailsImpl userDetails)
    {
        Course course = readCourse(courseId);
        boolean isOwner = course.getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber());
        if (isOwner || userDetails.getAdmin())
            return true;

        return false;
    }

    protected Course readCourse(Long courseId) throws ServiceException {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent())
            throw new ServiceException("Error: Course not found!", HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }
}
