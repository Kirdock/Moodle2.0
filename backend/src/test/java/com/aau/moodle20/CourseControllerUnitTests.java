package com.aau.moodle20;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerUnitTests extends AbstractControllerTest {

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
        when(courseService.createCourse(any(CreateCourseRequest.class))).thenReturn(new CourseResponseObject());
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
        String jwtToken = prepareAdminUser();

        // get Course
        perform_Get("/api/course/200", jwtToken).andExpect(status().isOk());
        // get course presented
        perform_Get("/api/course/200/presented", jwtToken).andExpect(status().isOk());
        //create course
        perform_Put("/api/course", jwtToken, getCreateCourseRequest_Json()).andExpect(status().isOk());
        // update course
        perform_Post("/api/course", jwtToken, getUpdateCourseRequest_Json()).andExpect(status().isOk());
        //update course presets
        perform_Post("/api/course/presets", jwtToken, getUpdateCoursePresets_Json()).andExpect(status().isOk());
        //delete course
        perform_Delete("/api/course/200", jwtToken).andExpect(status().isOk());
        //copy course
        perform_Post("/api/course/copy", jwtToken, getCopyCourseRequest_Json()).andExpect(status().isOk());
    }

    @Test
    public void check_all_apis_unauthorized_invalid_jwt_token() throws Exception {
        String jwtToken = "test123";

        // get Course
        perform_Get("/api/course/200", jwtToken).andExpect(status().isUnauthorized());
        // get course presented
        perform_Get("/api/course/200/presented", jwtToken).andExpect(status().isUnauthorized());
        //create course
        perform_Put("/api/course", jwtToken, getCreateCourseRequest_Json()).andExpect(status().isUnauthorized());
        // update course
        perform_Post("/api/course", jwtToken, getUpdateCourseRequest_Json()).andExpect(status().isUnauthorized());
        //update course presets
        perform_Post("/api/course/presets", jwtToken, getUpdateCoursePresets_Json()).andExpect(status().isUnauthorized());
        //delete course
        perform_Delete("/api/course/200", jwtToken).andExpect(status().isUnauthorized());
        //copy course
        perform_Post("/api/course/200", jwtToken, getCopyCourseRequest_Json()).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_jwt_token() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        // get Course
        perform_Get("/api/course/200", jwtToken).andExpect(status().isUnauthorized());
        // get course presented
        perform_Get("/api/course/200/presented", jwtToken).andExpect(status().isUnauthorized());
        //create course
        perform_Put("/api/course", jwtToken, getCreateCourseRequest_Json()).andExpect(status().isUnauthorized());
        // update course
        perform_Post("/api/course", jwtToken, getUpdateCourseRequest_Json()).andExpect(status().isUnauthorized());
        //update course presets
        perform_Post("/api/course/presets", jwtToken, getUpdateCoursePresets_Json()).andExpect(status().isUnauthorized());
        //delete course
        perform_Delete("/api/course/200", jwtToken).andExpect(status().isUnauthorized());
        //copy course
        perform_Post("/api/course/200", jwtToken, getCopyCourseRequest_Json()).andExpect(status().isUnauthorized());
    }


    @Test
    public void getCourse_authorized_owner() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void getCourse_unauthorized_not_owner() throws Exception {
        String jwtToken = prepareForUnAuthorizedOwner();
        this.mvc.perform(get("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    public void getCoursePresented_authorized_owner() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        this.mvc.perform(get("/api/course/200/presented").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void getCoursePresented_unauthorized_not_owner() throws Exception {
        String jwtToken = prepareForUnAuthorizedOwner();
        perform_Get("/api/course/200/presented", jwtToken).andExpect(status().isForbidden());
    }


    // create course
    @Test
    public void createCourse_forbidden_not_admin() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        perform_Put("/api/course", jwtToken, getCreateCourseRequest_Json()).andExpect(status().isForbidden());
    }

    @Test
    public void createCourse_invalid_Course_Number() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateCourseRequest createCourseRequest = createCreateCourseRequest();
        createCourseRequest.setNumber("12345679");

        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("number: must match \"^[0-9]{3}\\.[0-9]{3}$\""))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createCourseRequest.setNumber(null);
        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("number: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createCourseRequest.setNumber("");
        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(Matchers.containsInAnyOrder("number: must match \"^[0-9]{3}\\.[0-9]{3}$\"", "number: must not be blank")))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void createCourse_SemesterId_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateCourseRequest createCourseRequest = createCreateCourseRequest();
        createCourseRequest.setSemesterId(null);

        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("semesterId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void createCourse_Name_Not_Blank() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateCourseRequest createCourseRequest = createCreateCourseRequest();

        createCourseRequest.setName(null);
        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createCourseRequest.setName("");
        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void createCourse_Owner_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateCourseRequest createCourseRequest = createCreateCourseRequest();

        createCourseRequest.setOwner(null);
        perform_Put("/api/course", jwtToken, mapToJson(createCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("owner: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }


    // create Course end

    // update course
    @Test
    public void updateCourse_authorized_owner() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        // update course
        this.mvc.perform(post("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCourseRequest_Json())).andExpect(status().isOk());
    }

    @Test
    public void updateCourse_unauthorized_not_owner() throws Exception {
        String jwtToken = prepareForUnAuthorizedOwner();
        // update course
        this.mvc.perform(post("/api/course").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCourseRequest_Json())).andExpect(status().isForbidden());
    }

    @Test
    public void updateCourse_Owner_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateCourseRequest updateCourseRequest = createUpdateCourseRequest();
        updateCourseRequest.setId(null);

        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("id: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void updateCourse_invalid_Course_Number() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateCourseRequest updateCourseRequest = createUpdateCourseRequest();
        updateCourseRequest.setNumber("123");

        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("number: must match \"^[0-9]{3}\\.[0-9]{3}$\""))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateCourseRequest.setNumber(null);
        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("number: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateCourseRequest.setNumber("");
        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(Matchers.containsInAnyOrder("number: must match \"^[0-9]{3}\\.[0-9]{3}$\"", "number: must not be blank")))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void updateCourse_Name_Not_Blank() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateCourseRequest updateCourseRequest = createUpdateCourseRequest();
        updateCourseRequest.setName(null);

        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateCourseRequest.setName("");
        perform_Post("/api/course", jwtToken, mapToJson(updateCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    // update course end

    @Test
    public void updateCoursePresets_authorized_owner() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //update course presets
        this.mvc.perform(post("/api/course/presets").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCoursePresets_Json())).andExpect(status().isOk());
    }

    @Test
    public void updateCoursePresets_unauthorized_not_owner() throws Exception {
        String jwtToken = prepareForUnAuthorizedOwner();
        //update course presets
        this.mvc.perform(post("/api/course/presets").header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(getUpdateCoursePresets_Json())).andExpect(status().isForbidden());
    }

    @Test
    public void updateCoursePresets_Id_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateCoursePresets updateCoursePresets = createUpdateCoursePresets();
        updateCoursePresets.setId(null);

        perform_Post("/api/course/presets", jwtToken, mapToJson(updateCoursePresets))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("id: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void updateCoursePresets_Invalid_UploadCount() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateCoursePresets updateCoursePresets = createUpdateCoursePresets();
        updateCoursePresets.setUploadCount(null);

        perform_Post("/api/course/presets", jwtToken, mapToJson(updateCoursePresets))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateCoursePresets.setUploadCount(-1);
        perform_Post("/api/course/presets", jwtToken, mapToJson(updateCoursePresets))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: Upload count must be greater or equal 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void deleteCourse_unauthorized_not_admin() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //delete course
        this.mvc.perform(delete("/api/course/200").header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    public void copyCourse_unauthorized_not_admin() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //copy course
        perform_Post("/api/course/copy",jwtToken,getCopyCourseRequest_Json()).andExpect(status().isForbidden());
    }

    @Test
    public void copyCoursePresets_Invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        CopyCourseRequest copyCourseRequest = createCopyCourseRequest();
        copyCourseRequest.setSemesterId(null);

        perform_Post("/api/course/copy", jwtToken, mapToJson(copyCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("semesterId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        copyCourseRequest.setSemesterId(200L);
        copyCourseRequest.setCourseId(null);
        perform_Post("/api/course/copy", jwtToken, mapToJson(copyCourseRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("courseId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }


    private String prepareForAuthorizedOwner() {
        User user1 = getUser1();
        Course course = getTestCourse(user1);
        String jwtToken = generateValidUserJWToken(user1);
        when(courseRepository.findById(200L)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        return jwtToken;
    }

    private String prepareForUnAuthorizedOwner() {
        User user1 = getUser1();
        User user2 = getUser2();
        Course course = getTestCourse(user1);
        String jwtToken = generateValidUserJWToken(user2);
        when(courseRepository.findById(200L)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        return jwtToken;
    }

    private String prepareAdminUser() {
        String jwtToken = generateValidAdminJWToken();
        User adminUser = getAdminUser();
        when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));

        return jwtToken;
    }


    private Course getTestCourse(User user) {
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


    private User getUser1() {
        User user = new User();
        user.setForename("user1_forename");
        user.setSurname("user1_surname");
        user.setUsername("user1");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("12345678");

        return user;
    }

    private User getUser2() {
        User user = new User();
        user.setForename("user2_forename");
        user.setSurname("user2_surname");
        user.setUsername("user2");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("87654321");

        return user;
    }

    private User getAdminUser() {
        User user = new User();
        user.setForename("admin");
        user.setSurname("admin");
        user.setUsername("admin");
        user.setAdmin(Boolean.TRUE);
        user.setMatriculationNumber(adminMatriculationNumber);

        return user;
    }

    private String getCreateCourseRequest_Json() throws JsonProcessingException {
        return mapToJson(createCreateCourseRequest());
    }

    private CreateCourseRequest createCreateCourseRequest() {
        CreateCourseRequest createCourseRequest = new CreateCourseRequest();
        createCourseRequest.setNumber("123.333");
        createCourseRequest.setDescription("");
        createCourseRequest.setMinKreuzel(20);
        createCourseRequest.setMinPoints(20);
        createCourseRequest.setOwner("2000000");
        createCourseRequest.setSemesterId(200L);
        createCourseRequest.setName("dd");

        return createCourseRequest;
    }

    private UpdateCourseRequest createUpdateCourseRequest() {
        UpdateCourseRequest updateCourseRequest = new UpdateCourseRequest();
        updateCourseRequest.setNumber("123.333");
        updateCourseRequest.setDescription("");
        updateCourseRequest.setMinKreuzel(20);
        updateCourseRequest.setMinPoints(20);
        updateCourseRequest.setOwner("2000000");
        updateCourseRequest.setName("dd");
        updateCourseRequest.setId(200L);

        return updateCourseRequest;
    }

    private String getUpdateCourseRequest_Json() throws JsonProcessingException {
        return mapToJson(createUpdateCourseRequest());
    }

    private String getCopyCourseRequest_Json() throws JsonProcessingException {
        return mapToJson(createCopyCourseRequest());
    }

    private CopyCourseRequest createCopyCourseRequest()
    {
        CopyCourseRequest copyCourseRequest = new CopyCourseRequest();
        copyCourseRequest.setCourseId(200L);
        copyCourseRequest.setSemesterId(200L);

        return copyCourseRequest;
    }

    private UpdateCoursePresets createUpdateCoursePresets() {
        UpdateCoursePresets updateCoursePresets = new UpdateCoursePresets();
        updateCoursePresets.setDescription("");
        updateCoursePresets.setUploadCount(20);
        updateCoursePresets.setId(200L);
        return updateCoursePresets;
    }

    private String getUpdateCoursePresets_Json() throws JsonProcessingException {
        return mapToJson(createUpdateCoursePresets());
    }
}
