package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class UserCourseService extends AbstractService {

    @Autowired
    PdfService pdfService;

    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);

    public CourseResponseObject getCourseAssigned(Long courseId) throws ServiceException {
        Course course = readCourse(courseId);
        User currentUser = getCurrentUser();
        Boolean isCourseAssigned = currentUser.getCourses().stream().anyMatch(userInCourse -> userInCourse.getCourse().getId().equals(courseId));
        if (!isCourseAssigned)
            throw new ServiceException("Error: User is not assigned to this course!", HttpStatus.UNAUTHORIZED);

        return course.createCourseResponseObject_GetAssignedCourse(currentUser.getMatriculationNumber());
    }




    public void assignUsers(List<AssignUserToCourseRequest> assignUserToCourseRequests) throws SemesterException
    {
        //TODO add validation

        List<UserInCourse> userInCourses = new ArrayList<>();

        for(AssignUserToCourseRequest assignUserToCourseRequest: assignUserToCourseRequests) {

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

        }
        userInCourseRepository.saveAll(userInCourses);
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
