package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.repository.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class UserCourseServiceUnitTest extends AbstractServiceTest{


    @InjectMocks
    private UserCourseService userCourseService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserInCourseRepository userInCourseRepository;
    @Mock
    private FinishesExampleRepository finishesExampleRepository;
    @Mock
    private FinishesExampleService finishesExampleService;

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCourseAssigned_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.getCourseAssigned(COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getCourseAssigned_user_not_find()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.getCourseAssigned(COURSE_ID);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getCourseAssigned_user_not_assigned()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        User user = getTestUser();
        when(userRepository.findByMatriculationNumber(user.getMatriculationNumber())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.getCourseAssigned(COURSE_ID);
        });
        String expectedMessage = "Error: User is not assigned to this course!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getCourseAssigned_role_none()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        User user = getTestUser();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(user);
        userInCourse.setCourse(course);
        userInCourse.setId(new UserCourseKey(normalMatriculationNumber,course.getId()));
        userInCourse.setRole(ECourseRole.NONE);
        user.getCourses().add(userInCourse);
        when(userRepository.findByMatriculationNumber(user.getMatriculationNumber())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.getCourseAssigned(COURSE_ID);
        });
        String expectedMessage = "Error: User is not assigned to this course!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }


    @Test
    public void getCourseAssigned_not_right_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        User user = getTestUser();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(user);
        userInCourse.setCourse(new Course(400L));
        userInCourse.setId(new UserCourseKey(normalMatriculationNumber,course.getId()));
        userInCourse.setRole(ECourseRole.STUDENT);
        user.getCourses().add(userInCourse);
        when(userRepository.findByMatriculationNumber(user.getMatriculationNumber())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.getCourseAssigned(COURSE_ID);
        });
        String expectedMessage = "Error: User is not assigned to this course!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getCourseAssigned()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        User user = getTestUser();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(user);
        userInCourse.setCourse(course);
        userInCourse.setId(new UserCourseKey(normalMatriculationNumber,course.getId()));
        userInCourse.setRole(ECourseRole.STUDENT);
        user.getCourses().add(userInCourse);
        when(userRepository.findByMatriculationNumber(user.getMatriculationNumber())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = userCourseService.getCourseAssigned(COURSE_ID);
        CourseResponseObject expectedResponseObject  = course.createCourseResponseObjectGetCourse();
        assertEquals(expectedResponseObject,responseObject);
    }


    @Test
    public void assignFile_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.assignFile(file,COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void assignFile_course()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "resources/test.csv", MediaType.MULTIPART_FORM_DATA_VALUE, "resources/test.csv".getBytes());
        Course course = getTestCourse();
        User user = getTestUser();
        List<User> allGivenUsers = new ArrayList<>();
        allGivenUsers.add(user);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userService.registerMissingUsersFromFile(any(MultipartFile.class),any(RegisterMultipleUserResponse.class))).thenReturn(allGivenUsers);

        RegisterMultipleUserResponse responseObject = userCourseService.assignFile(file,COURSE_ID);
        RegisterMultipleUserResponse expectedResponseObject  = new RegisterMultipleUserResponse();
        assertEquals(expectedResponseObject.getFailedUsers(),responseObject.getFailedUsers());
        assertEquals(expectedResponseObject.getRegisteredUsers(),responseObject.getRegisteredUsers());
    }

    @Test
    public void assignUsers_user_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.assignUsers(assignUserToCourseRequests);
        });
        String expectedMessage = "Error: User with matrikulationNumber:" + assignUserToCourseRequests.get(0).getMatriculationNumber() + " does not exist";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void assignUsers_course_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        when(userRepository.existsByMatriculationNumber(assignUserToCourseRequests.get(0).getMatriculationNumber())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.assignUsers(assignUserToCourseRequests);
        });
        String expectedMessage = "Error: Course with id:" + assignUserToCourseRequests.get(0).getCourseId() + " does not exist";
        assertEquals(expectedMessage,exception.getMessage());
    }
    @Test
    public void assignUsers_user_not_owner()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        User user = getTestUser();
        user.setMatriculationNumber("33333333");
        course.setOwner(user);
        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        when(userRepository.existsByMatriculationNumber(assignUserToCourseRequests.get(0).getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(courseRepository.existsById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Boolean.TRUE);
        when(courseRepository.findById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Optional.of(course));


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userCourseService.assignUsers(assignUserToCourseRequests);
        });
        String expectedMessage = "Error: User is not owner of course: " + assignUserToCourseRequests.get(0).getCourseId();
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void assignUsers_none_role_no_UserInCourse_found() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        course.setOwner(getTestUser());
        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        assignUserToCourseRequests.get(0).setRole(ECourseRole.NONE);
        when(userRepository.existsByMatriculationNumber(assignUserToCourseRequests.get(0).getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(courseRepository.existsById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Boolean.TRUE);
        when(courseRepository.findById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Optional.of(course));

        userCourseService.assignUsers(assignUserToCourseRequests);
        final ArgumentCaptor<List<UserInCourse>> listCaptor
                = ArgumentCaptor.forClass(List.class);
        verify(userInCourseRepository).saveAll(listCaptor.capture());
        List<UserInCourse> userInCourseList = listCaptor.getValue();
        assertEquals(1,userInCourseList.size());
        assertEquals(assignUserToCourseRequests.get(0).getCourseId(),userInCourseList.get(0).getCourse().getId());
        assertEquals(assignUserToCourseRequests.get(0).getMatriculationNumber(),userInCourseList.get(0).getUser().getMatriculationNumber());
        assertEquals(assignUserToCourseRequests.get(0).getRole(),userInCourseList.get(0).getRole());
    }
    @Test
    public void assignUsers_none_role() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourseWithExerciseSheet();
        User testUser = getTestUser();
        course.setOwner(getTestUser());
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setUser(testUser);
        userInCourse.setCourse(course);
        userInCourse.setId(new UserCourseKey(testUser.getMatriculationNumber(),course.getId()));
        userInCourse.setRole(ECourseRole.STUDENT);

        Example example = getTestExample();
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        example.setExerciseSheet(exerciseSheet);
        exerciseSheet.setCourse(course);

        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setExample(example);
        testUser.getFinishedExamples().add(finishesExample);

        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        assignUserToCourseRequests.get(0).setRole(ECourseRole.NONE);
        when(userRepository.existsByMatriculationNumber(assignUserToCourseRequests.get(0).getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(courseRepository.existsById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Boolean.TRUE);
        when(courseRepository.findById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Optional.of(course));
        when(userInCourseRepository.findByUserMatriculationNumberAndCourseId(assignUserToCourseRequests.get(0).getMatriculationNumber(),assignUserToCourseRequests.get(0).getCourseId()))
                .thenReturn(Optional.of(userInCourse));
        when(userRepository.findByMatriculationNumber(testUser.getMatriculationNumber())).thenReturn(Optional.of(testUser));
        doNothing().when(finishesExampleService).deleteFinishExampleData(any(FinishesExample.class));

        userCourseService.assignUsers(assignUserToCourseRequests);
        verify(finishesExampleRepository).deleteAll(any(List.class));

        final ArgumentCaptor<List<UserInCourse>> listCaptor
                = ArgumentCaptor.forClass(List.class);
        verify(userInCourseRepository).saveAll(listCaptor.capture());
        List<UserInCourse> userInCourseList = listCaptor.getValue();
        assertEquals(1,userInCourseList.size());
        assertEquals(assignUserToCourseRequests.get(0).getCourseId(),userInCourseList.get(0).getCourse().getId());
        assertEquals(assignUserToCourseRequests.get(0).getMatriculationNumber(),userInCourseList.get(0).getUser().getMatriculationNumber());
        assertEquals(assignUserToCourseRequests.get(0).getRole(),userInCourseList.get(0).getRole());
    }
    @Test
    public void assignUsers() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        course.setOwner(getTestUser());
        List<AssignUserToCourseRequest> assignUserToCourseRequests = getListAssignUserRequest();
        when(userRepository.existsByMatriculationNumber(assignUserToCourseRequests.get(0).getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(courseRepository.existsById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Boolean.TRUE);
        when(courseRepository.findById(assignUserToCourseRequests.get(0).getCourseId())).thenReturn(Optional.of(course));

        userCourseService.assignUsers(assignUserToCourseRequests);

        final ArgumentCaptor<List<UserInCourse>> listCaptor
                = ArgumentCaptor.forClass(List.class);
        verify(userInCourseRepository).saveAll(listCaptor.capture());
        List<UserInCourse> userInCourseList = listCaptor.getValue();
        assertEquals(1,userInCourseList.size());
        assertEquals(assignUserToCourseRequests.get(0).getCourseId(),userInCourseList.get(0).getCourse().getId());
        assertEquals(assignUserToCourseRequests.get(0).getMatriculationNumber(),userInCourseList.get(0).getUser().getMatriculationNumber());
        assertEquals(assignUserToCourseRequests.get(0).getRole(),userInCourseList.get(0).getRole());
    }

    private User getTestUser()
    {
        User user = new User();
        user.setMatriculationNumber(normalMatriculationNumber);
        user.setSurname("normal");
        user.setForename("normal");
        user.setAdmin(Boolean.TRUE);
        user.setCourses(new HashSet<>());
        user.setFinishedExamples(new HashSet<>());

        return user;
    }


    private List<AssignUserToCourseRequest> getListAssignUserRequest() {
        List<AssignUserToCourseRequest> assignUserToCourseRequests = new ArrayList<>();
        AssignUserToCourseRequest request = new AssignUserToCourseRequest();
        request.setCourseId(COURSE_ID);
        request.setMatriculationNumber(normalMatriculationNumber);
        request.setRole(ECourseRole.STUDENT);
        assignUserToCourseRequests.add(request);

        return assignUserToCourseRequests;
    }

    private Example getTestExample() {
        Example example = new Example();
        example.setId(EXAMPLE_ID);
        example.setValidator("test.txt");
        example.setExamplesFinishedByUser(new HashSet<>());

        return example;

    }
}
