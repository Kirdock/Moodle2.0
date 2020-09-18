package com.aau.moodle20.controller;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.services.ExampleService;
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

import java.io.IOException;
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
public class ExampleControllerUnitTests  extends AbstractControllerTest {

    private final Long exampleId = 200L;
    private final Long exerciseSheetId = 100L;


    @MockBean
    private ExampleService exampleService;

    @MockBean
    private ExampleRepository exampleRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private ExerciseSheetRepository exerciseSheetRepository;

    @Before
    public void mockExampleService_Methods() throws IOException, ClassNotFoundException {
        when(exampleService.getExample(anyLong())).thenReturn(new ExampleResponseObject());
        when(exampleService.getFileTypes()).thenReturn(new ArrayList<>());
        when(exampleService.getExampleValidator(anyLong())).thenReturn(getExampleValidator());
        when(exampleService.createExample(any(CreateExampleRequest.class))).thenReturn(new ExampleResponseObject());
        doNothing().when(exampleService).updateExample(any(UpdateExampleRequest.class));
        doNothing().when(exampleService).updateExampleOrder(new ArrayList<>());
        doNothing().when(exampleService).setExampleValidator(any(MultipartFile.class),anyLong());
        doNothing().when(exampleService).deleteExample(anyLong());
        doNothing().when(exampleService).deleteExampleValidator(anyLong());
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {

        //get /fileTypes   		getFileTypes
        this.mvc.perform(get("/api/fileTypes").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /example/{id}		getExample
        this.mvc.perform(get("/api/example/200").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /example/{id}/validator	getExampleValidator
        this.mvc.perform(get("/api/example/200/validator").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //put /example			createExample
        this.mvc.perform(put("/api/example").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /example			updateExample
        this.mvc.perform(post("/api/example").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /example/order		updateExampleOrder
        this.mvc.perform(post("/api/example/order").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /example/validator	setExampleValidator
        this.mvc.perform(post("/api/example/validator").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //delete /example/{id}		deleteExample
        this.mvc.perform(delete("/api/example/200").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //delete /example/{id}/validator delete ExampleValidator
        this.mvc.perform(delete("/api/example/200/validator").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_Admin() throws Exception {
        String jwtToken = prepareAdminUser();

        perform_Get("/api/fileTypes",jwtToken).andExpect(status().isOk());
        //get /example/{id}		getExample
        perform_Get("/api/example/200",jwtToken).andExpect(status().isOk());
        //get /example/{id}/validator	getExampleValidator
        perform_Get("/api/example/200/validator",jwtToken).andExpect(status().isOk());
        //put /example			createExample
        perform_Put("/api/example",jwtToken,mapToJson(getCreateExampleRequest())).andExpect(status().isOk());
        //post /example			updateExample
        perform_Post("/api/example",jwtToken,mapToJson(createUpdateExampleRequest())).andExpect(status().isOk());
        //post /example/order		updateExampleOrder
        perform_Post("/api/examples/order",jwtToken,mapToJson(createUpdateExampleOrderList())).andExpect(status().isOk());
        //post /example/validator	setExampleValidator

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/example/validator")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isOk());

        //delete /example/{id}		deleteExample
        perform_Delete("/api/example/200",jwtToken).andExpect(status().isOk());
        //delete /example/{id}/validator delete ExampleValidator
        perform_Delete("/api/example/200/validator",jwtToken).andExpect(status().isOk());
    }



    @Test
    public void check_all_apis_unauthorized_invalid_JWTToken() throws Exception {
        String jwtToken = "prepareAdminUser()";

        perform_Get("/api/fileTypes",jwtToken).andExpect(status().isUnauthorized());
        //get /example/{id}		getExample
        perform_Get("/api/example/200",jwtToken).andExpect(status().isUnauthorized());
        //get /example/{id}/validator	getExampleValidator
        perform_Get("/api/example/200/validator",jwtToken).andExpect(status().isUnauthorized());
        //put /example			createExample
        perform_Put("/api/example",jwtToken,mapToJson(getCreateExampleRequest())).andExpect(status().isUnauthorized());
        //post /example			updateExample
        perform_Post("/api/example",jwtToken,mapToJson(createUpdateExampleRequest())).andExpect(status().isUnauthorized());
        //post /example/order		updateExampleOrder
        perform_Post("/api/examples/order",jwtToken,mapToJson(createUpdateExampleOrderList())).andExpect(status().isUnauthorized());
        //post /example/validator	setExampleValidator

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/example/validator")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());

        //delete /example/{id}		deleteExample
        perform_Delete("/api/example/200",jwtToken).andExpect(status().isUnauthorized());
        //delete /example/{id}/validator delete ExampleValidator
        perform_Delete("/api/example/200/validator",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_JWTToken() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        perform_Get("/api/fileTypes",jwtToken).andExpect(status().isUnauthorized());
        //get /example/{id}		getExample
        perform_Get("/api/example/200",jwtToken).andExpect(status().isUnauthorized());
        //get /example/{id}/validator	getExampleValidator
        perform_Get("/api/example/200/validator",jwtToken).andExpect(status().isUnauthorized());
        //put /example			createExample
        perform_Put("/api/example",jwtToken,mapToJson(getCreateExampleRequest())).andExpect(status().isUnauthorized());
        //post /example			updateExample
        perform_Post("/api/example",jwtToken,mapToJson(createUpdateExampleRequest())).andExpect(status().isUnauthorized());
        //post /example/order		updateExampleOrder
        perform_Post("/api/examples/order",jwtToken,mapToJson(createUpdateExampleOrderList())).andExpect(status().isUnauthorized());
        //post /example/validator	setExampleValidator

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/example/validator")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());

        //delete /example/{id}		deleteExample
        perform_Delete("/api/example/200",jwtToken).andExpect(status().isUnauthorized());
        //delete /example/{id}/validator delete ExampleValidator
        perform_Delete("/api/example/200/validator",jwtToken).andExpect(status().isUnauthorized());
    }


    @Test
    public void check_get_FileTypes_owner_and_not_owner_authorized() throws Exception
    {
        User user1 = getUser1();
        User user2 = getUser2();
        Course course = getTestCourse(user1);
        String jwtToken_user1 = generateValidUserJWToken(user1);
        String jwtToken_user2 = generateValidUserJWToken(user2);
        when(courseRepository.findById(200L)).thenReturn(Optional.of(course));
        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));

        // user 1 -> owner of course
        perform_Get("/api/fileTypes",jwtToken_user1).andExpect(status().isOk());

        // user 2 -> not owner of course
        perform_Get("/api/fileTypes",jwtToken_user2).andExpect(status().isOk());
    }

    @Test
    public void check_get_Example_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /example/{id}		getExample
        perform_Get("/api/example/200",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_ExampleValidator_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //get /example/{id}/validator	getExampleValidator
        perform_Get("/api/example/200/validator",jwtToken).andExpect(status().isOk());
    }


    @Test
    public void check_put_Example_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //put /example			createExample
        perform_Put("/api/example",jwtToken,mapToJson(getCreateExampleRequest())).andExpect(status().isOk());
    }


    @Test
    public void check_post_Example_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //post /example			updateExample
        perform_Post("/api/example",jwtToken,mapToJson(createUpdateExampleRequest())).andExpect(status().isOk());
    }

    @Test
    public void check_post_ExampleOrder_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //post /example/order		updateExampleOrder
        perform_Post("/api/examples/order",jwtToken,mapToJson(createUpdateExampleOrderList())).andExpect(status().isOk());
    }

    @Test
    public void check_post_ExampleValidator_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //post /example/validator	setExampleValidator

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/example/validator")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isOk());
    }

    @Test
    public void check_delete_Example_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //delete /example/{id}		deleteExample
        perform_Delete("/api/example/200",jwtToken).andExpect(status().isOk());
    }


    @Test
    public void check_delete_ExampleValidator_owner_authorized() throws Exception
    {
        String jwtToken  = prepareForAuthorizedOwner();
        //delete /example/{id}/validator delete ExampleValidator
        perform_Delete("/api/example/200/validator",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_get_Example_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /example/{id}		getExample
        perform_Get("/api/example/200",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_get_ExampleValidator_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //get /example/{id}/validator	getExampleValidator
        perform_Get("/api/example/200/validator",jwtToken).andExpect(status().isForbidden());
    }


    @Test
    public void check_put_Example_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //put /example			createExample
        perform_Put("/api/example",jwtToken,mapToJson(getCreateExampleRequest())).andExpect(status().isForbidden());
    }


    @Test
    public void check_post_Example_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //post /example			updateExample
        perform_Post("/api/example",jwtToken,mapToJson(createUpdateExampleRequest())).andExpect(status().isForbidden());
    }

    @Test
    public void check_post_ExampleOrder_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //post /example/order		updateExampleOrder
        perform_Post("/api/examples/order",jwtToken,mapToJson(createUpdateExampleOrderList())).andExpect(status().isForbidden());
    }

    @Test
    public void check_post_ExampleValidator_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //post /example/validator	setExampleValidator

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/example/validator")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isForbidden());
    }

    @Test
    public void check_delete_Example_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //delete /example/{id}		deleteExample
        perform_Delete("/api/example/200",jwtToken).andExpect(status().isForbidden());
    }


    @Test
    public void check_delete_ExampleValidator_not_owner_unauthorized() throws Exception
    {
        String jwtToken  = prepareForUserWhoIsNoOwner();
        //delete /example/{id}/validator delete ExampleValidator
        perform_Delete("/api/example/200/validator",jwtToken).andExpect(status().isForbidden());
    }


    @Test
    public void check_createExample_Invalid_UploadCount() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();

        createExampleRequest.setUploadCount(-1);
        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }


    @Test
    public void check_createExample_ExerciseSheetId_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();
        createExampleRequest.setExerciseSheetId(null);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("exerciseSheetId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

    }

    @Test
    public void check_createExample_name_Not_Blank() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();
        createExampleRequest.setName(null);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExampleRequest.setName("");

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExample_points_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();
        createExampleRequest.setPoints(null);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("points: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExampleRequest.setPoints(-1);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("points: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExample_weighting_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();
        createExampleRequest.setWeighting(null);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("weighting: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExampleRequest.setWeighting(-1);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("weighting: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_createExample_order_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        CreateExampleRequest createExampleRequest = getCreateExampleRequest();
        createExampleRequest.setOrder(null);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("order: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        createExampleRequest.setOrder(-1);

        perform_Put("/api/example", jwtToken, mapToJson(createExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("order: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }



    @Test
    public void check_updateExample_Invalid_UploadCount() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExampleRequest updateExampleRequest = createUpdateExampleRequest();

        updateExampleRequest.setUploadCount(-1);
        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("uploadCount: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }


    @Test
    public void check_updateExample_ExerciseSheetId_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExampleRequest updateExampleRequest = createUpdateExampleRequest();
        updateExampleRequest.setExerciseSheetId(null);

        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("exerciseSheetId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

    }
    @Test
    public void check_updateExample_Id_Not_Null() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExampleRequest updateExampleRequest = createUpdateExampleRequest();
        updateExampleRequest.setId(null);

        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("id: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

    }

    @Test
    public void check_updateExample_name_Not_Blank() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExampleRequest updateExampleRequest = createUpdateExampleRequest();
        updateExampleRequest.setName(null);

        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateExampleRequest.setName("");

        perform_Put("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("name: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_updateExample_order_invalid() throws Exception {
        String jwtToken = prepareAdminUser();
        UpdateExampleRequest updateExampleRequest = createUpdateExampleRequest();
        updateExampleRequest.setOrder(null);

        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("order: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        updateExampleRequest.setOrder(-1);

        perform_Post("/api/example", jwtToken, mapToJson(updateExampleRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("order: must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }



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

    private CreateExampleRequest getCreateExampleRequest()
    {
        CreateExampleRequest createExampleRequest = new CreateExampleRequest();
        createExampleRequest.setName("ddd");
        createExampleRequest.setExerciseSheetId(exerciseSheetId);
        createExampleRequest.setCustomFileTypes(new ArrayList<>());
        createExampleRequest.setDescription("asdf");
        createExampleRequest.setMandatory(Boolean.FALSE);
        createExampleRequest.setOrder(1);
        createExampleRequest.setPoints(10);
        createExampleRequest.setWeighting(20);
        createExampleRequest.setUploadCount(20);
        createExampleRequest.setSupportedFileTypes(new ArrayList<>());
        createExampleRequest.setSubmitFile(Boolean.FALSE);

        return createExampleRequest;
    }

    private UpdateExampleRequest createUpdateExampleRequest()
    {
        UpdateExampleRequest updateExampleRequest = new UpdateExampleRequest();
        updateExampleRequest.setName("ddd");
        updateExampleRequest.setExerciseSheetId(100L);
        updateExampleRequest.setCustomFileTypes(new ArrayList<>());
        updateExampleRequest.setDescription("asdf");
        updateExampleRequest.setMandatory(Boolean.FALSE);
        updateExampleRequest.setOrder(1);
        updateExampleRequest.setPoints(10);
        updateExampleRequest.setWeighting(20);
        updateExampleRequest.setUploadCount(20);
        updateExampleRequest.setSupportedFileTypes(new ArrayList<>());
        updateExampleRequest.setSubmitFile(Boolean.FALSE);
        updateExampleRequest.setId(exampleId);

        return updateExampleRequest;
    }

    private Example getExampleValidator()
    {
        Example example = new Example();
        example.setValidator("test.txt");
        example.setValidatorContent("test".getBytes());
        return example;
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

}
