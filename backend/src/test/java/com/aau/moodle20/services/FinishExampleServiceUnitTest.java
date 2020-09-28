package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.UserKreuzeMultilRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.repository.*;
import com.aau.moodle20.validation.ValidatorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setExample(example);
        finishesExample.setUser(normalUser);
        finishesExample.setRemainingUploadCount(4);

        UserInCourse userInCourse = createUserInCourse(normalUser,example.getExerciseSheet().getCourse());
        normalUser.getCourses().add(userInCourse);

        when(validator.validate(anyString())).thenReturn(new ArrayList<>());
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(example.getId(),normalUser.getMatriculationNumber())).thenReturn(Optional.of(finishesExample));

        finishesExampleService.setKreuzelUserAttachment(file,EXAMPLE_ID);
        // TODO add verification
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
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
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


}
