package com.aau.moodle20.services;

import com.aau.moodle20.component.PdfHelper;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserCourseService extends AbstractService {

    @Autowired
    PdfHelper pdfHelper;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);

    public CourseResponseObject getCourseAssigned(Long courseId) throws ServiceValidationException {
        Course course = readCourse(courseId);
        User currentUser = getCurrentUser();
        Boolean isCourseAssigned = currentUser.getCourses().stream().anyMatch(userInCourse -> userInCourse.getCourse().getId().equals(courseId));
        if (!isCourseAssigned)
            throw new ServiceValidationException("Error: User is not assigned to this course!", HttpStatus.UNAUTHORIZED);

        return course.createCourseResponseObject_GetAssignedCourse(currentUser.getMatriculationNumber());
    }


    public ByteArrayInputStream generateCourseAttendanceList(Long courseId) throws ServiceValidationException {
        Course course = readCourse(courseId);
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfWriter.getInstance(document, out);
            document.open();
            pdfHelper.addTitle(document,"attendanceList.title");
            pdfHelper.addAttendanceTable(document, course);
            document.close();
        } catch (DocumentException ex) {
            logger.error("Error occurred: {0}", ex);
            throw new ServiceValidationException(ex.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
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
    public void assignFile(MultipartFile file, Long courseId) throws ServiceValidationException {
        Course course = readCourse(courseId);
        List<User> allGivenUsers = userDetailsService.registerUsers(file);
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
    }
}
