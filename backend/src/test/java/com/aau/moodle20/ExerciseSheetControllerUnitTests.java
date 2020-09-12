package com.aau.moodle20;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExerciseSheetKreuzelResponse;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.services.ExerciseSheetService;
import com.aau.moodle20.services.PdfService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class ExerciseSheetControllerUnitTests extends AbstractControllerTest {

    private Long exampleId = 200L;
    private Long exerciseSheetId = 100L;


    @MockBean
    private ExerciseSheetService exerciseSheetService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private ExampleRepository exampleRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private ExerciseSheetRepository exerciseSheetRepository;

    @Before
    public void mockExampleService_Methods() throws IOException, ClassNotFoundException {
        when(exerciseSheetService.getExerciseSheet(anyLong())).thenReturn(new ExerciseSheetResponseObject());
        when(exerciseSheetService.getExerciseSheetAssigned(anyLong())).thenReturn(new ExerciseSheetResponseObject());
        when(exerciseSheetService.getExerciseSheetKreuzel(anyLong())).thenReturn(new ExerciseSheetKreuzelResponse());
        when(exerciseSheetService.getExerciseSheetsFromCourse(anyLong())).thenReturn(new ArrayList<>());
        when(pdfService.generateCourseAttendanceList(anyLong())).thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(pdfService.generateKreuzelList(anyLong())).thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(pdfService.generateExerciseSheetDocument(anyLong())).thenReturn(new ByteArrayInputStream("Test".getBytes()));
        doNothing().when(exerciseSheetService).createExerciseSheet(any(CreateExerciseSheetRequest.class));
        doNothing().when(exerciseSheetService).updateExerciseSheet(any(UpdateExerciseSheetRequest.class));
        doNothing().when(exerciseSheetService).deleteExerciseSheet(anyLong());
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {

        //post /exerciseSheet   		create ExerciseSheet
        this.mvc.perform(put("/api/exerciseSheet").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}		getExerciseSheet
        this.mvc.perform(get("/api/exerciseSheet/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}/kreuzel	getExerciseSheetKreuzel
        this.mvc.perform(get("/api/exerciseSheet/100/kreuzel").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /exerciseSheetAssigned/100		getExerciseSheetAssigned
        this.mvc.perform(get("/api/exerciseSheetAssigned/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /exerciseSheet			updateExerciseSheet
        this.mvc.perform(post("/api/exerciseSheet").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        this.mvc.perform(get("/api/course/100/exerciseSheets").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        this.mvc.perform(get("/api/exerciseSheet/100/kreuzelList").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /exerciseSheetAssigned/100/pdf		getExerciseSheetDocument
        this.mvc.perform(get("/api/exerciseSheetAssigned/100/pdf").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //delete /exerciseSheet/{id} deleteExerciseSheet
        this.mvc.perform(delete("/api/exerciseSheet/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = prepareAdminUser();

        //get /api/exerciseSheetAssigned/100		getExerciseSheetAssigned
        perform_Get("/api/exerciseSheetAssigned/100",jwtToken).andExpect(status().isOk());
        //get /exerciseSheet/{id}		getExerciseSheet
        perform_Get("/api/exerciseSheet/100",jwtToken).andExpect(status().isOk());
        //get /exerciseSheet/100/kreuzel	getExerciseSheetKreuzel
        perform_Get("/api/exerciseSheet/100/kreuzel",jwtToken).andExpect(status().isOk());
        //put /api/exerciseSheet			createExerciseSheet
        perform_Put("/api/exerciseSheet",jwtToken,mapToJson(getCreateExerciseSheetRequest())).andExpect(status().isOk());
        //post /exerciseSheet			updateExercisesheet
        perform_Post("/api/exerciseSheet",jwtToken,mapToJson(createUpdateExerciseSheetRequest())).andExpect(status().isOk());
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        perform_Get("/api/course/100/exerciseSheets",jwtToken).andExpect(status().isOk());
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        perform_Get("/api/exerciseSheet/100/kreuzelList",jwtToken).andExpect(status().isOk());
        //get /exerciseSheetAssigned/100/pdf		getExerciseSheetDocument
        perform_Get("/api/exerciseSheetAssigned/100/pdf",jwtToken).andExpect(status().isOk());
        //delete /exerciseSheet/{id} deleteExerciseSheet
        perform_Delete("/api/exerciseSheet/100",jwtToken).andExpect(status().isOk());
    }



    @Test
    public void check_all_apis_unauthorized_invalid_JWTToken() throws Exception {
        String jwtToken = "prepareAdminUser()";

        //get /api/exerciseSheetAssigned/100		getExerciseSheetAssigned
        perform_Get("/api/exerciseSheetAssigned/100",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}		getExerciseSheet
        perform_Get("/api/exerciseSheet/100",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/100/kreuzel	getExerciseSheetKreuzel
        perform_Get("/api/exerciseSheet/100/kreuzel",jwtToken).andExpect(status().isUnauthorized());
        //put /api/exerciseSheet			createExerciseSheet
        perform_Put("/api/exerciseSheet",jwtToken,mapToJson(getCreateExerciseSheetRequest())).andExpect(status().isUnauthorized());
        //post /exerciseSheet			updateExercisesheet
        perform_Post("/api/exerciseSheet",jwtToken,mapToJson(createUpdateExerciseSheetRequest())).andExpect(status().isUnauthorized());
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        perform_Get("/api/course/100/exerciseSheets",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        perform_Get("/api/exerciseSheet/100/kreuzelList",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheetAssigned/100/pdf		getExerciseSheetDocument
        perform_Get("/api/exerciseSheetAssigned/100/pdf",jwtToken).andExpect(status().isUnauthorized());
        //delete /exerciseSheet/{id} deleteExerciseSheet
        perform_Delete("/api/exerciseSheet/100",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_JWTToken() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        //get /api/exerciseSheetAssigned/100		getExerciseSheetAssigned
        perform_Get("/api/exerciseSheetAssigned/100",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}		getExerciseSheet
        perform_Get("/api/exerciseSheet/100",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/100/kreuzel	getExerciseSheetKreuzel
        perform_Get("/api/exerciseSheet/100/kreuzel",jwtToken).andExpect(status().isUnauthorized());
        //put /api/exerciseSheet			createExerciseSheet
        perform_Put("/api/exerciseSheet",jwtToken,mapToJson(getCreateExerciseSheetRequest())).andExpect(status().isUnauthorized());
        //post /exerciseSheet			updateExercisesheet
        perform_Post("/api/exerciseSheet",jwtToken,mapToJson(createUpdateExerciseSheetRequest())).andExpect(status().isUnauthorized());
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        perform_Get("/api/course/100/exerciseSheets",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        perform_Get("/api/exerciseSheet/100/kreuzelList",jwtToken).andExpect(status().isUnauthorized());
        //get /exerciseSheetAssigned/100/pdf		getExerciseSheetDocument
        perform_Get("/api/exerciseSheetAssigned/100/pdf",jwtToken).andExpect(status().isUnauthorized());
        //delete /exerciseSheet/{id} deleteExerciseSheet
        perform_Delete("/api/exerciseSheet/100",jwtToken).andExpect(status().isUnauthorized());
    }



    @Test
    public void check_get_ExerciseSheet_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /exerciseSheet/{id}		getExerciseSheet
        perform_Get("/api/exerciseSheet/100",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExerciseSheetKreuzel_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /exerciseSheet/100/kreuzel	getExerciseSheetKreuzel
        perform_Get("/api/exerciseSheet/100/kreuzel",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExerciseSheetAssigned_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /api/exerciseSheetAssigned/100		getExerciseSheetAssigned
        perform_Get("/api/exerciseSheetAssigned/100",jwtToken).andExpect(status().isOk());
    }


    @Test
    public void check_put_ExerciseSheet_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //put /api/exerciseSheet			createExerciseSheet
        perform_Put("/api/exerciseSheet",jwtToken,mapToJson(getCreateExerciseSheetRequest())).andExpect(status().isOk());
    }

    @Test
    public void check_post_ExerciseSheet_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //post /exerciseSheet			updateExercisesheet
        perform_Post("/api/exerciseSheet",jwtToken,mapToJson(createUpdateExerciseSheetRequest())).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExerciseSheetsFromCourse_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        perform_Get("/api/course/200/exerciseSheets",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_KreuzelList_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        perform_Get("/api/exerciseSheet/100/kreuzelList",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExerciseSheetDocument_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /exerciseSheetAssigned/100/pdf		getExerciseSheetDocument
        perform_Get("/api/exerciseSheetAssigned/100/pdf",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_delete_ExerciseSheet_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //delete /exerciseSheet/{id} deleteExerciseSheet
        perform_Delete("/api/exerciseSheet/100",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExerciseSheet_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /exerciseSheet/{id}		getExerciseSheet
        perform_Get("/api/exerciseSheet/100",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_get_ExerciseSheetKreuzel_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /exerciseSheet/100/kreuzel	getExerciseSheetKreuzel
        perform_Get("/api/exerciseSheet/100/kreuzel",jwtToken).andExpect(status().isForbidden());
    }



    @Test
    public void check_put_ExerciseSheet_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //put /api/exerciseSheet			createExerciseSheet
        perform_Put("/api/exerciseSheet",jwtToken,mapToJson(getCreateExerciseSheetRequest())).andExpect(status().isForbidden());
    }

    @Test
    public void check_post_ExerciseSheet_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //post /exerciseSheet			updateExercisesheet
        perform_Post("/api/exerciseSheet",jwtToken,mapToJson(createUpdateExerciseSheetRequest())).andExpect(status().isForbidden());
    }

    @Test
    public void check_get_ExerciseSheetsFromCourse_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /course/{id}/exerciseSheets		getExerciseSheetsFromCourse
        perform_Get("/api/course/200/exerciseSheets",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_get_KreuzelList_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /exerciseSheet/{id}/kreuzelList	getKreuzelList
        perform_Get("/api/exerciseSheet/100/kreuzelList",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_delete_ExerciseSheet_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //delete /exerciseSheet/{id} deleteExerciseSheet
        perform_Delete("/api/exerciseSheet/100",jwtToken).andExpect(status().isForbidden());
    }


    @Test
    public void check_createExerciseSheet_CourseId_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();

        createExerciseSheetRequest.setCourseId(null);
        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("courseId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExerciseSheet_name_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();

        createExerciseSheetRequest.setName(null);
        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExerciseSheet_submissionDate_invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();

        createExerciseSheetRequest.setSubmissionDate(null);
        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("submissionDate: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        JsonNode actualObject  = objectMapper.readTree(mapToJson(getCreateExerciseSheetRequest()));
        ((ObjectNode)actualObject).put("submissionDate","dd");

        perform_Put("/api/exerciseSheet", jwtToken, actualObject.toString())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void check_createExerciseSheet_issueDate_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();

        createExerciseSheetRequest.setIssueDate(null);
        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("issueDate: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        JsonNode actualObject  = objectMapper.readTree(mapToJson(getCreateExerciseSheetRequest()));
        ((ObjectNode)actualObject).put("issueDate","dd");

        perform_Put("/api/exerciseSheet", jwtToken, actualObject.toString())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void check_createExerciseSheet_minKreuzel_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();
        createExerciseSheetRequest.setMinKreuzel(null);

        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minKreuzel: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExerciseSheetRequest.setMinKreuzel(-1);

        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minKreuzel: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExerciseSheet_minPoints_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExerciseSheetRequest createExerciseSheetRequest = getCreateExerciseSheetRequest();
        createExerciseSheetRequest.setMinPoints(null);

        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minPoints: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExerciseSheetRequest.setMinPoints(-1);

        perform_Put("/api/exerciseSheet", jwtToken, mapToJson(createExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minPoints: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }



    // update

    @Test
    public void check_updateExerciseSheet_Id_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();

        updateExerciseSheetRequest.setId(null);
        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("id: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExerciseSheet_name_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();

        updateExerciseSheetRequest.setName(null);
        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExerciseSheet_submissionDate_invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();

        updateExerciseSheetRequest.setSubmissionDate(null);
        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("submissionDate: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        JsonNode actualObject  = objectMapper.readTree(mapToJson(createUpdateExerciseSheetRequest()));
        ((ObjectNode)actualObject).put("submissionDate","dd");

        perform_Post("/api/exerciseSheet", jwtToken, actualObject.toString())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void check_updateExerciseSheet_issueDate_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();

        updateExerciseSheetRequest.setIssueDate(null);
        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("issueDate: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        JsonNode actualObject  = objectMapper.readTree(mapToJson(createUpdateExerciseSheetRequest()));
        ((ObjectNode)actualObject).put("issueDate","dd");

        perform_Post("/api/exerciseSheet", jwtToken, actualObject.toString())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void check_updateExerciseSheet_minKreuzel_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setMinKreuzel(null);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minKreuzel: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateExerciseSheetRequest.setMinKreuzel(-1);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minKreuzel: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExerciseSheet_minPoints_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setMinPoints(null);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minPoints: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateExerciseSheetRequest.setMinPoints(-1);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("minPoints: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExerciseSheet_includeThird_not_null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setIncludeThird(null);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("includeThird: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExerciseSheet_uploadCount_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExerciseSheetRequest updateExerciseSheetRequest = createUpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setUploadCount(null);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateExerciseSheetRequest.setUploadCount(-1);

        perform_Post("/api/exerciseSheet", jwtToken, mapToJson(updateExerciseSheetRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

//
//
//    @Test
//    public void check_createExample_ExerciseSheetId_Not_Null() throws Exception {
//        String jwtToken = prepareAdminUser();
//        CreateExampleRequest createExampleRequest = getCreateExerciseSheetRequest();
//        createExampleRequest.setExerciseSheetId(null);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("exerciseSheetId: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//    }
//
//    @Test
//    public void check_createExample_name_Not_Blank() throws Exception {
//        String jwtToken = prepareAdminUser();
//        CreateExampleRequest createExampleRequest = getCreateExerciseSheetRequest();
//        createExampleRequest.setName(null);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        createExampleRequest.setName("");
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//    @Test
//    public void check_createExample_points_invalid() throws Exception {
//        String jwtToken = prepareAdminUser();
//        CreateExampleRequest createExampleRequest = getCreateExerciseSheetRequest();
//        createExampleRequest.setPoints(null);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("points: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        createExampleRequest.setPoints(-1);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("points: must be greater than or equal to 0"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//    @Test
//    public void check_createExample_weighting_invalid() throws Exception {
//        String jwtToken = prepareAdminUser();
//        CreateExampleRequest createExampleRequest = getCreateExerciseSheetRequest();
//        createExampleRequest.setWeighting(null);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("weighting: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        createExampleRequest.setWeighting(-1);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("weighting: must be greater than or equal to 0"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//    @Test
//    public void check_createExample_order_invalid() throws Exception {
//        String jwtToken = prepareAdminUser();
//        CreateExampleRequest createExampleRequest = getCreateExerciseSheetRequest();
//        createExampleRequest.setOrder(null);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("order: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        createExampleRequest.setOrder(-1);
//
//        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("order: must be greater than or equal to 0"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//
//
//    @Test
//    public void check_updateExample_Invalid_UploadCount() throws Exception {
//        String jwtToken = prepareAdminUser();
//        UpdateExampleRequest updateExampleRequest = createUpdateExerciseSheetRequest();
//
//        updateExampleRequest.setUploadCount(-1);
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("uploadCount: must be greater than or equal to 0"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//
//    @Test
//    public void check_updateExample_ExerciseSheetId_Not_Null() throws Exception {
//        String jwtToken = prepareAdminUser();
//        UpdateExampleRequest updateExampleRequest = createUpdateExerciseSheetRequest();
//        updateExampleRequest.setExerciseSheetId(null);
//
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("exerciseSheetId: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//    }
//    @Test
//    public void check_updateExample_Id_Not_Null() throws Exception {
//        String jwtToken = prepareAdminUser();
//        UpdateExampleRequest updateExampleRequest = createUpdateExerciseSheetRequest();
//        updateExampleRequest.setId(null);
//
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("id: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//    }
//
//    @Test
//    public void check_updateExample_name_Not_Blank() throws Exception {
//        String jwtToken = prepareAdminUser();
//        UpdateExampleRequest updateExampleRequest = createUpdateExerciseSheetRequest();
//        updateExampleRequest.setName(null);
//
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        updateExampleRequest.setName("");
//
//        perform_Put("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }
//
//    @Test
//    public void check_updateExample_order_invalid() throws Exception {
//        String jwtToken = prepareAdminUser();
//        UpdateExampleRequest updateExampleRequest = createUpdateExerciseSheetRequest();
//        updateExampleRequest.setOrder(null);
//
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("order: must not be null"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//
//        updateExampleRequest.setOrder(-1);
//
//        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").value("order: must be greater than or equal to 0"))
//                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
//    }



    private String prepareForAuthorizedOwner() {
        User user1 = getUser1();
        Course course = getTestCourse(user1);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(course);
        Example example = getTestExample(exerciseSheet);

        String jwtToken = generateValidUserJWToken(user1);
        when(exampleRepository.findById(exampleId)).thenReturn(Optional.of(example));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(exerciseSheetRepository.findById(exerciseSheetId)).thenReturn(Optional.of(exerciseSheet));

        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        return jwtToken;
    }

    protected String prepareForUserWhoIsNoOwner() {
        User user1 = getUser1();
        User user2 = getUser2();
        Course course = getTestCourse(user1);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(course);
        Example example = getTestExample(exerciseSheet);

        String jwtToken = generateValidUserJWToken(user2);
        when(exampleRepository.findById(exampleId)).thenReturn(Optional.of(example));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(exerciseSheetRepository.findById(exerciseSheetId)).thenReturn(Optional.of(exerciseSheet));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        return jwtToken;
    }

    private List<ExampleOrderRequest> createUpdateExampleOrderList()
    {
        List<ExampleOrderRequest> exampleOrderRequests = new ArrayList<>();
        ExampleOrderRequest exampleOrderRequest = new ExampleOrderRequest();
        exampleOrderRequest.setOrder(1);
        exampleOrderRequest.setId(exampleId);
        exampleOrderRequests.add(exampleOrderRequest);
        exampleOrderRequest = new ExampleOrderRequest();
        exampleOrderRequest.setId(exampleId);
        exampleOrderRequest.setOrder(2);

        return exampleOrderRequests;
    }

    private CreateExerciseSheetRequest getCreateExerciseSheetRequest()
    {
        CreateExerciseSheetRequest createExerciseSheetRequest = new CreateExerciseSheetRequest();
        createExerciseSheetRequest.setCourseId(courseId);
        createExerciseSheetRequest.setDescription("ddd");
        createExerciseSheetRequest.setIncludeThird(Boolean.FALSE);
        createExerciseSheetRequest.setIssueDate(LocalDateTime.now());
        createExerciseSheetRequest.setSubmissionDate(LocalDateTime.now());
        createExerciseSheetRequest.setMinKreuzel(20);
        createExerciseSheetRequest.setMinPoints(20);
        createExerciseSheetRequest.setName("hhh");

        return createExerciseSheetRequest;
    }

    private UpdateExerciseSheetRequest createUpdateExerciseSheetRequest()
    {
        UpdateExerciseSheetRequest updateExerciseSheetRequest = new UpdateExerciseSheetRequest();
        updateExerciseSheetRequest.setName("ddd");
        updateExerciseSheetRequest.setId(exerciseSheetId);
        updateExerciseSheetRequest.setDescription("asdf");
        updateExerciseSheetRequest.setIncludeThird(Boolean.FALSE);
        updateExerciseSheetRequest.setMinKreuzel(1);
        updateExerciseSheetRequest.setMinPoints(10);
        updateExerciseSheetRequest.setIssueDate(LocalDateTime.now());
        updateExerciseSheetRequest.setUploadCount(20);
        updateExerciseSheetRequest.setSubmissionDate(LocalDateTime.now());

        return updateExerciseSheetRequest;
    }

    private ExerciseSheet getTestExerciseSheet(Course course)
    {
        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setId(exerciseSheetId);
        exerciseSheet.setCourse(course);

        return exerciseSheet;
    }

    private Example getTestExample(ExerciseSheet exerciseSheet)
    {
        Example example = new Example();
        example.setId(exampleId);
        example.setExerciseSheet(exerciseSheet);

        return example;
    }


    @Override
    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(obj);
    }

}
