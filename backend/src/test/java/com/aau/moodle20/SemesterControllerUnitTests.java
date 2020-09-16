package com.aau.moodle20;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ESemesterType;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExerciseSheetKreuzelResponse;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.services.ExerciseSheetService;
import com.aau.moodle20.services.PdfService;
import com.aau.moodle20.services.SemesterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SemesterControllerUnitTests extends AbstractControllerTest {
    @MockBean
    private SemesterService semesterService;

    @MockBean
    private SemesterRepository semesterRepository;

    @Before
    public void mockExampleService_Methods() throws IOException, ClassNotFoundException {
        when(semesterService.getSemesters()).thenReturn(new ArrayList<>());
        when(semesterService.getSemestersAssigned()).thenReturn(new ArrayList<>());
        when(semesterService.getAssignedCoursesFromSemester(anyLong())).thenReturn(new ArrayList<>());
        when(semesterService.getCoursesFromSemester(anyLong())).thenReturn(new ArrayList<>());
        doNothing().when(semesterService).createSemester(any(CreateSemesterRequest.class));
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {
        //post /semester   		create semester
        this.mvc.perform(put("/api/semester").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /semesters		getSemesters
        this.mvc.perform(get("/api/semesters").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /semesters/assigned	getSemestersAssigned
        this.mvc.perform(get("/api/semesters/assigned").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /semester/{semesterId}/courses		getCoursesFromSemester
        this.mvc.perform(get("/api/semester/10/courses").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /semester/{semesterId}/coursesAssigned			getAssignedCoursesFromSemester
        this.mvc.perform(get("/api/semester/10/coursesAssigned").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = prepareAdminUser();

        //post /semester   		create semester
        perform_Put("/api/semester",jwtToken,mapToJson(getCreateSemesterRequest())).andExpect(status().isOk());
        //get /semesters		getSemesters
        perform_Get("/api/semesters",jwtToken).andExpect(status().isOk());
        //get /semesters/assigned	getSemestersAssigned
        perform_Get("/api/semesters/assigned",jwtToken).andExpect(status().isOk());
        //get /semester/{semesterId}/courses		getCoursesFromSemester
        perform_Get("/api/semester/10/courses",jwtToken).andExpect(status().isOk());
        //post /semester/{semesterId}/coursesAssigned			getAssignedCoursesFromSemester
        perform_Get("/api/semester/10/coursesAssigned",jwtToken).andExpect(status().isOk());
    }



    @Test
    public void check_all_apis_unauthorized_invalid_JWTToken() throws Exception {
        String jwtToken = "prepareAdminUser()";

        //post /semester   		create semester
        perform_Put("/api/semester",jwtToken,mapToJson(getCreateSemesterRequest())).andExpect(status().isUnauthorized());
        //get /semesters		getSemesters
        perform_Get("/api/semesters",jwtToken).andExpect(status().isUnauthorized());
        //get /semesters/assigned	getSemestersAssigned
        perform_Get("/api/semesters/assigned",jwtToken).andExpect(status().isUnauthorized());
        //get /semester/{semesterId}/courses		getCoursesFromSemester
        perform_Get("/api/semester/10/courses",jwtToken).andExpect(status().isUnauthorized());
        //post /semester/{semesterId}/coursesAssigned			getAssignedCoursesFromSemester
        perform_Get("/api/semester/10/coursesAssigned",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_JWTToken() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        //post /semester   		create semester
        perform_Put("/api/semester",jwtToken,mapToJson(getCreateSemesterRequest())).andExpect(status().isUnauthorized());
        //get /semesters		getSemesters
        perform_Get("/api/semesters",jwtToken).andExpect(status().isUnauthorized());
        //get /semesters/assigned	getSemestersAssigned
        perform_Get("/api/semesters/assigned",jwtToken).andExpect(status().isUnauthorized());
        //get /semester/{semesterId}/courses		getCoursesFromSemester
        perform_Get("/api/semester/10/courses",jwtToken).andExpect(status().isUnauthorized());
        //post /semester/{semesterId}/coursesAssigned			getAssignedCoursesFromSemester
        perform_Get("/api/semester/10/coursesAssigned",jwtToken).andExpect(status().isUnauthorized());
    }



    @Test
    public void check_createSemester_not_admin_unauthroized() throws Exception {
        String jwtToken = prepareNonAdminUser();
        CreateSemesterRequest createSemesterRequest = getCreateSemesterRequest();

        perform_Put("/api/semester", jwtToken, mapToJson(createSemesterRequest))
                .andExpect(status().isForbidden());
    }


    @Test
    public void check_createSemester_year_invalid_parameter() throws Exception {
        String jwtToken = prepareNonAdminUser();
        CreateSemesterRequest createSemesterRequest = getCreateSemesterRequest();

        createSemesterRequest.setYear(null);
        perform_Put("/api/semester", jwtToken, mapToJson(createSemesterRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("year: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
        createSemesterRequest.setYear(-1);

        perform_Put("/api/semester", jwtToken, mapToJson(createSemesterRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("year: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }


    @Test
    public void check_createSemester_Type_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateSemesterRequest createSemesterRequest = getCreateSemesterRequest();
        createSemesterRequest.setType(null);

        perform_Put("/api/semester", jwtToken, mapToJson(createSemesterRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("type: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createSemesterRequest.setType(ESemesterType.S);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualObject  = objectMapper.readTree(mapToJson(createSemesterRequest));
        ((ObjectNode)actualObject).put("type","dd");

        perform_Put("/api/semester", jwtToken, mapToJson(actualObject.toString()))
                .andExpect(status().isBadRequest());
    }

    private CreateSemesterRequest getCreateSemesterRequest()
    {
        CreateSemesterRequest createSemesterRequest = new CreateSemesterRequest();
        createSemesterRequest.setType(ESemesterType.S);
        createSemesterRequest.setYear(2020);

        return createSemesterRequest;
    }

    protected String prepareNonAdminUser() {
        User user1 = getUser1();
        String jwtToken = generateValidUserJWToken(user1);
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));

        return jwtToken;
    }

}
