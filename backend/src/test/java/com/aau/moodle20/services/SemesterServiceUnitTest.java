package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.ESemesterType;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import com.aau.moodle20.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class SemesterServiceUnitTest extends AbstractServiceTest{


    @InjectMocks
    private SemesterService semesterService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInCourseRepository userInCourseRepository;
    @Mock
    private SemesterRepository semesterRepository;

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createSemester()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester);
        when(semesterRepository.existsByTypeAndYear(semester.getType(),semester.getYear())).thenReturn(Boolean.FALSE);

        CreateSemesterRequest createSemesterRequest = getSemesterCreateRequest();
        Semester semester_expected = new Semester();
        semester_expected.setType(createSemesterRequest.getType());
        semester_expected.setYear(createSemesterRequest.getYear());

        semesterService.createSemester(createSemesterRequest);
        verify(semesterRepository).save(semester_expected);
    }

    @Test
    public void createSemester_where_Semester_with_Year_and_type_already_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        when(semesterRepository.save(any(Semester.class))).thenReturn(semester);

        CreateSemesterRequest createSemesterRequest = getSemesterCreateRequest();
        createSemesterRequest.setYear(2000);
        createSemesterRequest.setType(ESemesterType.S);
        when(semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(),createSemesterRequest.getYear())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            semesterService.createSemester(createSemesterRequest);
        });
        String expectedMessage = "Error: Semester with this year and type already exists!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.SEMESTER_ALREADY_EXISTS,exception.getErrorResponseCode());
    }

    @Test
    public void getSemesters_Admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<Semester> semesters = getListSemesters();
        List<Semester> unsortedSemester = getListSemesters();
        when(semesterRepository.findAll()).thenReturn(semesters);

        List<Semester> responseSemesters = semesterService.getSemesters();
        assertEquals(4,responseSemesters.size());
        assertNotEquals(unsortedSemester, responseSemesters);

        unsortedSemester.sort(Comparator.comparing(Semester::getYear).thenComparing(Semester::getType, Comparator.reverseOrder()));
        assertEquals(unsortedSemester, responseSemesters);
    }

    @Test
    public void getSemesters_Non_Admin() {
        UserDetailsImpl userDetails = getUserDetails_Not_Admin();
        Course course1 = getTestCourse();
        Course course2 = getTestCourse();
        mockSecurityContext_WithUserDetails(userDetails);
        List<Semester> semesters = getListSemesters_2();
        List<Semester> unsortedSemester = getListSemesters_2();
        course1.setSemester(semesters.get(0));
        course2.setSemester(semesters.get(1));
        List<Course> courses = new ArrayList<>();
        courses.add(course1);
        courses.add(course2);

        when(courseRepository.existsByOwnerMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(courseRepository.findByOwnerMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(courses);


        List<Semester> responseSemesters = semesterService.getSemesters();
        assertEquals(2,responseSemesters.size());
        assertNotEquals(unsortedSemester, responseSemesters);

        unsortedSemester.sort(Comparator.comparing(Semester::getYear).thenComparing(Semester::getType, Comparator.reverseOrder()));
        assertEquals(unsortedSemester, responseSemesters);
    }

    @Test
    public void getSemestersAssigned() {
        UserDetailsImpl userDetails = getUserDetails_Not_Admin();
        Course course1 = getTestCourse();
        Course course2 = getTestCourse();
        mockSecurityContext_WithUserDetails(userDetails);
        List<Semester> semesters = getListSemesters_2();
        List<Semester> unsortedSemester = getListSemesters_2();
        course1.setSemester(semesters.get(0));
        course2.setSemester(semesters.get(1));
        List<UserInCourse> userInCourses = new ArrayList<>();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setCourse(course1);
        UserInCourse userInCourse2 = new UserInCourse();
        userInCourse2.setCourse(course2);

        userInCourses.add(userInCourse);
        userInCourses.add(userInCourse2);

        when(userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(userInCourses);

        List<Semester> responseSemesters = semesterService.getSemestersAssigned();
        assertEquals(2,responseSemesters.size());
        assertNotEquals(unsortedSemester, responseSemesters);
        unsortedSemester.sort(Comparator.comparing(Semester::getYear).thenComparing(Semester::getType, Comparator.reverseOrder()));
        assertEquals(unsortedSemester, responseSemesters);
    }


    @Test
    public void getCoursesFromSemester_where_Semester_does_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            semesterService.getCoursesFromSemester(SEMESTER_ID);
        });
        String expectedMessage = "Error: Semester not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getCoursesFromSemester_Admin()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course1 = getTestCourse();
        Course course2 = getTestCourse();
        List<Course> courses = new ArrayList<>();
        courses.add(course1);
        courses.add(course2);
        List<CourseResponseObject> testResponseObject = courses.stream().map(Course::createCourseResponseObject).collect(Collectors.toList());
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findCoursesBySemesterId(SEMESTER_ID)).thenReturn(courses);

        List<CourseResponseObject> responseObjects = semesterService.getCoursesFromSemester(SEMESTER_ID);
        assertEquals(2,responseObjects.size());
        assertNotEquals(responseObjects, testResponseObject);
        testResponseObject.forEach(courseResponseObject -> courseResponseObject.setOwner(null));
        assertEquals(responseObjects, testResponseObject);
    }

    @Test
    public void getCoursesFromSemester_Not_Admin()  {
        UserDetailsImpl userDetails = getUserDetails_Not_Admin();
        mockSecurityContext_WithUserDetails(userDetails);
        Semester semester = getTestSemester();
        Course course1 = getTestCourse();
        Course course2 = getTestCourse();
        List<Course> courses = new ArrayList<>();
        courses.add(course1);
        courses.add(course2);
        List<CourseResponseObject> testResponseObject = courses.stream().map(Course::createCourseResponseObject).collect(Collectors.toList());
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findCoursesBySemesterIdAndOwnerMatriculationNumber(SEMESTER_ID,userDetails.getMatriculationNumber())).thenReturn(courses);

        List<CourseResponseObject> responseObjects = semesterService.getCoursesFromSemester(SEMESTER_ID);
        assertEquals(2,responseObjects.size());
        assertNotEquals(responseObjects, testResponseObject);
        testResponseObject.forEach(courseResponseObject -> courseResponseObject.setOwner(null));
        assertEquals(responseObjects, testResponseObject);
    }

    @Test
    public void getAssignedCoursesFromSemester_where_Semester_does_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            semesterService.getAssignedCoursesFromSemester(SEMESTER_ID);
        });
        String expectedMessage = "Error: Semester not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getAssignedCoursesFromSemester_Admin()  {
        UserDetailsImpl userDetails = getUserDetails_Admin();
        mockSecurityContext_WithUserDetails(userDetails);
        Semester semester = getTestSemester();

        List<UserInCourse> userInCourses = getListUserInCourse();

        List<CourseResponseObject> testResponseObjects = userInCourses.stream()
                .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject()).collect(Collectors.toList());
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(userInCourses);

        List<CourseResponseObject> responseObjects = semesterService.getAssignedCoursesFromSemester(SEMESTER_ID);
        assertEquals(2,responseObjects.size());
        assertEquals(testResponseObjects,responseObjects);
    }

    @Test
    public void getAssignedCoursesFromSemester_Admin_with_Role_None()  {
        UserDetailsImpl userDetails = getUserDetails_Admin();
        mockSecurityContext_WithUserDetails(userDetails);
        Semester semester = getTestSemester();

        List<UserInCourse> userInCourses = getListUserInCourse();
        UserInCourse userInCourse1 = new UserInCourse();
        userInCourse1.setRole(ECourseRole.NONE);
        userInCourse1.setCourse(getTestCourse());

        userInCourses.add(userInCourse1);

        List<CourseResponseObject> testResponseObjects = userInCourses.stream()
                .filter(userInCourse -> !ECourseRole.NONE.equals(userInCourse.getRole()))
                .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject()).collect(Collectors.toList());
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(userInCourses);

        List<CourseResponseObject> responseObjects = semesterService.getAssignedCoursesFromSemester(SEMESTER_ID);
        assertEquals(2,responseObjects.size());
        assertEquals(testResponseObjects,responseObjects);
    }

    @Test
    public void getAssignedCoursesFromSemester_Admin_with_other_semesterId()  {
        UserDetailsImpl userDetails = getUserDetails_Admin();
        mockSecurityContext_WithUserDetails(userDetails);
        Semester semester = getTestSemester();

        List<UserInCourse> userInCourses = getListUserInCourse();
        UserInCourse userInCourse1 = new UserInCourse();
        userInCourse1.setRole(ECourseRole.STUDENT);
        Course testCourse = getTestCourse();
        testCourse.setSemester(new Semester(300L));
        userInCourse1.setCourse(testCourse);

        userInCourses.add(userInCourse1);

        List<CourseResponseObject> testResponseObjects = userInCourses.stream()
                .filter(userInCourse -> userInCourse.getCourse().getSemester().getId().equals(SEMESTER_ID))
                .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject()).collect(Collectors.toList());
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber())).thenReturn(userInCourses);

        List<CourseResponseObject> responseObjects = semesterService.getAssignedCoursesFromSemester(SEMESTER_ID);
        assertEquals(2,responseObjects.size());
        assertEquals(testResponseObjects,responseObjects);
    }


    private List<UserInCourse> getListUserInCourse() {
        Course course1 = getTestCourse();
        Course course2 = getTestCourse();

        List<UserInCourse> userInCourses = new ArrayList<>();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setCourse(course1);
        userInCourse.setRole(ECourseRole.STUDENT);
        UserInCourse userInCourse2 = new UserInCourse();
        userInCourse2.setCourse(course2);
        userInCourse2.setRole(ECourseRole.STUDENT);

        userInCourses.add(userInCourse);
        userInCourses.add(userInCourse2);
        return userInCourses;
    }

    private CreateSemesterRequest getSemesterCreateRequest()
    {
        CreateSemesterRequest createSemesterRequest = new CreateSemesterRequest();
        createSemesterRequest.setType(ESemesterType.S);
        createSemesterRequest.setYear(2020);

        return createSemesterRequest;
    }

    protected Semester getTestSemester()
    {
       Semester semester = new Semester();
       semester.setId(SEMESTER_ID);
       semester.setType(ESemesterType.S);
       semester.setYear(2002);
       return semester;
    }

    private List<Semester> getListSemesters() {
        List<Semester> semesters = new ArrayList<>();
        Semester semester = new Semester();
        semester.setId(100L);
        semester.setType(ESemesterType.S);
        semester.setYear(2002);

        Semester semester1 = new Semester();
        semester1.setId(101L);
        semester1.setType(ESemesterType.S);
        semester1.setYear(2020);


        Semester semester2 = new Semester();
        semester2.setId(102L);
        semester2.setType(ESemesterType.S);
        semester2.setYear(2012);

        Semester semester3 = new Semester();
        semester3.setId(103L);
        semester3.setType(ESemesterType.W);
        semester3.setYear(2012);

        semesters.add(semester);
        semesters.add(semester1);
        semesters.add(semester2);
        semesters.add(semester3);
        return semesters;
    }

    private List<Semester> getListSemesters_2() {
        List<Semester> semesters = new ArrayList<>();

        Semester semester1 = new Semester();
        semester1.setId(101L);
        semester1.setType(ESemesterType.S);
        semester1.setYear(2020);

        Semester semester2 = new Semester();
        semester2.setId(102L);
        semester2.setType(ESemesterType.S);
        semester2.setYear(2012);

        semesters.add(semester1);
        semesters.add(semester2);
        return semesters;
    }
}
