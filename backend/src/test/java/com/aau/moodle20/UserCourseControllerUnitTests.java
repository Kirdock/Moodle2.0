package com.aau.moodle20;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.services.PdfService;
import com.aau.moodle20.services.UserCourseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserCourseControllerUnitTests extends AbstractControllerTest {
    @MockBean
    private UserCourseService userCourseService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private CourseRepository courseRepository;

    @Before
    public void mockExampleService_Methods() throws IOException {
        when(userCourseService.getCourseAssigned(anyLong())).thenReturn(new CourseResponseObject());
        when(userCourseService.assignFile(any(MultipartFile.class),anyLong())).thenReturn(new RegisterMultipleUserResponse());
        doNothing().when(userCourseService).assignUsers(anyList());
        when(pdfService.generateCourseAttendanceList(anyLong())).thenReturn(new ByteArrayInputStream("Test".getBytes()));
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {
        //get /courseAssigned/{courseId}   		isCourseAssigned
        this.mvc.perform(get("/api/courseAssigned/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /course/100/attendanceList		getAttendanceList
        this.mvc.perform(get("/api/course/100/attendanceList").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /course/assign	assignCourseToSemester
        this.mvc.perform(post("/api/course/assign").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /course/assignFile		assignFile
        this.mvc.perform(post("/api/course/assignFile").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = prepareAdminUser();

        //get /courseAssigned/{courseId}   		isCourseAssigned
        perform_Get("/api/courseAssigned/100",jwtToken).andExpect(status().isOk());
        //get /course/100/attendanceList		getAttendanceList
        perform_Get("/api/course/100/attendanceList",jwtToken).andExpect(status().isOk());
        //get /course/assign	assignCourseToSemester
        perform_Post("/api/course/assign",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isOk());
        //get /course/assignFile		assignFile
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/course/assignFile")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isOk());
    }



    @Test
    public void check_all_apis_unauthorized_invalid_JWTToken() throws Exception {
        String jwtToken = "prepareAdminUser()";

        ///get /courseAssigned/{courseId}   		isCourseAssigned
        perform_Get("/api/courseAssigned/100",jwtToken).andExpect(status().isUnauthorized());
        //get /course/100/attendanceList		getAttendanceList
        perform_Get("/api/course/100/attendanceList",jwtToken).andExpect(status().isUnauthorized());
        //get /course/assign	assignCourseToSemester
        perform_Post("/api/course/assign",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //get /course/assignFile		assignFile
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/course/assignFile")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_JWTToken() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        //get /courseAssigned/{courseId}   		isCourseAssigned
        perform_Get("/api/courseAssigned/100",jwtToken).andExpect(status().isUnauthorized());
        //get /course/100/attendanceList		getAttendanceList
        perform_Get("/api/course/100/attendanceList",jwtToken).andExpect(status().isUnauthorized());
        //post /course/assign	assignCourseToSemester
        perform_Post("/api/course/assign",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //post /course/assignFile		assignFile
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/course/assignFile")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());
    }



    @Test
    public void check_getAttendanceList_not_owner_forbidden() throws Exception {
        String jwtToken = prepareForUserWhoIsNoOwner();
        //get /course/200/attendanceList		getAttendanceList
        perform_Get("/api/course/200/attendanceList",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_getAttendanceList_owner_ok() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //get /course/200/attendanceList		getAttendanceList
        perform_Get("/api/course/200/attendanceList",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_assignFile_not_owner_forbidden() throws Exception {
        String jwtToken = prepareForUserWhoIsNoOwner();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/course/assignFile")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isForbidden());
    }
    @Test
    public void check_assignFile_owner_ok() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/course/assignFile")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isOk());
    }
    protected String prepareForUserWhoIsNoOwner() {
        User user1 = getUser1();
        User user2 = getUser2();
        Course course = getTestCourse(user1);

        String jwtToken = generateValidUserJWToken(user2);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
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
