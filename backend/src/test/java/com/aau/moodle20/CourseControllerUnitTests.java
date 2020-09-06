package com.aau.moodle20;

import com.aau.moodle20.controller.CourseController;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hibernate.sql.Update;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Value("${adminMatriculationNumber}")
    private String adminMatriculationNumber;

    @Value("${jwtSecret}")
    private String jwtSecret;

    @Value("${jwtExpirationMs}")
    private int jwtExpirationMs;

    @Before
    public void mockCourseService_Methods() throws IOException {
        when(courseService.getCourse(anyLong())).thenReturn(new CourseResponseObject());
        when(courseService.getCoursePresented(anyLong())).thenReturn(new ArrayList<>());
        when(courseService.createCourse(any( CreateCourseRequest.class))).thenReturn(new CourseResponseObject());
        when(courseService.updateCourse(any(UpdateCourseRequest.class))).thenReturn(new Course());
        doNothing().when(courseService).updateCoursePresets(any(UpdateCoursePresets.class));
        doNothing().when(courseService).deleteCourse(anyLong());
        when(courseService.copyCourse(new CopyCourseRequest())).thenReturn(new CourseResponseObject());
    }

    @Test
    public void check_All_Apis_unauthorized() throws Exception {
        // get Course
        this.mvc.perform(get("/api/course/200").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        // get course presented
        this.mvc.perform(get("/api/course/200/presented").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //create course
        this.mvc.perform(put("/api/course").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        // update course
        this.mvc.perform(post("/api/course").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //update course presets
        this.mvc.perform(post("/api/course/presets").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //delete course
        this.mvc.perform(delete("/api/course/200").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //copy course
        this.mvc.perform(post("/api/course/copy").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }


    @Test
    public void allApis_authorized_Admin() throws Exception {
        String jwtToken = generateValidAdminJWToken();

        // get Course
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        // get course presented
        this.mvc.perform(get("/api/course/200/presented").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        //create course
        this.mvc.perform(put("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCreateCourseRequest_Json())).andExpect(status().isOk());
        // update course
        this.mvc.perform(post("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCourseRequest_Json())).andExpect(status().isOk());
        //update course presets
        this.mvc.perform(post("/api/course/presets").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCoursePresets_Json())).andExpect(status().isOk());
        //delete course
        this.mvc.perform(delete("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        //copy course
        this.mvc.perform(post("/api/course/copy").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCopyCourseRequest_Json())).andExpect(status().isOk());
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

    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

    private String getCreateCourseRequest_Json() throws JsonProcessingException {
        CreateCourseRequest createCourseRequest = new CreateCourseRequest();
        createCourseRequest.setNumber("123.333");
        createCourseRequest.setDescription("");
        createCourseRequest.setMinKreuzel(20);
        createCourseRequest.setMinPoints(20);
        createCourseRequest.setOwner("2000000");
        createCourseRequest.setSemesterId(200L);
        createCourseRequest.setName("dd");
        return mapToJson(createCourseRequest);
    }

    private String getUpdateCourseRequest_Json() throws JsonProcessingException {
        UpdateCourseRequest updateCourseRequest = new UpdateCourseRequest();
        updateCourseRequest.setNumber("123.333");
        updateCourseRequest.setDescription("");
        updateCourseRequest.setMinKreuzel(20);
        updateCourseRequest.setMinPoints(20);
        updateCourseRequest.setOwner("2000000");
        updateCourseRequest.setName("dd");
        updateCourseRequest.setId(200L);
        return mapToJson(updateCourseRequest);
    }

    private String getCopyCourseRequest_Json() throws JsonProcessingException {
        CopyCourseRequest copyCourseRequest = new CopyCourseRequest();
        copyCourseRequest.setCourseId(200L);
        copyCourseRequest.setSemesterId(200L);

        return mapToJson(copyCourseRequest);
    }

    private String getUpdateCoursePresets_Json() throws JsonProcessingException {
        UpdateCoursePresets updateCoursePresets = new UpdateCoursePresets();
        updateCoursePresets.setDescription("");
        updateCoursePresets.setUploadCount(20);
        updateCoursePresets.setId(200L);

        return mapToJson(updateCoursePresets);
    }



    private String  generateValidAdminJWToken()
    {
        return "Bearer "+Jwts.builder()
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim(JwtUtils.CLAIM_IS_ADMIN,Boolean.TRUE)
                .claim(JwtUtils.CLAIM_MATRICULATION_NUMBER, adminMatriculationNumber)
                .claim(JwtUtils.CLAIM_FORENAME,"admin")
                .claim(JwtUtils.CLAIM_SURNAME,"admin")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


}
