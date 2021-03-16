package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzeMultilRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.KreuzelResponse;
import com.aau.moodle20.payload.response.ViolationHistoryResponse;
import com.aau.moodle20.payload.response.ViolationResponse;
import com.aau.moodle20.repository.*;
import com.aau.moodle20.validation.ValidatorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import validation.IValidator;
import validation.Violation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class FinishExampleServiceUnitTest extends AbstractServiceTest{

    @InjectMocks
    private FinishesExampleService finishesExampleService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private ExampleRepository exampleRepository;
    @Mock
    private FinishesExampleRepository finishesExampleRepository;
    @Mock
    private ValidatorHandler validatorHandler;
    @Mock
    private ViolationHistoryRepository violationHistoryRepository;
    @Mock
    private CourseRepository courseRepository;


    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void setKreuzelUser_empty()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        finishesExampleService.setKreuzelUser(userKreuzelRequests);
        verify(finishesExampleRepository,times(0)).findByExample_IdAndUser_MatriculationNumber(anyLong(),anyString());
        verify(finishesExampleRepository,times(0)).save(any(FinishesExample.class));
    }

    @Test
    public void setKreuzelUser_example_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User adminUser = getAdminUser();
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createUserKreuzelRequest());
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUser(userKreuzelRequests);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUser_forbidden()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = mockExample();
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createUserKreuzelRequest());

        User adminUser = getAdminUser();
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUser(userKreuzelRequests);
        });
        String expectedMessage = "Error: Access denied";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN,exception.getHttpStatus());

        UserInCourse userInCourse = createUserInCourse(adminUser,example.getExerciseSheet().getCourse());
        userInCourse.setRole(ECourseRole.NONE);
        adminUser.getCourses().add(userInCourse);
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUser(userKreuzelRequests);
        });

        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUser_example_is_parent_example()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createUserKreuzelRequest());

        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        example.getSubExamples().add(getTestExample(EXAMPLE_ID+20));

        User adminUser = getAdminUser();
        adminUser.getCourses().add(createUserInCourse(adminUser,example.getExerciseSheet().getCourse()));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUser(userKreuzelRequests);
        });
        String expectedMessage = "Error: Example has sub-examples and can therefore not be kreuzelt";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void setKreuzelUser_createFinishExample()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createUserKreuzelRequest());

        Example example = mockExample();
        example.setSubExamples(new HashSet<>());

        User adminUser = getAdminUser();
        adminUser.getCourses().add(createUserInCourse(adminUser,example.getExerciseSheet().getCourse()));
        FinishesExample expectedFinishExample = createFinishExample(adminUser,example,userKreuzelRequests.get(0));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminUser.getMatriculationNumber())).thenReturn(Optional.empty());

        finishesExampleService.setKreuzelUser(userKreuzelRequests);
        verify(finishesExampleRepository,times(1)).findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminMatriculationNumber);
        verify(finishesExampleRepository,times(1)).save(expectedFinishExample);
    }

    @Test
    public void setKreuzelUser_updateFinishExample()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzelRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createUserKreuzelRequest());

        Example example = mockExample();
        example.setSubExamples(new HashSet<>());

        User adminUser = getAdminUser();
        adminUser.getCourses().add(createUserInCourse(adminUser,example.getExerciseSheet().getCourse()));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        FinishesExample finishesExample = createFinishExample(adminUser,example,userKreuzelRequests.get(0));
        finishesExample.setDescription("strange description");
        finishesExample.setState(EFinishesExampleState.NO);
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),adminUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));

        FinishesExample expectedFinishExample = createFinishExample(adminUser,example,userKreuzelRequests.get(0));
        finishesExampleService.setKreuzelUser(userKreuzelRequests);
        verify(finishesExampleRepository,times(1)).save(expectedFinishExample);
    }

    @Test
    public void setKreuzelUserMulti_empty()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        verify(finishesExampleRepository,times(0)).findByExample_IdAndUser_MatriculationNumber(anyLong(),anyString());
        verify(finishesExampleRepository,times(0)).save(any(FinishesExample.class));
        verify(exampleRepository,times(0)).findById(anyLong());

    }


    @Test
    public void setKreuzelUserMulti_example_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User adminUser = getAdminUser();
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        userKreuzelRequests.add(createKreuzelMultiRequest(adminUser.getMatriculationNumber()));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUserMulti_not_admin_or_user()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        User adminUser = getAdminUser();
        adminUser.getCourses().add(createUserInCourse(adminUser,example.getExerciseSheet().getCourse()));

        example.getExerciseSheet().getCourse().setOwner(adminUser);

        userKreuzelRequests.add(createKreuzelMultiRequest(adminUser.getMatriculationNumber()));

        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminUser.getMatriculationNumber())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        });
        String expectedMessage = "Error: Access denied";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN,exception.getHttpStatus());
    }


    @Test
    public void setKreuzelUserMulti_createFinishExample_user_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        User adminUser = getAdminUser();

        userKreuzelRequests.add(createKreuzelMultiRequest(normalMatriculationNumber));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminUser.getMatriculationNumber())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUserMulti_createFinishExample_admin()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        User adminUser = getAdminUser();

        userKreuzelRequests.add(createKreuzelMultiRequest(adminUser.getMatriculationNumber()));
        FinishesExample expectedFinishExample = createFinishExample(adminUser,example,userKreuzelRequests.get(0));
        when(userRepository.findByMatriculationNumber(adminUser.getMatriculationNumber())).thenReturn(Optional.of(adminUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminUser.getMatriculationNumber())).thenReturn(Optional.empty());

        finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        verify(finishesExampleRepository,times(1)).findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,adminMatriculationNumber);
        verify(finishesExampleRepository,times(1)).save(expectedFinishExample);
    }

    @Test
    public void setKreuzelUserMulti_createFinishExample_owner()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        User normalUser = getNormalUser();
        example.getExerciseSheet().getCourse().setOwner(normalUser);

        userKreuzelRequests.add(createKreuzelMultiRequest(normalUser.getMatriculationNumber()));
        FinishesExample expectedFinishExample = createFinishExample(normalUser,example,userKreuzelRequests.get(0));
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,normalUser.getMatriculationNumber())).thenReturn(Optional.empty());

        finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        verify(finishesExampleRepository,times(1)).findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,normalMatriculationNumber);
        verify(finishesExampleRepository,times(1)).save(expectedFinishExample);
    }


    @Test
    public void setKreuzelUserMulti_updateFinishExample_owner()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<UserKreuzeMultilRequest> userKreuzelRequests = new ArrayList<>();
        Example example = mockExample();
        example.setSubExamples(new HashSet<>());
        User normalUser = getNormalUser();
        example.getExerciseSheet().getCourse().setOwner(normalUser);

        userKreuzelRequests.add(createKreuzelMultiRequest(normalUser.getMatriculationNumber()));
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));

        FinishesExample finishesExample = createFinishExample(normalUser,example,userKreuzelRequests.get(0));
        finishesExample.setState(EFinishesExampleState.NO);
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));

        FinishesExample expectedFinishExample = createFinishExample(normalUser,example,userKreuzelRequests.get(0));
        expectedFinishExample.setDescription(null);
        finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        verify(finishesExampleRepository,times(1)).findByExample_IdAndUser_MatriculationNumber(EXAMPLE_ID,normalMatriculationNumber);
        verify(finishesExampleRepository,times(1)).save(expectedFinishExample);
    }

    @Test
    public void setKreuzelUserAttachment_example_submitFile_false()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = mockExample();
        example.setSubmitFile(Boolean.FALSE);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: not submit file for this example";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void setKreuzelUserAttachment_example_file_is_empty()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = mockExample();
        example.setSubmitFile(Boolean.TRUE);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{});

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: given file is empty";
        assertEquals(expectedMessage,exception.getMessage());
    }


    @Test
    public void setKreuzelUserAttachment_example_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUserAttachment_not_assigned_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        Example example = mockExample();
        example.setSubmitFile(Boolean.TRUE);
        User normalUser = getNormalUser();
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: Not assigned to this course";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN,exception.getHttpStatus());
    }

    @Test
    public void setKreuzelUserAttachment_uploadCount_reached()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        Example example = mockExample();
        example.setSubmitFile(Boolean.TRUE);
        User normalUser = getNormalUser();
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setExample(example);
        finishesExample.setUser(normalUser);
        finishesExample.setRemainingUploadCount(0);

        UserInCourse userInCourse = createUserInCourse(normalUser,example.getExerciseSheet().getCourse());
        normalUser.getCourses().add(userInCourse);

        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: max upload counts reached!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void setKreuzelUserAttachment() throws IOException, ClassNotFoundException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        IValidator validator = mock(IValidator.class);

        Example example = mockExample();
        example.setSubmitFile(Boolean.TRUE);
        User normalUser = getNormalUser();
        FinishesExample finishesExample = createFinishExample(example,normalUser);
        finishesExample.setRemainingUploadCount(4);

        FinishesExample finishesExample_expected = createFinishExample(example,normalUser);
        finishesExample_expected.setRemainingUploadCount(3);
        finishesExample_expected.setFileName(file.getOriginalFilename());

        UserInCourse userInCourse = createUserInCourse(normalUser,example.getExerciseSheet().getCourse());
        normalUser.getCourses().add(userInCourse);
        List<Violation> violations = new ArrayList<>();
        violations.add(new Violation("ddd"));


        doReturn(violations).when(validator).validate(anyString());
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));
        when(finishesExampleRepository.existsByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(validatorHandler.loadValidator(anyString())).thenReturn(validator);

        ViolationHistoryResponse response = finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);

        verify(finishesExampleRepository).existsByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber());
        verify(finishesExampleRepository).saveAndFlush(finishesExample_expected);

        ArgumentCaptor<ViolationHistory> argumentCaptor = ArgumentCaptor.forClass(ViolationHistory.class);
        verify(violationHistoryRepository).saveAndFlush(argumentCaptor.capture());
        ViolationHistory violationHistory = argumentCaptor.getValue();
        assertEquals(1,violationHistory.getViolations().size());
        assertEquals(createViolationEntities(violations).iterator().next(),violationHistory.getViolations().iterator().next());
        assertNotNull(violationHistory.getDate());

        verify(violationHistoryRepository).save(argumentCaptor.capture());
        violationHistory = argumentCaptor.getValue();

        assertEquals(finishesExample_expected,violationHistory.getFinishesExample());
        assertNotNull(violationHistory.getViolations());
        for(ViolationEntity violation: violationHistory.getViolations())
        {
            assertEquals(violationHistory,violation.getViolationHistory());
        }
        assertEquals(createViolationHistoryResponse(violationHistory),response);
    }

    @Test
    public void setKreuzelUserAttachment_create_finish_example() throws IOException, ClassNotFoundException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        IValidator validator = mock(IValidator.class);

        Example example = mockExample();
        example.setSubmitFile(Boolean.TRUE);
        User normalUser = getNormalUser();
        FinishesExample finishesExample = createFinishExample(example,normalUser);
        finishesExample.setRemainingUploadCount(4);

        FinishesExample finishesExample_expected = createFinishExample(example,normalUser);
        finishesExample_expected.setRemainingUploadCount(3);
        finishesExample_expected.setFileName(file.getOriginalFilename());

        UserInCourse userInCourse = createUserInCourse(normalUser,example.getExerciseSheet().getCourse());
        normalUser.getCourses().add(userInCourse);
        List<Violation> violations = new ArrayList<>();
        violations.add(new Violation("ddd"));

        // define mock method results
        doReturn(violations).when(validator).validate(anyString());
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));
        when(finishesExampleRepository.existsByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Boolean.FALSE);
        when(validatorHandler.loadValidator(anyString())).thenReturn(validator);

        ViolationHistoryResponse response = finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);

        // verify method correctness
        verify(finishesExampleRepository).existsByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber());
        verify(finishesExampleRepository,times(2)).findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber());
        verify(finishesExampleRepository).save(any(FinishesExample.class));
        verify(finishesExampleRepository).flush();
    }


    @Test
    public void deleteFinishExampleData_fileName_null() throws IOException, ClassNotFoundException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        FinishesExample finishesExample = mock(FinishesExample.class);

        // define mock method results
        when(finishesExample.getFileName()).thenReturn(null);

        finishesExampleService.deleteFinishExampleData(finishesExample);

        // verify method correctness
        verify(finishesExample,times(1)).getFileName();
    }

    @Test
    public void deleteFinishExampleData() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        example.setExerciseSheet(exerciseSheet);
        exerciseSheet.setCourse(course);
        course.setSemester(semester);

        FinishesExample finishesExample = mock(FinishesExample.class);
        FinishesExampleKey key = new FinishesExampleKey(normalMatriculationNumber,EXAMPLE_ID);

        // define mock method results
        when(finishesExample.getFileName()).thenReturn("test.jar");
        when(finishesExample.getId()).thenReturn(key);
        when(finishesExample.getExample()).thenReturn(example);

        finishesExampleService.deleteFinishExampleData(finishesExample);

        // verify method correctness
        verify(finishesExample,times(2)).getFileName();
        verify(finishesExample,times(1)).getId();
        verify(finishesExample,times(1)).getExample();
    }


    @Test
    public void getKreuzelAttachment_finishExample_notFound()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.getKreuzelAttachment(EXAMPLE_ID);
        });
        String expectedMessage = "Error: user did not check this example";
        assertEquals(expectedMessage,exception.getMessage());
    }
    
    @Test
    public void setUserExamplePresented_example_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        UserExamplePresentedRequest request = createUserExamplePresentedRequest();
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setUserExamplePresented(request);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void setUserExamplePresented_user_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        UserExamplePresentedRequest request = createUserExamplePresentedRequest();
        Example example = getTestExample(EXAMPLE_ID);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setUserExamplePresented(request);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void setUserExamplePresented_finishExample_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        UserExamplePresentedRequest request = createUserExamplePresentedRequest();
        Example example = getTestExample(EXAMPLE_ID);
        User normalUser = getNormalUser();
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.setUserExamplePresented(request);
        });
        String expectedMessage = "Error: user did not check this example";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void setUserExamplePresented()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        UserExamplePresentedRequest request = createUserExamplePresentedRequest();
        Example example = getTestExample(EXAMPLE_ID);
        User normalUser = getNormalUser();
        FinishesExample finishesExample = createFinishExample(example,normalUser);
        finishesExample.setHasPresented(Boolean.FALSE);
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));

        finishesExampleService.setUserExamplePresented(request);
        FinishesExample finishesExampleExpected = createFinishExample(example,normalUser);
        finishesExampleExpected.setHasPresented(Boolean.TRUE);
        verify(finishesExampleRepository).save(finishesExampleExpected);
    }

    //getKreuzelUserCourse

    @Test
    public void getKreuzelUserCourse_user_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void getKreuzelUserCourse_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }


    @Test
    public void getKreuzelUserCourse_not_admin_not_owner()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        Course course = getTestCourse();
        course.setOwner(getAdminUser());
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        });
        String expectedMessage = "Error: Not admin or Course Owner!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    public void getKreuzelUserCourse_no_exerciseSheets()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        Course course = getTestCourse();
        course.setOwner(normalUser);
        course.setExerciseSheets(new HashSet<>());
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<KreuzelResponse> responseList =  finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        assertEquals(new ArrayList<>(), responseList);
    }

    @Test
    public void getKreuzelUserCourse_exerciseSheets_no_example()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        Course course = getTestCourse();
        course.setOwner(normalUser);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setExamples(new HashSet<>());
        course.getExerciseSheets().add(exerciseSheet);
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<KreuzelResponse> responseList =  finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        assertEquals(new ArrayList<>(), responseList);
    }


    @Test
    public void getKreuzelUserCourse_exerciseSheets_and_examples()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        Course course = getTestCourse();
        course.setOwner(normalUser);

        course.getExerciseSheets().addAll(getTestExerciseSheets(course,normalUser));
        when(userRepository.findByMatriculationNumber(normalMatriculationNumber)).thenReturn(Optional.of(normalUser));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<KreuzelResponse> responseList =  finishesExampleService.getKreuzelUserCourse(normalMatriculationNumber,COURSE_ID);
        assertEquals(3, responseList.size());
        assertEquals("aaaa",responseList.get(0).getExerciseSheetName());
        assertEquals("bbbb",responseList.get(1).getExerciseSheetName());
        assertEquals("zzzz",responseList.get(2).getExerciseSheetName());

        for(KreuzelResponse response: responseList)
        {
            assertEquals(EXAMPLE_ID+10, response.getExamples().get(0).getExampleId());
            assertEquals(EXAMPLE_ID+30, response.getExamples().get(1).getExampleId());
            assertEquals(EXAMPLE_ID+20, response.getExamples().get(2).getExampleId());
        }
    }

    private UserExamplePresentedRequest createUserExamplePresentedRequest()
    {
        UserExamplePresentedRequest request = new UserExamplePresentedRequest();
        request.setHasPresented(Boolean.TRUE);
        request.setExampleId(EXAMPLE_ID);
        request.setMatriculationNumber(normalMatriculationNumber);

        return request;
    }

    private  ViolationHistoryResponse createViolationHistoryResponse(ViolationHistory violationHistory) {
        ViolationHistoryResponse response = new ViolationHistoryResponse();
        response.setDate(violationHistory.getDate());
        List<ViolationResponse> violations = violationHistory.getViolations().stream().map(ViolationEntity::createViolationResponse).collect(Collectors.toList());
        response.setViolations(violations);

        return response;
    }

    private  Set<ViolationEntity> createViolationEntities(List<? extends Violation> violations) {
        Set<ViolationEntity> violationEntities = new HashSet<>();
        for (Violation violation : violations) {
            ViolationEntity entity = new ViolationEntity();
            entity.setResult(violation.getResult());
            violationEntities.add(entity);
        }
        return violationEntities;
    }

    private FinishesExample createFinishExample(Example example, User user)
    {
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setExample(example);
        finishesExample.setUser(user);
        finishesExample.setRemainingUploadCount(4);

        return finishesExample;
    }

    private Example getExampleWithSemester()
    {
        Example example = getTestExample(EXAMPLE_ID);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        example.setExerciseSheet(exerciseSheet);
        exerciseSheet.setCourse(course);
        course.setSemester(semester);

        return example;
    }

    private UserKreuzelRequest createUserKreuzelRequest()
    {
        UserKreuzelRequest request = new UserKreuzelRequest();
        request.setExampleId(EXAMPLE_ID);
        request.setDescription("description");
        request.setState(EFinishesExampleState.MAYBE);

        return request;
    }

    private UserKreuzeMultilRequest createKreuzelMultiRequest(String matriculationNumber)
    {
        UserKreuzeMultilRequest request = new UserKreuzeMultilRequest();
        request.setExampleId(EXAMPLE_ID);
        request.setMatriculationNumber(matriculationNumber);
        request.setState(EFinishesExampleState.YES);

        return request;
    }


    private Example mockExample() {
        Example example = getTestExample(EXAMPLE_ID);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        Course course = getTestCourse();
        Semester semester = getTestSemester();
        example.setExerciseSheet(exerciseSheet);
        exerciseSheet.setCourse(course);
        course.setSemester(semester);

        when(exampleRepository.findById(example.getId())).thenReturn(Optional.of(example));

        return example;
    }


    private UserInCourse createUserInCourse(User user, Course course)
    {
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setRole(ECourseRole.STUDENT);
        userInCourse.setUser(user);
        userInCourse.setCourse(course);

        UserCourseKey key = new UserCourseKey();
        key.setMatriculationNumber(user.getMatriculationNumber());
        key.setCourseId(course.getId());

        userInCourse.setId(key);
        return userInCourse;
    }

    private FinishesExample createFinishExample(User user, Example example, UserKreuzelRequest request) {
        FinishesExampleKey finishesExampleKey = new FinishesExampleKey(user.getMatriculationNumber(), example.getId());
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setId(finishesExampleKey);
        finishesExample.setExample(example);
        finishesExample.setUser(user);
        finishesExample.setDescription(request.getDescription());
        finishesExample.setState(request.getState());
        finishesExample.setRemainingUploadCount(example.getUploadCount());

        return finishesExample;
    }

    private FinishesExample createFinishExample(User user, Example example, UserKreuzeMultilRequest request) {
        FinishesExampleKey finishesExampleKey = new FinishesExampleKey(user.getMatriculationNumber(), example.getId());
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setId(finishesExampleKey);
        finishesExample.setExample(example);
        finishesExample.setUser(user);
        finishesExample.setState(request.getState());
        finishesExample.setRemainingUploadCount(example.getUploadCount());

        return finishesExample;
    }


    protected List<ExerciseSheet> getTestExerciseSheets(Course course,User user)
    {
        List<ExerciseSheet> exerciseSheetList = new ArrayList<>();

        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setCourse(course);
        exerciseSheet.setName("zzzz");
        exerciseSheet.setSubmissionDate(LocalDateTime.now().plusDays(10));
        exerciseSheet.getExamples().addAll(getTestExamples(user));
        course.getExerciseSheets().add(exerciseSheet);

        LocalDateTime now = LocalDateTime.now();

        ExerciseSheet exerciseSheet1 = getTestExerciseSheet(EXERCISE_SHEET_ID+10);
        exerciseSheet1.setCourse(course);
        exerciseSheet1.setName("bbbb");
        exerciseSheet1.setSubmissionDate(now);
        exerciseSheet1.getExamples().addAll(getTestExamples(user));

        course.getExerciseSheets().add(exerciseSheet1);

        ExerciseSheet exerciseSheet2 = getTestExerciseSheet(EXERCISE_SHEET_ID+20);
        exerciseSheet2.setCourse(course);
        exerciseSheet2.setName("aaaa");
        exerciseSheet2.setSubmissionDate(now);
        exerciseSheet2.getExamples().addAll(getTestExamples(user));

        course.getExerciseSheets().add(exerciseSheet2);

        exerciseSheetList.add(exerciseSheet);
        exerciseSheetList.add(exerciseSheet1);
        exerciseSheetList.add(exerciseSheet2);

        return exerciseSheetList;
    }

    private List<Example> getTestExamples(User user)
    {
        Example example = getTestExample(EXAMPLE_ID);
        example.setOrder(1);
        example.getExamplesFinishedByUser().add(createFinishExample(example,user));

        Example example1 = getTestExample(EXAMPLE_ID+10);
        example1.setOrder(0);
        example1.getExamplesFinishedByUser().add(createFinishExample(example1,user));

        Example example2 = getTestExample(EXAMPLE_ID+20);
        example2.setOrder(1);
        example2.setParentExample(example);
        example2.getExamplesFinishedByUser().add(createFinishExample(example2,user));
        example.setSubExamples(new HashSet<>());
        example.getSubExamples().add(example2);

        Example example3 = getTestExample(EXAMPLE_ID+30);
        example3.setOrder(0);
        example3.setName("testExample3");
        example3.setParentExample(example);
        example3.getExamplesFinishedByUser().add(createFinishExample(example3,user));
        example.getSubExamples().add(example3);


        List<Example> exampleList = new ArrayList<>();
        exampleList.add(example);
        exampleList.add(example1);

        return exampleList;
    }
}
