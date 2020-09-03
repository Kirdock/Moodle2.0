package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserCourseService extends AbstractService {

    PdfService pdfService;
    UserService userDetailsService;
    FinishesExampleService finishesExampleService;

    public UserCourseService(PdfService pdfService, UserService userDetailsService, FinishesExampleService finishesExampleService)
    {
        this.pdfService = pdfService;
        this.userDetailsService = userDetailsService;
        this.finishesExampleService = finishesExampleService;
    }

    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);

    public CourseResponseObject getCourseAssigned(Long courseId) throws ServiceException {
        Course course = readCourse(courseId);
        User currentUser = getCurrentUser();
        Boolean isCourseAssigned = currentUser.getCourses().stream()
                .filter(userInCourse -> !ECourseRole.None.equals(userInCourse.getRole()))
                .anyMatch(userInCourse -> userInCourse.getCourse().getId().equals(courseId));
        if (!isCourseAssigned)
            throw new ServiceException("Error: User is not assigned to this course!", HttpStatus.UNAUTHORIZED);

        return course.createCourseResponseObject_GetAssignedCourse(currentUser.getMatriculationNumber());
    }




    @Transactional
    public void assignUsers(List<AssignUserToCourseRequest> assignUserToCourseRequests) throws ServiceException, IOException {
        UserDetailsImpl userDetails = getUserDetails();
        List<UserInCourse> userInCourses = new ArrayList<>();
        for(AssignUserToCourseRequest assignUserToCourseRequest: assignUserToCourseRequests) {

            if(!userRepository.existsByMatriculationNumber(assignUserToCourseRequest.getMatriculationNumber()))
                throw new ServiceException("Error: User with matrikulationNumber:"+assignUserToCourseRequest.getMatriculationNumber()+" does not exist");

            if(!courseRepository.existsById(assignUserToCourseRequest.getCourseId()))
                throw new ServiceException("Error: Course with id:"+assignUserToCourseRequest.getCourseId()+" does not exist");

            if(!getUserDetails().getAdmin() && !isOwner(assignUserToCourseRequest.getCourseId()))
                throw new ServiceException("Error: User is not owner of course: "+assignUserToCourseRequest.getCourseId(),HttpStatus.FORBIDDEN);

            UserCourseKey userCourseKey = new UserCourseKey();
            UserInCourse userInCourse = new UserInCourse();
            User user = new User();
            Course course = new Course();

            userCourseKey.setCourseId(assignUserToCourseRequest.getCourseId());
            userCourseKey.setMatriculationNumber(assignUserToCourseRequest.getMatriculationNumber());

            user.setMatriculationNumber(assignUserToCourseRequest.getMatriculationNumber());
            course.setId(assignUserToCourseRequest.getCourseId());
            userInCourse.setRole(assignUserToCourseRequest.getRole());
            userInCourse.setUser(user);
            userInCourse.setCourse(course);
            userInCourse.setId(userCourseKey);
            userInCourses.add(userInCourse);

            if(ECourseRole.None.equals(userInCourse.getRole()))
                clearUserInfo(userInCourse);
        }
        userInCourseRepository.saveAll(userInCourses);
    }


    protected void clearUserInfo(UserInCourse userInCourse) throws IOException {
        List<FinishesExample> finishesExamplesToBeDeleted = new ArrayList<>();
        Optional<UserInCourse> optionalUserInCourse = userInCourseRepository.findByUser_MatriculationNumberAndCourse_Id(userInCourse.getId().getMatriculationNumber(), userInCourse.getId().getCourseId());
        // check if user_course assigment role was set to none -> user was removed from course -> delete course related info
        if (optionalUserInCourse.isPresent() && !ECourseRole.None.equals(optionalUserInCourse.get().getRole()) && ECourseRole.None.equals(userInCourse.getRole())) {
            User user = readUser(userInCourse.getId().getMatriculationNumber());
            for (FinishesExample finishesExample : user.getFinishedExamples()) {
                if (userInCourse.getId().getCourseId().equals(finishesExample.getExample().getExerciseSheet().getCourse().getId()))
                    finishesExamplesToBeDeleted.add(finishesExample);
            }
            for (FinishesExample finishesExample : finishesExamplesToBeDeleted) {
                finishesExampleService.deleteFinishExampleData(finishesExample);
            }
            finishesExampleRepository.deleteAll(finishesExamplesToBeDeleted);
        }
    }

    @Transactional
    public RegisterMultipleUserResponse assignFile(MultipartFile file, Long courseId) throws ServiceException {
        Course course = readCourse(courseId);
        RegisterMultipleUserResponse registerMultipleUserResponse = new RegisterMultipleUserResponse();
        List<User> allGivenUsers = userService.registerMissingUsersFromFile(file,registerMultipleUserResponse);
        List<UserInCourse> userInCourses = new ArrayList<>();

        for (User user : allGivenUsers) {

            UserCourseKey userCourseKey = new UserCourseKey();
            UserInCourse userInCourse = new UserInCourse();

            userCourseKey.setCourseId(courseId);
            userCourseKey.setMatriculationNumber(user.getMatriculationNumber());

            userInCourse.setRole(ECourseRole.Student);
            userInCourse.setUser(user);
            userInCourse.setCourse(course);
            userInCourse.setId(userCourseKey);
            userInCourses.add(userInCourse);
        }
        userInCourseRepository.saveAll(userInCourses);

        return registerMultipleUserResponse;
    }
}
