package com.aau.moodle20.controller;

import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.services.AbstractServiceTest;
import com.aau.moodle20.services.PdfService;
import com.aau.moodle20.services.UserCourseService;
import com.aau.moodle20.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
    private JwtUtils jwtUtils;

    @Before
    public void mockUserService_Methods() {
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        when(userService.getUsersWithCourseRoles(anyLong())).thenReturn(new ArrayList<>());
        when(userService.isOwner()).thenReturn(Boolean.TRUE);
        when(userService.getUser(anyString())).thenReturn(new UserResponseObject());
        doNothing().when(userService).checkForTemporaryPassword(any(LoginRequest.class));
        Authentication authentication = mock(Authentication.class);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(getLoginRequest().getUsername(), getLoginRequest().getPassword());
        when(authenticationManager.authenticate(token)).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwtToken");
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
        //post /login		login
        this.mvc.perform(post("/api/login").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
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

    private LoginRequest getLoginRequest()
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("admin");
        loginRequest.setUsername("admin");
        return loginRequest;
    }
}
