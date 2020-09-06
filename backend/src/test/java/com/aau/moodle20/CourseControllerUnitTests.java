package com.aau.moodle20;

import com.aau.moodle20.controller.CourseController;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"com.mypackage"})
@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerUnitTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CourseService courseService;

    private String adminMatriculationNumber ="00000000";


    @Test
    public void getCourse_unauthorized() throws Exception {
        this.mvc.perform(get("/api/course/200").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getCourse_authorized() throws Exception {

        String jwtToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTU5OTM5NjA1NCwiZXhwIjoxNTk5NDgyNDU0LCJpc0FkbWluIjp0cnVlLCJtYXRyaWN1bGF0aW9uTnVtYmVyIjoiMDAwMDAwMDAiLCJmb3JlbmFtZSI6ImFkbWluIiwic3VybmFtZSI6ImFkbWluIn0.McmCS6-05R6_1lsBZFXNBZI1fcPOFN_zbn5Q2siaSy-Z2Az3oT_0EbDzVICG6JKqMrZd3XNCV--xFyLv9E9lwA";
        CourseResponseObject responseObject = new CourseResponseObject();
        when(courseService.getCourse(200)).thenReturn(responseObject);
        this.mvc.perform(get("/api/course/200").header("Authorization",jwtToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }



    private void mockSecurityContext_WithUserDetails(UserDetailsImpl userDetails)
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    private UserDetailsImpl getUserDetails_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("admin","admin", Boolean.TRUE, adminMatriculationNumber,"admin","admin" );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("Admin"));
        userDetails.setAuthorities(authorities);
        return userDetails;
    }
}
