package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.FailedUserResponse;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import com.aau.moodle20.repository.UserRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class UserServiceUnitTests extends AbstractServiceTest{

    @InjectMocks
    private UserService userService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserInCourseRepository userInCourseRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private EmailService emailService;

    @BeforeClass
    public static void setEnvironmentVariables()
    {
        System.setProperty("developerMode", "false");
    }

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);

        // create the real bean
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);
        messageSource.setBasenames("messages"); // this folder is just for testing and it contains a messages_en.properties file

        // inject the bean into the class I wanted to test
        ReflectionTestUtils.setField(userService, "resourceBundleMessageSource", messageSource);
        ReflectionTestUtils.setField(userService, "developerMode", Boolean.FALSE);
        ReflectionTestUtils.setField(userService, "tempPasswordExpirationHours", 24L);
        ReflectionTestUtils.setField(userService, "adminMatriculationNumber", "00000000");
    }


    @Test
    public void registerUser_matriculationNumber_already_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User user = getNormalUser();
        when(userRepository.existsByMatriculationNumber(user.getMatriculationNumber())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.registerUser(createSignUpRequest());
        });
        String expectedMessage = "Error: User with this matriculationNumber already exists!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.MATRICULATION_NUMBER_ALREADY_EXISTS,exception.getErrorResponseCode());
    }

    @Test
    public void registerUser_username_already_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        SignUpRequest request = createSignUpRequest();
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.registerUser(createSignUpRequest());
        });
        String expectedMessage = "Error: User with this username already exists!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.USERNAME_ALREADY_EXISTS,exception.getErrorResponseCode());
    }

    @Test
    public void registerUser() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        SignUpRequest request = createSignUpRequest();
        doNothing().when(emailService).sendEmail(anyString(),anyString(),anyString());
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(Boolean.FALSE);
        when(userRepository.existsByMatriculationNumber(request.getMatriculationNumber())).thenReturn(Boolean.FALSE);

        userService.registerUser(createSignUpRequest());

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(argumentCaptor.capture());
        User capturedUser = argumentCaptor.getValue();

        assertEquals(request.getEmail(),capturedUser.getEmail());
        assertEquals(request.getForename(),capturedUser.getForename());
        assertEquals(request.getIsAdmin(),capturedUser.getAdmin());
        assertEquals(request.getMatriculationNumber(),capturedUser.getMatriculationNumber());
        assertEquals(request.getSurname(),capturedUser.getSurname());
        assertEquals(request.getUsername(),capturedUser.getUsername());
        assertNotNull(capturedUser.getPasswordExpireDate());

        verify(emailService,times(1)).sendEmail(anyString(),anyString(),anyString());
    }

    @Test
    public void registerUsers() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        doNothing().when(emailService).sendEmail(anyString(),anyString(),anyString());
        when(userRepository.existsByUsername("ppipp2")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByUsername("ppipp3")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("12345678")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("88888888")).thenReturn(Boolean.TRUE);

        RegisterMultipleUserResponse registerMultipleUserResponse = userService.registerUsers(getTestCSVMultipartFile(),Boolean.FALSE);

        assertEquals(1,registerMultipleUserResponse.getRegisteredUsers().size());
        assertEquals("11111111",registerMultipleUserResponse.getRegisteredUsers().get(0).getMatriculationNumber());
        assertEquals(3,registerMultipleUserResponse.getFailedUsers().size());

        // first wrong matriculation number format
        FailedUserResponse failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(0);
        assertEquals(1,failedUserResponse.getLineNumber());
        assertEquals("Error: Wrong format for matriculationNumber",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_WRONG_MATRICULATION_NUMBER_FORMAT,failedUserResponse.getStatusCode());

        // second  matriculation number exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(1);
        assertEquals(2,failedUserResponse.getLineNumber());
        assertEquals("Error: Matriculation number already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_MATRICULATION_ALREADY_EXISTS,failedUserResponse.getStatusCode());

        // thirs  username exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(2);
        assertEquals(3,failedUserResponse.getLineNumber());
        assertEquals("Error: Username already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_USERNAME_ALREADY_EXISTS,failedUserResponse.getStatusCode());

    }

    @Test
    public void registerUsers_developerMode_true() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        ReflectionTestUtils.setField(userService, "developerMode", Boolean.TRUE);

        doNothing().when(emailService).sendEmail(anyString(),anyString(),anyString());
        when(userRepository.existsByUsername("ppipp2")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByUsername("ppipp3")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("12345678")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("88888888")).thenReturn(Boolean.TRUE);

        RegisterMultipleUserResponse registerMultipleUserResponse = userService.registerUsers(getTestCSVMultipartFile(),Boolean.FALSE);

        assertEquals(1,registerMultipleUserResponse.getRegisteredUsers().size());
        assertEquals("11111111",registerMultipleUserResponse.getRegisteredUsers().get(0).getMatriculationNumber());
        assertEquals(3,registerMultipleUserResponse.getFailedUsers().size());

        // first wrong matriculation number format
        FailedUserResponse failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(0);
        assertEquals(1,failedUserResponse.getLineNumber());
        assertEquals("Error: Wrong format for matriculationNumber",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_WRONG_MATRICULATION_NUMBER_FORMAT,failedUserResponse.getStatusCode());

        // second  matriculation number exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(1);
        assertEquals(2,failedUserResponse.getLineNumber());
        assertEquals("Error: Matriculation number already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_MATRICULATION_ALREADY_EXISTS,failedUserResponse.getStatusCode());

        // thirs  username exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(2);
        assertEquals(3,failedUserResponse.getLineNumber());
        assertEquals("Error: Username already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_USERNAME_ALREADY_EXISTS,failedUserResponse.getStatusCode());

    }

    @Test
    public void registerMissingUsersFromFile() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        doNothing().when(emailService).sendEmail(anyString(),anyString(),anyString());
        when(userRepository.existsByUsername("ppipp2")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByUsername("ppipp3")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("12345678")).thenReturn(Boolean.TRUE);
        when(userRepository.existsByMatriculationNumber("88888888")).thenReturn(Boolean.TRUE);

        RegisterMultipleUserResponse registerMultipleUserResponse = new RegisterMultipleUserResponse();
        List<User> allGivenUsers = userService.registerMissingUsersFromFile(getTestCSVMultipartFile(), registerMultipleUserResponse);

        assertEquals(1,registerMultipleUserResponse.getRegisteredUsers().size());
        assertEquals(2,allGivenUsers.size());
        assertEquals("12345678",allGivenUsers.get(0).getMatriculationNumber());
        assertEquals("88888888",allGivenUsers.get(1).getMatriculationNumber());

        assertEquals("11111111",registerMultipleUserResponse.getRegisteredUsers().get(0).getMatriculationNumber());
        assertEquals(3,registerMultipleUserResponse.getFailedUsers().size());

        // first wrong matriculation number format
        FailedUserResponse failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(0);
        assertEquals(1,failedUserResponse.getLineNumber());
        assertEquals("Error: Wrong format for matriculationNumber",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_WRONG_MATRICULATION_NUMBER_FORMAT,failedUserResponse.getStatusCode());

        // second  matriculation number exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(1);
        assertEquals(2,failedUserResponse.getLineNumber());
        assertEquals("Error: Matriculation number already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_MATRICULATION_ALREADY_EXISTS,failedUserResponse.getStatusCode());

        // thirs  username exists
        failedUserResponse = registerMultipleUserResponse.getFailedUsers().get(2);
        assertEquals(3,failedUserResponse.getLineNumber());
        assertEquals("Error: Username already exists",failedUserResponse.getMessage());
        assertEquals(ApiErrorResponseCodes.REGISTER_USERS_USERNAME_ALREADY_EXISTS,failedUserResponse.getStatusCode());
    }

    @Test
    public void getUsersWithCourseRoles_course_not_exits() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getUsersWithCourseRoles(COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getUsersWithCourseRoles_no_users() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userInCourseRepository.findByCourse_Id(COURSE_ID)).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserResponseObject> userResponseObjects = userService.getUsersWithCourseRoles(COURSE_ID);
        assertEquals(new ArrayList<>(),userResponseObjects);
    }

    @Test
    public void getUsersWithCourseRoles() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        Course course1 = getTestCourse();
        course1.setId(COURSE_ID+10);

        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        ExerciseSheet exerciseSheet1 = getTestExerciseSheet(EXERCISE_SHEET_ID+10);

        exerciseSheet.setCourse(course);
        exerciseSheet1.setCourse(course1);

        Example example = getTestExample(EXAMPLE_ID);
        Example example1 = getTestExample(EXAMPLE_ID+10);

        example.setExerciseSheet(exerciseSheet);
        example1.setExerciseSheet(exerciseSheet1);

        List<User> userList = getTestUsers();
        List<UserInCourse> userInCourseList = getTestUserInCourse(course);

        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setHasPresented(Boolean.TRUE);
        finishesExample.setUser(userList.get(1));
        finishesExample.setExample(example);

        FinishesExample finishesExample2 = new FinishesExample();
        finishesExample2.setHasPresented(Boolean.TRUE);
        finishesExample2.setUser(userList.get(1));
        finishesExample2.setExample(example1);

        userList.get(1).getFinishedExamples().add(finishesExample);
        userList.get(1).getFinishedExamples().add(finishesExample2);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userInCourseRepository.findByCourse_Id(COURSE_ID)).thenReturn(userInCourseList);
        when(userRepository.findAll()).thenReturn(userList);

        List<UserResponseObject> userResponseObjects = userService.getUsersWithCourseRoles(COURSE_ID);
        assertEquals(3,userResponseObjects.size());
        assertEquals("22222222",userResponseObjects.get(0).getMatriculationNumber());
        assertEquals(1,userResponseObjects.get(0).getPresentedCount());


        assertEquals("33333333",userResponseObjects.get(1).getMatriculationNumber());
        assertEquals(0,userResponseObjects.get(1).getPresentedCount());
        assertEquals("44444444",userResponseObjects.get(2).getMatriculationNumber());
        assertEquals(0,userResponseObjects.get(1).getPresentedCount());
    }


    @Test
    public void getAllUsers() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        List<User> userList = getTestUsers();
        when(userRepository.findAll()).thenReturn(userList);

        List<UserResponseObject> userResponseObjects = userService.getAllUsers();
        assertEquals(4,userResponseObjects.size());
        assertEquals("11111111",userResponseObjects.get(0).getMatriculationNumber());
        assertEquals("22222222",userResponseObjects.get(1).getMatriculationNumber());
        assertEquals("33333333",userResponseObjects.get(2).getMatriculationNumber());
        assertEquals("44444444",userResponseObjects.get(3).getMatriculationNumber());

    }

    @Test
    public void changePassword_oldPassword_not_match() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        when(userRepository.findByMatriculationNumber(getNormalUser().getMatriculationNumber())).thenReturn(Optional.of(getNormalUser()));
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        when(encoder.matches(anyString(),anyString())).thenReturn(Boolean.FALSE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.changePassword(request);
        });
        String expectedMessage = "Password for User not correct!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void changePassword_rootAdmin_cannot_change_password() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        when(userRepository.findByMatriculationNumber(getAdminUser().getMatriculationNumber())).thenReturn(Optional.of(getAdminUser()));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password");
        request.setNewPassword("newPassword");
        when(encoder.matches(request.getOldPassword(),getAdminUser().getPassword())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.changePassword(request);
        });
        String expectedMessage = "Password for Root Admin cannot be changed";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void changePassword() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        when(userRepository.findByMatriculationNumber(getNormalUser().getMatriculationNumber())).thenReturn(Optional.of(getNormalUser()));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password");
        request.setNewPassword("newPassword");
        when(encoder.matches(request.getOldPassword(),getNormalUser().getPassword())).thenReturn(Boolean.TRUE);
        when(encoder.encode(request.getNewPassword())).thenReturn("encoded");


        userService.changePassword(request);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository,times(1)).save(argumentCaptor.capture());
        User capturedUser = argumentCaptor.getValue();
        assertEquals("encoded",capturedUser.getPassword());
        assertEquals(null, capturedUser.getPasswordExpireDate());
    }

    @Test
    public void updateUser_root_user_not_updatable() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        UpdateUserRequest request = createUpdateUserRequest();
        request.setMatriculationNumber(adminMatriculationNumber);
        when(userRepository.existsByMatriculationNumber(request.getMatriculationNumber())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.updateUser(request);
        });
        String expectedMessage = "Error: Root admin cannot be updated!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void updateUser_root_user_not_updatable_2() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        UpdateUserRequest request = createUpdateUserRequest();
        request.setMatriculationNumber(null);
        when(userRepository.existsByMatriculationNumber(getAdminUser().getMatriculationNumber())).thenReturn(Boolean.TRUE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.updateUser(request);
        });
        String expectedMessage = "Error: Root admin cannot be updated!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void updateUser() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User normalUser = getNormalUser();
        UpdateUserRequest request = createUpdateUserRequest();
        request.setMatriculationNumber(normalUser.getMatriculationNumber());
        when(userRepository.existsByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));

        userService.updateUser(request);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository,times(1)).saveAndFlush(argumentCaptor.capture());
        User capturedUser = argumentCaptor.getValue();
        assertEquals(request.getEmail(),capturedUser.getEmail());
        assertEquals(request.getForename(), capturedUser.getForename());
        assertEquals(request.getSurname(), capturedUser.getSurname());
        assertEquals(request.getIsAdmin(), capturedUser.getAdmin());
    }

    @Test
    public void updateUser_generateNewTemporaryPassword() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User normalUser = getNormalUser();
        normalUser.setPasswordExpireDate(LocalDateTime.now());
        UpdateUserRequest request = createUpdateUserRequest();
        request.setMatriculationNumber(normalUser.getMatriculationNumber());
        when(userRepository.existsByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Boolean.TRUE);
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(encoder.encode(anyString())).thenReturn("encoded");

        userService.updateUser(request);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository,times(1)).saveAndFlush(argumentCaptor.capture());
        User capturedUser = argumentCaptor.getValue();
        assertEquals(request.getEmail(),capturedUser.getEmail());
        assertEquals(request.getForename(), capturedUser.getForename());
        assertEquals(request.getSurname(), capturedUser.getSurname());
        assertEquals(request.getIsAdmin(), capturedUser.getAdmin());


        verify(userRepository,times(1)).save(argumentCaptor.capture());
        capturedUser = argumentCaptor.getValue();
        assertEquals("encoded",capturedUser.getPassword());
    }

    @Test
    public void deleteUser_user_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.deleteUser(normalMatriculationNumber);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void deleteUser_root_admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        when(userRepository.findByMatriculationNumber(getAdminUser().getMatriculationNumber())).thenReturn(Optional.of(getAdminUser()));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.deleteUser(adminMatriculationNumber);
        });
        String expectedMessage = "Super Admin user cannot be deleted!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void deleteUser() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User normalUser = getNormalUser();
        List<Course> courses = new ArrayList<>();
        Course course = getTestCourse();
        course.setOwner(normalUser);
        courses.add(course);
        when(userRepository.findByMatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(Optional.of(normalUser));
        when(userRepository.findByMatriculationNumber(getAdminUser().getMatriculationNumber())).thenReturn(Optional.of(getAdminUser()));

        when(courseRepository.findByOwner_MatriculationNumber(normalUser.getMatriculationNumber())).thenReturn(courses);

        userService.deleteUser(normalMatriculationNumber);
        Course course_expected = getTestCourse();
        course_expected.setOwner(getAdminUser());
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        verify(courseRepository).saveAll(courseList);
        verify(userRepository).delete(normalUser);
    }


    @Test
    public void isOwner() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());

        Boolean isOwner = userService.isOwner();
        assertEquals(Boolean.FALSE,isOwner);

        when(courseRepository.existsByOwner_MatriculationNumber(getUserDetails_Not_Admin().getMatriculationNumber())).thenReturn(Boolean.TRUE);
        isOwner = userService.isOwner();
        assertEquals(Boolean.TRUE,isOwner);
    }

    @Test
    public void getUser_user_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getUser(adminMatriculationNumber);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getUser() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        User adminUser = getAdminUser();
        when(userRepository.findByMatriculationNumber(adminMatriculationNumber)).thenReturn(Optional.of(adminUser));
        UserResponseObject responseObject = userService.getUser(adminMatriculationNumber);

        assertEquals(adminUser.createUserResponseObject(),responseObject);
    }


    @Test
    public void checkForTemporaryPassword_expired() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        normalUser.setPasswordExpireDate(LocalDateTime.now().minusDays(10));
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("normal");
        loginRequest.setPassword("normal");

        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(normalUser));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.checkForTemporaryPassword(loginRequest);
        });
        String expectedMessage = "Error: temporary password is expired";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.TEMPORARY_PASSWORD_EXPIRED,exception.getErrorResponseCode());
    }

    @Test
    public void checkForTemporaryPassword() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        User normalUser = getNormalUser();
        normalUser.setPasswordExpireDate(LocalDateTime.now().plusDays(1));
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("normal");
        loginRequest.setPassword("normal");

        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(normalUser));

        userService.checkForTemporaryPassword(loginRequest);

        verify(userRepository).findByUsername(anyString());
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository,times(1)).save(argumentCaptor.capture());
        User capturedUser = argumentCaptor.getValue();
        assertNull(capturedUser.getPasswordExpireDate());
    }

    private UpdateUserRequest createUpdateUserRequest()
    {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setMatriculationNumber(normalMatriculationNumber);
        request.setSurname("normal");
        request.setForename("normal");
        request.setEmail("normal@edu.aau.at");
        request.setIsAdmin(Boolean.FALSE);

        return request;
    }

    private SignUpRequest createSignUpRequest()
    {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("normal");
        request.setEmail("normal@edu.aau.at");
        request.setSurname("normal");
        request.setForename("normal_forename");
        request.setMatriculationNumber(normalMatriculationNumber);
        request.setIsAdmin(Boolean.FALSE);

        return request;
    }

    private MultipartFile getTestCSVMultipartFile() throws IOException {

        String filepath = "src/test/resources/test.csv";
        MultipartFile multipartFile = new MockMultipartFile(filepath, filepath,"text/csv",
                new FileInputStream(new File(filepath)));
        return multipartFile;
    }

    private List<User> getTestUsers()
    {
        List<User> userList = new ArrayList<>();
        User user = new User();
        user.setMatriculationNumber("11111111");
        user.setSurname("normal");
        user.setForename("normal");
        user.setAdmin(Boolean.TRUE);
        user.setCourses(new HashSet<>());
        user.setFinishedExamples(new HashSet<>());


        User user1 = new User();
        user1.setMatriculationNumber("22222222");
        user1.setSurname("normal");
        user1.setForename("normal");
        user1.setAdmin(Boolean.FALSE);
        user1.setCourses(new HashSet<>());
        user1.setFinishedExamples(new HashSet<>());

        User user2 = new User();
        user2.setMatriculationNumber("33333333");
        user2.setSurname("normal");
        user2.setForename("normal");
        user2.setAdmin(Boolean.FALSE);
        user2.setCourses(new HashSet<>());
        user2.setFinishedExamples(new HashSet<>());

        User user3 = new User();
        user3.setMatriculationNumber("44444444");
        user3.setSurname("normal");
        user3.setForename("normal");
        user3.setAdmin(Boolean.FALSE);
        user3.setCourses(new HashSet<>());
        user3.setFinishedExamples(new HashSet<>());

        userList.add(user);
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);

        return  userList;
    }
    private List<UserInCourse> getTestUserInCourse(Course course)
    {
        List<UserInCourse> userInCourseList = new ArrayList<>();
        UserInCourse userInCourse = new UserInCourse();
        userInCourse.setCourse(course);
        userInCourse.setRole(ECourseRole.STUDENT);
        userInCourse.setUser(new User("22222222"));
        userInCourse.setId(new UserCourseKey("22222222",course.getId()));
        userInCourseList.add(userInCourse);

        UserInCourse userInCourse2 = new UserInCourse();
        userInCourse2.setCourse(course);
        userInCourse2.setRole(ECourseRole.LECTURER);
        userInCourse2.setUser(new User("44444444"));
        userInCourse2.setId(new UserCourseKey("44444444",course.getId()));
        userInCourseList.add(userInCourse2);

        return  userInCourseList;
    }
}
