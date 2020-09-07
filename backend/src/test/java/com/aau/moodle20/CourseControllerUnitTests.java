package com.aau.moodle20;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerUnitTests extends AbstractControllerTest{

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;




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
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {
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
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = generateValidAdminJWToken();
        User adminUser = getAdminUser();
        when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));


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

    @Test
    public void check_all_apis_unauthorized_invalid_jwt_token() throws Exception {
        String jwtToken = "test123";

        // get Course
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        // get course presented
        this.mvc.perform(get("/api/course/200/presented").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //create course
        this.mvc.perform(put("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCreateCourseRequest_Json())).andExpect(status().isUnauthorized());
        // update course
        this.mvc.perform(post("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCourseRequest_Json())).andExpect(status().isUnauthorized());
        //update course presets
        this.mvc.perform(post("/api/course/presets").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCoursePresets_Json())).andExpect(status().isUnauthorized());
        //delete course
        this.mvc.perform(delete("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //copy course
        this.mvc.perform(post("/api/course/copy").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCopyCourseRequest_Json())).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_jwt_token() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        // get Course
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        // get course presented
        this.mvc.perform(get("/api/course/200/presented").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //create course
        this.mvc.perform(put("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCreateCourseRequest_Json())).andExpect(status().isUnauthorized());
        // update course
        this.mvc.perform(post("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCourseRequest_Json())).andExpect(status().isUnauthorized());
        //update course presets
        this.mvc.perform(post("/api/course/presets").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCoursePresets_Json())).andExpect(status().isUnauthorized());
        //delete course
        this.mvc.perform(delete("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //copy course
        this.mvc.perform(post("/api/course/copy").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getCopyCourseRequest_Json())).andExpect(status().isUnauthorized());
    }


    @Test
    public void getCourse_authorized_owner() throws Exception {
        User user1 = getUser1();
        Course course = getTestCourse(user1);
        String jwtToken = generateValidUserJWToken(user1);
        when(courseRepository.findById(200L)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));

        // get Course
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    }

    @Test
    public void getCourse_unauthorized_not_owner() throws Exception {
        User user1 = getUser1();
        User user2 = getUser2();
        Course course = getTestCourse(user1);
        String jwtToken = generateValidUserJWToken(user2);
        when(courseRepository.findById(200L)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));

        // get Course
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

    }


    private Course getTestCourse(User user)
    {
        Course course = new Course();
        course.setId((long) 200);
        course.setDescription("dd");
        course.setDescriptionTemplate("dd");
        course.setId((long) 200);
        course.setMinKreuzel(20);
        course.setMinPoints(20);
        course.setName("dd");
        course.setNumber("123.456");
        course.setOwner(user);
        course.setSemester(new Semester(200L));
        course.setExerciseSheets(new HashSet<>());
        return course;
    }


    private User getUser1()
    {
        User user = new User();
        user.setForename("user1_forename");
        user.setSurname("user1_surname");
        user.setUsername("user1");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("12345678");

        return user;
    }

    private User getUser2()
    {
        User user = new User();
        user.setForename("user2_forename");
        user.setSurname("user2_surname");
        user.setUsername("user2");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("87654321");

        return user;
    }

    private User getAdminUser()
    {
        User user = new User();
        user.setForename("admin");
        user.setSurname("admin");
        user.setUsername("admin");
        user.setAdmin(Boolean.TRUE);
        user.setMatriculationNumber(adminMatriculationNumber);

        return user;
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
}
