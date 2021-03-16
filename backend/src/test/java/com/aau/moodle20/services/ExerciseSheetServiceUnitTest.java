package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.*;
import com.aau.moodle20.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class ExerciseSheetServiceUnitTest extends AbstractServiceTest{


    @InjectMocks
    private ExerciseSheetService exerciseSheetService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private ExampleRepository exampleRepository;
    @Mock
    private ExampleService exampleService;


    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createExerciseSheet() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        CreateExerciseSheetRequest createExerciseSheetRequest = getExerciseSheetCreateRequest(exerciseSheet);

        exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        verify(exerciseSheetRepository).save(exerciseSheet);
    }

    @Test
    public void createExerciseSheet_course_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        CreateExerciseSheetRequest createExerciseSheetRequest = getExerciseSheetCreateRequest(exerciseSheet);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void createExerciseSheet_name_exists_in_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        course.getExerciseSheets().add(exerciseSheet);
        CreateExerciseSheetRequest createExerciseSheetRequest = getExerciseSheetCreateRequest(exerciseSheet);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        });
        String expectedMessage = "Error Exercise Sheet with this name already exists in given course";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getErrorResponseCode(), ApiErrorResponseCodes.EXERCISE_SHEET_WITH_THIS_NAME_ALREADY_EXISTS);
    }

    @Test
    public void updateExerciseSheet() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        UpdateExerciseSheetRequest updateExerciseSheetRequest = getUpdateExerciseSheetRequest();
        when(exerciseSheetRepository.findById(exerciseSheet.getId())).thenReturn(Optional.of(exerciseSheet));

        exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);

        ExerciseSheet exerciseSheet_expected = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet_expected.setMinKreuzel(updateExerciseSheetRequest.getMinKreuzel());
        exerciseSheet_expected.setMinPoints(updateExerciseSheetRequest.getMinPoints());
        exerciseSheet_expected.setName(updateExerciseSheetRequest.getName());
        exerciseSheet_expected.setSubmissionDate(updateExerciseSheetRequest.getSubmissionDate());
        exerciseSheet_expected.setIssueDate(updateExerciseSheetRequest.getIssueDate());
        exerciseSheet_expected.setDescription(updateExerciseSheetRequest.getDescription());
        exerciseSheet_expected.setIncludeThird(updateExerciseSheetRequest.getIncludeThird());
        exerciseSheet_expected.setCourse(course);

        verify(exerciseSheetRepository).save(exerciseSheet_expected);
    }

    @Test
    public void updateExerciseSheet_exerciseSheet_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExerciseSheetRequest updateExerciseSheetRequest = getUpdateExerciseSheetRequest();

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateExerciseSheet_name_exists_in_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        ExerciseSheet exerciseSheet_2 = getTestExerciseSheet(EXERCISE_SHEET_ID+10);
        course.getExerciseSheets().add(exerciseSheet);
        course.getExerciseSheets().add(exerciseSheet_2);

        UpdateExerciseSheetRequest updateExerciseSheetRequest = getUpdateExerciseSheetRequest();
        exerciseSheet_2.setName(updateExerciseSheetRequest.getName());

        when(exerciseSheetRepository.findById(exerciseSheet.getId())).thenReturn(Optional.of(exerciseSheet));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);
        });
        String expectedMessage = "Error Exercise Sheet with this name already exists in given course";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getErrorResponseCode(), ApiErrorResponseCodes.EXERCISE_SHEET_WITH_THIS_NAME_ALREADY_EXISTS);
    }

    @Test
    public void getExerciseSheet()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);
        ExerciseSheetResponseObject exerciseSheetResponseObject_expected = getExerciseSheetResponseObject();
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        when(exerciseSheet.getResponseObject(null)).thenReturn(exerciseSheetResponseObject_expected);

        ExerciseSheetResponseObject exerciseSheetResponseObject = exerciseSheetService.getExerciseSheet(EXERCISE_SHEET_ID);
        assertEquals(exerciseSheetResponseObject_expected,exerciseSheetResponseObject);
    }

    @Test
    public void getExerciseSheet_exerciseSheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.getExerciseSheet(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getExerciseSheetKreuzel_exerciseSheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }
    @Test
    public void getExerciseSheetKreuzel_no_examples_no_Students()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());

        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());

        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        ExerciseSheetKreuzelResponse exerciseSheetKreuzelResponse_expected = new ExerciseSheetKreuzelResponse();
        exerciseSheetKreuzelResponse_expected.setIncludeThird(exerciseSheet.getIncludeThird());
        exerciseSheetKreuzelResponse_expected.setExamples(new ArrayList<>());
        exerciseSheetKreuzelResponse_expected.setKreuzel(new ArrayList<>());

        ExerciseSheetKreuzelResponse response = exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);

        assertEquals(0,response.getExamples().size());
        assertEquals(0,response.getKreuzel().size());
        assertEquals(Boolean.FALSE,response.isIncludeThird());
    }


    @Test
    public void getExerciseSheetKreuzel_with_Examples()  {
        List<Example> testExamples = getTextExamples();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Course course = getTestCourse();
        ExerciseSheetKreuzelResponse exerciseSheetKreuzelResponse_expected = prepareForGetExerciseSheetKreuzel(exerciseSheet,course);
        exerciseSheetKreuzelResponse_expected.getExamples().add(createExampleResponse(testExamples.get(4)));
        exerciseSheetKreuzelResponse_expected.getExamples().add(createExampleResponse(testExamples.get(3)));
        exerciseSheetKreuzelResponse_expected.getExamples().add(createExampleResponse(testExamples.get(2)));
        exerciseSheetKreuzelResponse_expected.getExamples().add(createExampleResponse(testExamples.get(5)));
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));


        ExerciseSheetKreuzelResponse response = exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);

        assertEquals(4,response.getExamples().size());
        assertEquals(0,response.getKreuzel().size());

        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(0).getName(),response.getExamples().get(0).getName());
        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(0).getId(),response.getExamples().get(0).getId());

        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(1).getName(),response.getExamples().get(1).getName());
        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(1).getId(),response.getExamples().get(1).getId());

        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(2).getName(),response.getExamples().get(2).getName());
        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(2).getId(),response.getExamples().get(2).getId());

        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(3).getName(),response.getExamples().get(3).getName());
        assertEquals(exerciseSheetKreuzelResponse_expected.getExamples().get(3).getId(),response.getExamples().get(3).getId());

        assertEquals(Boolean.FALSE,response.isIncludeThird());
    }

    @Test
    public void getExerciseSheetKreuzel_with_UserInCorse_but_no_Student()  {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        course.setStudents(new HashSet<>());

        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().addAll(getTextExamples());

        List<UserInCourse> userInCourseList = getTestUserInCourse();
        userInCourseList.get(0).setRole(ECourseRole.TEACHER);
        userInCourseList.get(1).setRole(ECourseRole.TEACHER);
        userInCourseList.get(2).setRole(ECourseRole.LECTURER);
        userInCourseList.get(3).setRole(ECourseRole.LECTURER);
        course.getStudents().addAll(userInCourseList);

        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));

        ExerciseSheetKreuzelResponse response = exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);
        assertEquals(4,response.getExamples().size());
        assertEquals(0,response.getKreuzel().size());
        assertEquals(Boolean.FALSE,response.isIncludeThird());
    }

    @Test
    public void getExerciseSheetKreuzel_with_UserInCorse_no_finished_example()  {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        course.setStudents(new HashSet<>());

        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().addAll(getTextExamples());

        List<UserInCourse> userInCourseList = getTestUserInCourse();
        course.getStudents().addAll(userInCourseList);
        exerciseSheet.setCourse(course);

        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));

        ExerciseSheetKreuzelResponse response = exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);
        assertEquals(4,response.getExamples().size());
        assertEquals(4,response.getKreuzel().size());
        for (int i = 0; i < 4; i++)
            assertEquals(4, response.getKreuzel().get(i).getStates().size());
        // check kreuzel sorting

        assertEquals("11111111", response.getKreuzel().get(0).getMatriculationNumber());
        assertEquals("22222222", response.getKreuzel().get(1).getMatriculationNumber());
        assertEquals("33333333", response.getKreuzel().get(2).getMatriculationNumber());
        assertEquals("44444444", response.getKreuzel().get(3).getMatriculationNumber());

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                assertEquals(EFinishesExampleState.NO.getRole(), response.getKreuzel().get(i).getStates().get(j).getType());
                assertEquals("", response.getKreuzel().get(i).getStates().get(j).getDescription());
            }
        assertEquals(Boolean.FALSE,response.isIncludeThird());
    }


    @Test
    public void getExerciseSheetKreuzel_with_UserInCorse_with_finished_example()  {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        course.setStudents(new HashSet<>());

        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());

        List<UserInCourse> userInCourseList = getTestUserInCourse();
        course.getStudents().addAll(userInCourseList);
        exerciseSheet.setCourse(course);

        List<Example> testExamples = getTextExamples();

        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setUser(userInCourseList.get(0).getUser());
        finishesExample.setDescription("hallo");
        finishesExample.setState(EFinishesExampleState.MAYBE);
        finishesExample.setViolationHistories(new HashSet<>());

        FinishesExample finishesExample2 = new FinishesExample();
        finishesExample2.setUser(userInCourseList.get(1).getUser());
        finishesExample2.setDescription("hallo2");
        finishesExample2.setState(EFinishesExampleState.YES);
        finishesExample2.setViolationHistories(new HashSet<>());

        testExamples.get(3).getExamplesFinishedByUser().add(finishesExample);
        testExamples.get(4).getExamplesFinishedByUser().add(finishesExample2);

        exerciseSheet.getExamples().addAll(testExamples);
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));

        ExerciseSheetKreuzelResponse response = exerciseSheetService.getExerciseSheetKreuzel(EXERCISE_SHEET_ID);
        assertEquals(4,response.getExamples().size());
        assertEquals(4,response.getKreuzel().size());

        assertEquals("hallo2",response.getKreuzel().get(2).getStates().get(0).getDescription());
        assertEquals(EFinishesExampleState.YES.getRole(),response.getKreuzel().get(2).getStates().get(0).getType());
        assertEquals(new ArrayList<>(),response.getKreuzel().get(2).getStates().get(0).getResult());

        assertEquals("hallo",response.getKreuzel().get(3).getStates().get(1).getDescription());
        assertEquals(EFinishesExampleState.MAYBE.getRole(),response.getKreuzel().get(3).getStates().get(1).getType());
        assertEquals(new ArrayList<>(),response.getKreuzel().get(3).getStates().get(1).getResult());

        assertEquals(Boolean.FALSE,response.isIncludeThird());
    }

    @Test
    public void getExerciseSheetAssigned_exerciseSheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.getExerciseSheetAssigned(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getExerciseSheetAssigned_not_assigned_to_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);
        ExerciseSheetResponseObject exerciseSheetResponseObject_expected = getExerciseSheetResponseObject();
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        when(exerciseSheet.getResponseObject(anyString())).thenReturn(exerciseSheetResponseObject_expected);
        when(exerciseSheet.getCourse()).thenReturn(course);

        ExerciseSheetResponseObject exerciseSheetResponseObject = exerciseSheetService.getExerciseSheetAssigned(EXERCISE_SHEET_ID);
        assertNotEquals(exerciseSheetResponseObject_expected,exerciseSheetResponseObject);
        assertEquals(new ExerciseSheetResponseObject(),exerciseSheetResponseObject);
    }

    @Test
    public void getExerciseSheetAssigned_not_assigned_to_course2()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(new User(normalMatriculationNumber));
        course.getStudents().add(userInCourse);
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);
        ExerciseSheetResponseObject exerciseSheetResponseObject_expected = getExerciseSheetResponseObject();
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        when(exerciseSheet.getResponseObject(anyString())).thenReturn(exerciseSheetResponseObject_expected);
        when(exerciseSheet.getCourse()).thenReturn(course);

        ExerciseSheetResponseObject exerciseSheetResponseObject = exerciseSheetService.getExerciseSheetAssigned(EXERCISE_SHEET_ID);
        assertNotEquals(exerciseSheetResponseObject_expected,exerciseSheetResponseObject);
        assertEquals(new ExerciseSheetResponseObject(),exerciseSheetResponseObject);
    }

    @Test
    public void getExerciseSheetAssigned()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        course.setStudents(new HashSet<>());
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(new User(adminMatriculationNumber));
        course.getStudents().add(userInCourse);
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);
        ExerciseSheetResponseObject exerciseSheetResponseObject_expected = getExerciseSheetResponseObject();
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        when(exerciseSheet.getResponseObject(anyString())).thenReturn(exerciseSheetResponseObject_expected);
        when(exerciseSheet.getCourse()).thenReturn(course);

        ExerciseSheetResponseObject exerciseSheetResponseObject = exerciseSheetService.getExerciseSheetAssigned(EXERCISE_SHEET_ID);
        assertEquals(exerciseSheetResponseObject_expected,exerciseSheetResponseObject);
        assertNotEquals(new ExerciseSheetResponseObject(),exerciseSheetResponseObject);

    }

    @Test
    public void deleteExerciseSheet_exerciseSheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.deleteExerciseSheet(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteExerciseSheet() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        doNothing().when(exampleService).deleteExampleValidator(anyLong());
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Example example = new Example(EXAMPLE_ID);
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().add(example);
        when(exerciseSheetRepository.findById(EXERCISE_SHEET_ID)).thenReturn(Optional.of(exerciseSheet));
        exerciseSheetService.deleteExerciseSheet(EXERCISE_SHEET_ID);
        verify(exampleRepository).flush();
        verify(exerciseSheetRepository).delete(exerciseSheet);
    }

    @Test
    public void getExerciseSheetsFromCourse_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.deleteExerciseSheet(EXERCISE_SHEET_ID);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getExerciseSheetsFromCourse_no_exerciseSheets_in_course() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<ExerciseSheetResponseObject> responseObjects = exerciseSheetService.getExerciseSheetsFromCourse(course.getId());
        assertEquals(new ArrayList<>(), responseObjects);
    }

    @Test
    public void getExerciseSheetsFromCourse_not_owner_of_course_nor_student()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        course.setOwner(new User(adminMatriculationNumber));
        course.setStudents(new HashSet<>());

        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(new User(adminMatriculationNumber));
        userInCourse.setRole(ECourseRole.STUDENT);

        UserInCourse userInCourse2 = new UserInCourse();
        userInCourse2.setUser(new User(adminMatriculationNumber));
        userInCourse2.setRole(ECourseRole.NONE);
        course.getStudents().add(userInCourse);
        course.getStudents().add(userInCourse2);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exerciseSheetService.getExerciseSheetsFromCourse(course.getId());
        });
        String expectedMessage = "Error: not authorized to access exerciseSheets";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getExerciseSheetsFromCourse_owner_no_exerciseSheets()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        course.setOwner(new User(getUserDetails_Not_Admin().getMatriculationNumber()));
        course.setStudents(new HashSet<>());

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<ExerciseSheetResponseObject> responseObjects = exerciseSheetService.getExerciseSheetsFromCourse(course.getId());
        assertEquals(new ArrayList<>(), responseObjects);
    }

    @Test
    public void getExerciseSheetsFromCourse_student_no_exerciseSheets()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        List<ExerciseSheet> exerciseSheetList = new ArrayList<>();

        ExerciseSheet exerciseSheet = new ExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setSubmissionDate(LocalDateTime.now().minusDays(10));
        exerciseSheet.setName("abc");

        ExerciseSheet exerciseSheet2 = new ExerciseSheet(EXERCISE_SHEET_ID+20);
        exerciseSheet2.setSubmissionDate(LocalDateTime.now().minusDays(3));
        exerciseSheet2.setName("abc");

        ExerciseSheet exerciseSheet3 = new ExerciseSheet(EXERCISE_SHEET_ID+30);
        exerciseSheet3.setSubmissionDate(LocalDateTime.now().minusDays(5));
        exerciseSheet3.setName("abc");

        LocalDateTime dateTime = LocalDateTime.now();

        ExerciseSheet exerciseSheet4 = new ExerciseSheet(EXERCISE_SHEET_ID+40);
        exerciseSheet4.setSubmissionDate(dateTime);
        exerciseSheet4.setName("abc");

        ExerciseSheet exerciseSheet5 = new ExerciseSheet(EXERCISE_SHEET_ID+50);
        exerciseSheet5.setSubmissionDate(dateTime);
        exerciseSheet5.setName("ccc");

        ExerciseSheet exerciseSheet6 = new ExerciseSheet(EXERCISE_SHEET_ID+60);
        exerciseSheet6.setSubmissionDate(dateTime);
        exerciseSheet6.setName("bbb");

        exerciseSheetList.add(exerciseSheet);
        exerciseSheetList.add(exerciseSheet2);
        exerciseSheetList.add(exerciseSheet3);
        exerciseSheetList.add(exerciseSheet4);
        exerciseSheetList.add(exerciseSheet5);
        exerciseSheetList.add(exerciseSheet6);

        when(exerciseSheetRepository.findByCourseId(course.getId())).thenReturn(exerciseSheetList);

        List<ExerciseSheetResponseObject> responseObjects_expected = exerciseSheetList.stream()
                .map(exerciseSheet1 -> createExerciseSheetResponseObject(exerciseSheet1))
                .collect(Collectors.toList());

        responseObjects_expected.sort(Comparator.comparing(ExerciseSheetResponseObject::getSubmissionDate)
                .thenComparing(ExerciseSheetResponseObject::getName));

        List<ExerciseSheetResponseObject> responseObjects = exerciseSheetService.getExerciseSheetsFromCourse(course.getId());
        assertEquals(responseObjects_expected, responseObjects);
    }


    private ExerciseSheetKreuzelResponse prepareForGetExerciseSheetKreuzel(ExerciseSheet exerciseSheet, Course course)
    {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        course.setStudents(new HashSet<>());

        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().addAll(getTextExamples());

        ExerciseSheetKreuzelResponse exerciseSheetKreuzelResponse_expected = new ExerciseSheetKreuzelResponse();
        exerciseSheetKreuzelResponse_expected.setIncludeThird(exerciseSheet.getIncludeThird());
        exerciseSheetKreuzelResponse_expected.setExamples(new ArrayList<>());
        exerciseSheetKreuzelResponse_expected.setKreuzel(new ArrayList<>());

        return exerciseSheetKreuzelResponse_expected;
    }

    private ExampleResponseObject createExampleResponse(Example example)
    {
        ExampleResponseObject exampleResponseObject = new ExampleResponseObject();
        exampleResponseObject.setName(example.getName());
        exampleResponseObject.setId(example.getId());
        exampleResponseObject.setSubExamples(null);

        return exampleResponseObject;
    }

    private KreuzelCourseResponse createKreuzelCourseResponse(UserInCourse userInCourse) {
        User user = userInCourse.getUser();
        KreuzelCourseResponse kreuzelCourseResponse = new KreuzelCourseResponse();
        kreuzelCourseResponse.setMatriculationNumber(user.getMatriculationNumber());
        kreuzelCourseResponse.setSurname(user.getSurname());
        kreuzelCourseResponse.setForename(user.getForename());
        return kreuzelCourseResponse;
    }

    private CreateExerciseSheetRequest getExerciseSheetCreateRequest(ExerciseSheet exerciseSheet)
    {
        CreateExerciseSheetRequest createExerciseSheetRequest = new CreateExerciseSheetRequest();
        createExerciseSheetRequest.setMinPoints(exerciseSheet.getMinPoints());
        createExerciseSheetRequest.setMinKreuzel(exerciseSheet.getMinKreuzel());
        createExerciseSheetRequest.setIssueDate(exerciseSheet.getIssueDate());
        createExerciseSheetRequest.setSubmissionDate(exerciseSheet.getSubmissionDate());
        createExerciseSheetRequest.setName(exerciseSheet.getName());
        createExerciseSheetRequest.setCourseId(exerciseSheet.getCourse().getId());
        createExerciseSheetRequest.setDescription(exerciseSheet.getDescription());
        createExerciseSheetRequest.setIncludeThird(exerciseSheet.getIncludeThird());

        return createExerciseSheetRequest;
    }

    private ExerciseSheetResponseObject getExerciseSheetResponseObject()
    {
        Course course = getTestCourse();
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setId(EXERCISE_SHEET_ID);
        responseObject.setCourseId(course.getId());
        responseObject.setMinKreuzel(20);
        responseObject.setMinPoints(20);
        responseObject.setName("hallo");
        responseObject.setSubmissionDate(LocalDateTime.now());
        responseObject.setIssueDate(LocalDateTime.now());
        responseObject.setDescription("hallo");
        responseObject.setIncludeThird(Boolean.TRUE);

        responseObject.setCourseName(course.getName());
        responseObject.setCourseNumber(course.getNumber());
        responseObject.setUploadCount(course.getUploadCount());
        return responseObject;
    }



    private UpdateExerciseSheetRequest getUpdateExerciseSheetRequest()
    {
        UpdateExerciseSheetRequest updateExerciseSheetRequest = new UpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setMinPoints(40);
        updateExerciseSheetRequest.setMinKreuzel(55);
        updateExerciseSheetRequest.setName("ddddd");
        updateExerciseSheetRequest.setSubmissionDate(LocalDateTime.now());
        updateExerciseSheetRequest.setIssueDate(LocalDateTime.now());
        updateExerciseSheetRequest.setIncludeThird(Boolean.TRUE);
        updateExerciseSheetRequest.setDescription("aslödjfkas");
        updateExerciseSheetRequest.setId(EXERCISE_SHEET_ID);

        return updateExerciseSheetRequest;
    }

    private List<Example> getTextExamples ()
    {
        List<Example> exampleList = new ArrayList<>();
        Example example1 = new Example();
        example1.setId(EXAMPLE_ID);
        example1.setName("example1");
        example1.setOrder(1);
        example1.setSubExamples(new HashSet<>());

        Example example2 = new Example();
        example2.setId(EXAMPLE_ID+10);
        example2.setName("example2");
        example2.setOrder(0);
        example2.setSubExamples(new HashSet<>());


        Example example3 = new Example();
        example3.setId(EXAMPLE_ID+20);
        example3.setName("example3");
        example3.setOrder(1);
        example3.setParentExample(example1);

        Example example4 = new Example();
        example4.setId(EXAMPLE_ID+30);
        example4.setName("example4");
        example4.setOrder(0);
        example4.setParentExample(example1);

        Example example5 = new Example();
        example5.setId(EXAMPLE_ID+40);
        example5.setName("example5");
        example5.setOrder(0);
        example5.setParentExample(example2);

        Example example6 = new Example();
        example6.setId(EXAMPLE_ID+40);
        example6.setName("example6");
        example6.setOrder(2);

        example1.getSubExamples().add(example3);
        example1.getSubExamples().add(example4);
        example2.getSubExamples().add(example5);

        exampleList.add(example1);
        exampleList.add(example2);
        exampleList.add(example3);
        exampleList.add(example4);
        exampleList.add(example5);
        exampleList.add(example6);
        exampleList.forEach(example -> example.setExamplesFinishedByUser(new HashSet<>()));

        return exampleList;
    }

    private List<Example> getTextExamplesWithFinisExamples ()
    {
        List<Example> exampleList = getTextExamples();


        return exampleList;
    }

    private List<UserInCourse> getTestUserInCourse ()
    {
        List<UserInCourse> userInCourses = new ArrayList<>();
        User user1 = new User();
        user1.setMatriculationNumber("44444444");
        user1.setForename("forename1");
        user1.setSurname("bbbb");

        User user2 = new User();
        user2.setMatriculationNumber("33333333");
        user2.setForename("bforename");
        user2.setSurname("aaaa");

        User user3 = new User();
        user3.setMatriculationNumber("22222222");
        user3.setForename("aforename");
        user3.setSurname("aaaa");

        User user4 = new User();
        user4.setMatriculationNumber("11111111");
        user4.setForename("bforename");
        user4.setSurname("aaaa");

        UserInCourse userInCourse1 = new UserInCourse();
        userInCourse1.setUser(user1);
        userInCourse1.setRole(ECourseRole.STUDENT);

        UserInCourse userInCourse2 = new UserInCourse();
        userInCourse2.setUser(user2);
        userInCourse2.setRole(ECourseRole.STUDENT);

        UserInCourse userInCourse3 = new UserInCourse();
        userInCourse3.setUser(user3);
        userInCourse3.setRole(ECourseRole.STUDENT);

        UserInCourse userInCourse4 = new UserInCourse();
        userInCourse4.setUser(user4);
        userInCourse4.setRole(ECourseRole.STUDENT);

        userInCourses.add(userInCourse1);
        userInCourses.add(userInCourse2);
        userInCourses.add(userInCourse3);
        userInCourses.add(userInCourse4);

        return userInCourses;
    }

    private ExerciseSheetResponseObject createExerciseSheetResponseObject(ExerciseSheet exerciseSheet) {
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setName(exerciseSheet.getName());
        responseObject.setSubmissionDate(exerciseSheet.getSubmissionDate());
        responseObject.setId(exerciseSheet.getId());

        return responseObject;
    }
}
