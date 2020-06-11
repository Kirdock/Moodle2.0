package com.aau.moodle20.services;

import com.aau.moodle20.component.PdfHelper;
import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SemesterService extends AbstractService{

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PdfHelper pdfHelper;

    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);


    public void createSemester(CreateSemesterRequest createSemesterRequest) throws ServiceValidationException {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear()))
            throw new ServiceValidationException("Error: Semester with this year and type already exists!", ApiErrorResponseCodes.SEMESTER_ALREADY_EXISTS);
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
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

    public List<Semester> getSemesters()
    {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();
        if(userDetails.getAdmin())
            semestersToBeReturned =  semesterRepository.findAll();
        else if(courseRepository.existsByOwner_MatriculationNumber(userDetails.getMatriculationNumber()))
        {
            List<Course> courses = courseRepository.findByOwner_MatriculationNumber(userDetails.getMatriculationNumber());
            List<Semester> semesters = courses.stream().map(Course::getSemester).collect(Collectors.toList());
            for(Semester semester: semesters)
            {
                if(!semestersToBeReturned.contains(semester))
                    semestersToBeReturned.add(semester);
            }
        }
        return semestersToBeReturned;
    }

    public List<Semester> getSemestersAssigned() {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUser_MatriculationNumber(userDetails.getMatriculationNumber());
        List<Semester> semesters = userInCourses.stream().map(userInCourse -> userInCourse.getCourse().getSemester()).collect(Collectors.toList());
        for (Semester semester : semesters) {
            if (!semestersToBeReturned.contains(semester))
                semestersToBeReturned.add(semester);
        }

        return semestersToBeReturned;
    }

    public List<CourseResponseObject> getCoursesFromSemester(Long semesterId) {
        readSemester(semesterId);
        UserDetailsImpl userDetails = getUserDetails();
        List<CourseResponseObject> responseObjects = new ArrayList<>();
        List<Course> courses = null;

        if (userDetails.getAdmin())
            courses = courseRepository.findCoursesBySemester_Id(semesterId);
        else
            courses = courseRepository.findCoursesBySemester_IdAndOwner_MatriculationNumber(semesterId, userDetails.getMatriculationNumber());

        if (courses != null && !courses.isEmpty()) {
            for (Course course : courses) {
                CourseResponseObject courseResponseObject = course.createCourseResponseObject();
                courseResponseObject.setOwner(null);
                responseObjects.add(courseResponseObject);
            }
        }
        return responseObjects;
    }

    public List<CourseResponseObject> getAssignedCoursesFromSemester(Long semesterId) {
        readSemester(semesterId);
        UserDetailsImpl userDetails = getUserDetails();

        List<CourseResponseObject> responseObjects = new ArrayList<>();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUser_MatriculationNumber(userDetails.getMatriculationNumber());

        if (userInCourses != null) {
            responseObjects.addAll(userInCourses.stream()
                    .filter(userInCourse -> userInCourse.getCourse().getSemester().getId().equals(semesterId))
                    .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject())
                    .collect(Collectors.toList())
            );
        }
        return responseObjects;
    }

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
            pdfHelper.addAttendanceTitle(document);
            pdfHelper.addAttendanceTable(document, course);
            document.close();
        } catch (DocumentException ex) {
            logger.error("Error occurred: {0}", ex);
            throw new ServiceValidationException(ex.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }




}
