package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class PdfServiceUnitTests extends AbstractServiceTest{


    @InjectMocks
    private PdfService pdfService;

    @Mock
    CourseRepository courseRepository;
    @Mock
    ExerciseSheetRepository exerciseSheetRepository;


    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);

        // create the real bean
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);
        messageSource.setBasenames("messages"); // this folder is just for testing and it contains a messages_en.properties file

        // inject the bean into the class I wanted to test
        ReflectionTestUtils.setField(pdfService, "resourceBundleMessageSource", messageSource);
    }

    @Test
    public void generateCourseAttendanceList_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            pdfService.generateCourseAttendanceList(COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void generateCourseAttendanceList_no_students() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateCourseAttendanceList(COURSE_ID);

        assertNotNull(byteArrayInputStream);
    }


    @Test
    public void generateCourseAttendanceList_students() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(createUserInCourses(course));

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateCourseAttendanceList(COURSE_ID);
        assertNotNull(byteArrayInputStream);
    }

    @Test
    public void generateKreuzelList_exerciseSheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            pdfService.generateKreuzelList(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void generateKreuzelList() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        Course course = getTestCourse();
        course.setStudents(createUserInCourses(course));
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setExamples(createExerciseSheetExamples());
        exerciseSheet.setCourse(course);
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateKreuzelList(EXERCISE_SHEET_ID);
        assertNotNull(byteArrayInputStream);
    }

    @Test
    public void generateExerciseSheetDocument_exerciseSheetNotFound() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            pdfService.generateExerciseSheetDocument(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void generateExerciseSheetDocument_notOwner_not_student_not_admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        Course course = getTestCourse();
        course.setOwner(new User("3333333"));
        course.setStudents(createUserInCourses(course));
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            pdfService.generateExerciseSheetDocument(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Access is denied";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN,exception.getHttpStatus());
    }


    @Test
    public void generateExerciseSheetDocument_admin() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        Course course = getTestCourse();
        course.setOwner(new User("3333333"));
        course.setStudents(createUserInCourses(course));
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(createExerciseSheetExamples());
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateExerciseSheetDocument(EXERCISE_SHEET_ID);
        assertNotNull(byteArrayInputStream);
    }

    @Test
    public void generateExerciseSheetDocument_owner() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        Course course = getTestCourse();
        course.setOwner(getNormalUser());
        course.setStudents(createUserInCourses(course));
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(createExerciseSheetExamples());
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateExerciseSheetDocument(EXERCISE_SHEET_ID);
        assertNotNull(byteArrayInputStream);
    }


    @Test
    public void generateExerciseSheetDocument_student() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        Course course = getTestCourse();
        course.setOwner(new User("23322333"));
        course.setStudents(createUserInCourses(course));
        
        UserInCourse userInCourse = new UserInCourse();

        userInCourse.setUser(getNormalUser());
        userInCourse.setCourse(course);
        userInCourse.setId(new UserCourseKey(getNormalUser().getMatriculationNumber(),course.getId()));
        userInCourse.setRole(ECourseRole.STUDENT);
        course.getStudents().add(userInCourse);

        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(createExerciseSheetExamples());
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ByteArrayInputStream byteArrayInputStream = pdfService.generateExerciseSheetDocument(EXERCISE_SHEET_ID);
        assertNotNull(byteArrayInputStream);
    }
    private Set<Example> createExerciseSheetExamples()
    {
        Set<Example> exampleSet = new HashSet<>();
        Example example = getTestExample(EXAMPLE_ID);
        example.setName("example1");
        example.setOrder(0);

        Example subExample = getTestExample(EXAMPLE_ID+10);
        subExample.setName("subExample");
        subExample.setOrder(0);

        example.setSubExamples(new HashSet<>());
        example.getSubExamples().add(subExample);
        subExample.setParentExample(example);

        Example example2 = getTestExample(EXAMPLE_ID+20);
        example2.setName("example2");
        example2.setOrder(1);

        exampleSet.add(example);
        exampleSet.add(subExample);
        exampleSet.add(example2);

        exampleSet.forEach(example1 -> {
            example1.setPoints(10);
            example1.setWeighting(10);
        });

        return exampleSet;
    }

    private Set<UserInCourse> createUserInCourses(Course course)
    {
        Set<UserInCourse> userInCourseSet = new HashSet<>();
        User user = new User("12345558");
        user.setForename("user1");
        user.setSurname("user1_surname");

        UserInCourse userInCourse = new UserInCourse();

        userInCourse.setUser(user);
        userInCourse.setCourse(course);
        userInCourse.setId(new UserCourseKey("12345558",course.getId()));
        userInCourse.setRole(ECourseRole.STUDENT);

        User user2 = new User("87654321");
        user2.setForename("user1");
        user2.setSurname("user1_surname");

        UserInCourse userInCourse1 = new UserInCourse();
        userInCourse1.setUser(user2);
        userInCourse1.setCourse(course);
        userInCourse1.setId(new UserCourseKey("87654321",course.getId()));
        userInCourse1.setRole(ECourseRole.LECTURER);

        userInCourseSet.add(userInCourse);
        userInCourseSet.add(userInCourse1);

        return userInCourseSet;
    }

}
