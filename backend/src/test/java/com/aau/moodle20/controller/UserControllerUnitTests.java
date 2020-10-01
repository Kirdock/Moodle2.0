package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.services.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerUnitTests extends AbstractControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CourseRepository courseRepository;





    @Before
    public void mockUserService_Methods() {
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        when(userService.getUsersWithCourseRoles(anyLong())).thenReturn(new ArrayList<>());
        when(userService.isOwner()).thenReturn(Boolean.TRUE);
        when(userService.getUser(anyString())).thenReturn(new UserResponseObject());
        doNothing().when(userService).checkForTemporaryPassword(any(LoginRequest.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(getUserDetails_Admin());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(getLoginRequest().getUsername(), getLoginRequest().getPassword());
        when(authenticationManager.authenticate(token)).thenReturn(authentication);
//        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwtToken");
        doNothing().when(userService).changePassword(any(ChangePasswordRequest.class));
        doNothing().when(userService).updateUser(any(UpdateUserRequest.class));
        doNothing().when(userService).registerUser(any(SignUpRequest.class));
        when(userService.registerUsers(any(MultipartFile.class),any(Boolean.class))).thenReturn(new RegisterMultipleUserResponse());
        doNothing().when(userService).deleteUser(anyString());
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {
        //get users
        this.mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /users/course/100		getUsersWithCourseRoles
        this.mvc.perform(get("/api/users/course/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /user/isOwner  isOwner
        this.mvc.perform(get("/api/user/isOwner").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /user/00000000		getUser
        this.mvc.perform(get("/api/user/00000000").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /user/password		changePassword
        this.mvc.perform(post("/api/user/password").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /user	updateUser
        this.mvc.perform(post("/api/user").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //put /user	registerUser
        this.mvc.perform(put("/api/user").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //put /user	registerUsers
        this.mvc.perform(put("/api/users").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //delete /user/0000000	registerUsers
        this.mvc.perform(delete("/api/user/0000000").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = prepareAdminUser();

        //get users
        perform_Get("/api/users",jwtToken).andExpect(status().isOk());
        //get /users/course/100		getUsersWithCourseRoles
        perform_Get("/api/users/course/100",jwtToken).andExpect(status().isOk());
        //get /user/isOwner  isOwner
        perform_Get("/api/user/isOwner",jwtToken).andExpect(status().isOk());
        //get /user/00000000		getUser
        perform_Get("/api/user/00000000",jwtToken).andExpect(status().isOk());
        //post /login		login
        perform_Post("/api/login",jwtToken,mapToJson(getLoginRequest())).andExpect(status().isOk());
        //post /user/password		changePassword
        perform_Post("/api/user/password",jwtToken,mapToJson(getChangePasswordRequest())).andExpect(status().isOk());
        //post /user	updateUser
        perform_Post("/api/user",jwtToken,mapToJson(getUpdateUserRequest())).andExpect(status().isOk());
        //put /user	registerUser
        perform_Put("/api/user",jwtToken,mapToJson(getSignUpRequest())).andExpect(status().isOk());
        //put /user	register Multiple Users

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/users");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        this.mvc.perform(builder
                .file(file)
                .param("isAdmin","false")
                .header("Authorization",jwtToken)).andExpect(status().isOk());
        //delete /user/0000000	delete User
        perform_Delete("/api/user/0000000",jwtToken).andExpect(status().isOk());
    }


    @Test
    public void check_all_apis_unauthorized_invalid_jwtToke() throws Exception {
        String jwtToken = "prepareAdminUser();";

        //get users
        perform_Get("/api/users",jwtToken).andExpect(status().isUnauthorized());
        //get /users/course/100		getUsersWithCourseRoles
        perform_Get("/api/users/course/100",jwtToken).andExpect(status().isUnauthorized());
        //get /user/isOwner  isOwner
        perform_Get("/api/user/isOwner",jwtToken).andExpect(status().isUnauthorized());
        //get /user/00000000		getUser
        perform_Get("/api/user/00000000",jwtToken).andExpect(status().isUnauthorized());
        //post /user/password		changePassword
        perform_Post("/api/user/password",jwtToken,mapToJson(getChangePasswordRequest())).andExpect(status().isUnauthorized());
        //post /user	updateUser
        perform_Post("/api/user",jwtToken,mapToJson(getUpdateUserRequest())).andExpect(status().isUnauthorized());
        //put /user	registerUser
        perform_Put("/api/user",jwtToken,mapToJson(getSignUpRequest())).andExpect(status().isUnauthorized());
        //put /user	register Multiple Users

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/users");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        this.mvc.perform(builder
                .file(file)
                .param("isAdmin","false")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());
        //delete /user/0000000	delete User
        perform_Delete("/api/user/0000000",jwtToken).andExpect(status().isUnauthorized());
    }


    @Test
    public void check_all_apis_unauthorized_expired_jwtToke() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        //get users
        perform_Get("/api/users",jwtToken).andExpect(status().isUnauthorized());
        //get /users/course/100		getUsersWithCourseRoles
        perform_Get("/api/users/course/100",jwtToken).andExpect(status().isUnauthorized());
        //get /user/isOwner  isOwner
        perform_Get("/api/user/isOwner",jwtToken).andExpect(status().isUnauthorized());
        //get /user/00000000		getUser
        perform_Get("/api/user/00000000",jwtToken).andExpect(status().isUnauthorized());
        //post /user/password		changePassword
        perform_Post("/api/user/password",jwtToken,mapToJson(getChangePasswordRequest())).andExpect(status().isUnauthorized());
        //post /user	updateUser
        perform_Post("/api/user",jwtToken,mapToJson(getUpdateUserRequest())).andExpect(status().isUnauthorized());
        //put /user	registerUser
        perform_Put("/api/user",jwtToken,mapToJson(getSignUpRequest())).andExpect(status().isUnauthorized());
        //put /user	register Multiple Users

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/users");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        this.mvc.perform(builder
                .file(file)
                .param("isAdmin","false")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());
        //delete /user/0000000	delete User
        perform_Delete("/api/user/0000000",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void getUsers_not_admin_unauthroized() throws Exception {
        String jwtToken = prepareNonAdminUser();
        //get users
        perform_Get("/api/users",jwtToken).andExpect(status().isForbidden());
    }


    @Test
    public void getUsersWithCourseRoles_owner_authorized() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //get /users/course/100		getUsersWithCourseRoles
        perform_Get("/api/users/course/"+courseId,jwtToken).andExpect(status().isOk());
    }

    @Test
    public void getUsersWithCourseRoles_notOwner_nor_Admin_forbidden() throws Exception {
        String jwtToken = prepareForUserWhoIsNotOwner();
        //get /users/course/100		getUsersWithCourseRoles
        perform_Get("/api/users/course/"+courseId,jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void getUser_not_admin_not_myself_forbidden() throws Exception {
        String jwtToken = prepareNonAdminUser();
        //get /user/00000000		getUser
        perform_Get("/api/user/12345555",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void getUser_not_admin_myself() throws Exception {
        String jwtToken = prepareNonAdminUser();
        //get /user/00000000		getUser
        perform_Get("/api/user/12345678",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void updateUser_not_admin_myself() throws Exception {
        String jwtToken = prepareNonAdminUser();
        UpdateUserRequest request = getUpdateUserRequest();
        request.setMatriculationNumber(getUser1().getMatriculationNumber());
        //post /user	updateUser
        perform_Post("/api/user",jwtToken,mapToJson(request)).andExpect(status().isOk());
    }

    @Test
    public void updateUser_not_admin_not_myself() throws Exception {
        String jwtToken = prepareNonAdminUser();
        UpdateUserRequest request = getUpdateUserRequest();
        request.setMatriculationNumber(getUser2().getMatriculationNumber());
        //post /user	updateUser
        perform_Post("/api/user",jwtToken,mapToJson(request)).andExpect(status().isForbidden());
    }

    @Test
    public void registerUser_not_admin_forbidden() throws Exception {
        String jwtToken = prepareNonAdminUser();
        String jwtToken_owner= prepareForAuthorizedOwner();
        perform_Put("/api/user",jwtToken,mapToJson(getSignUpRequest())).andExpect(status().isForbidden());
        perform_Put("/api/user",jwtToken_owner,mapToJson(getSignUpRequest())).andExpect(status().isForbidden());
    }

    @Test
    public void registerUsers_not_admin_forbidden() throws Exception {
        String jwtToken = prepareNonAdminUser();
        String jwtToken_owner= prepareForAuthorizedOwner();
        //put /user	register Multiple Users

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/users");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        this.mvc.perform(builder
                .file(file)
                .param("isAdmin","false")
                .header("Authorization",jwtToken)).andExpect(status().isForbidden());
        this.mvc.perform(builder
                .file(file)
                .param("isAdmin","false")
                .header("Authorization",jwtToken_owner)).andExpect(status().isForbidden());
    }

    @Test
    public void deleteUser_not_admin_forbidden() throws Exception {
        String jwtToken = prepareNonAdminUser();
        String jwtToken_owner= prepareForAuthorizedOwner();
        //delete /user/0000000	delete User
        perform_Delete("/api/user/0000000",jwtToken).andExpect(status().isForbidden());
        //delete /user/0000000	delete User
        perform_Delete("/api/user/0000000",jwtToken_owner).andExpect(status().isForbidden());
    }

    private LoginRequest getLoginRequest()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("admin");
        loginRequest.setUsername("admin");
        return loginRequest;
    }

    private ChangePasswordRequest getChangePasswordRequest()
    {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("oldPassword");
        changePasswordRequest.setNewPassword("oldPassword");
        return changePasswordRequest;
    }

    private UpdateUserRequest getUpdateUserRequest() {
      UpdateUserRequest updateUserRequest = new UpdateUserRequest();

      updateUserRequest.setMatriculationNumber(getAdminUser().getMatriculationNumber());
      updateUserRequest.setEmail("ppe@edu.aau.at");
      updateUserRequest.setForename("admin");
      updateUserRequest.setSurname("admin");
      return updateUserRequest;
    }

    private SignUpRequest getSignUpRequest()
    {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setMatriculationNumber(getAdminUser().getMatriculationNumber());
        signUpRequest.setForename("admin");
        signUpRequest.setSurname("admin");
        signUpRequest.setEmail("pepipp@edu.aau.at");
        signUpRequest.setUsername("admin");
        return signUpRequest;
    }

    protected UserDetailsImpl getUserDetails_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("admin","admin", Boolean.TRUE, adminMatriculationNumber,"admin","admin" );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("Admin"));
        userDetails.setAuthorities(authorities);
        return userDetails;
    }


    protected String prepareNonAdminUser() {
        User user1 = getUser1();
        String jwtToken = generateValidUserJWToken(user1);
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));


        return jwtToken;
    }

    private String prepareForUserWhoIsNotOwner() {
        User user1 = getUser1();
        Course course = getTestCourse(getUser2());

        String jwtToken = generateValidUserJWToken(user1);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        return jwtToken;
    }
    private String prepareForAuthorizedOwner() {
        User user1 = getUser1();
        Course course = getTestCourse(user1);

        String jwtToken = generateValidUserJWToken(user1);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        return jwtToken;
    }
}
