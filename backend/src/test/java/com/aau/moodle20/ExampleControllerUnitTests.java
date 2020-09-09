package com.aau.moodle20;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.services.ExampleService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExampleControllerUnitTests  extends AbstractControllerTest {

    @MockBean
    private ExampleService exampleService;

    @MockBean
    private ExampleRepository exampleRepository;

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


    private List<ExampleOrderRequest> createUpdateExampleOrderList()
    {
        List<ExampleOrderRequest> exampleOrderRequests = new ArrayList<>();
        ExampleOrderRequest exampleOrderRequest = new ExampleOrderRequest();
        exampleOrderRequest.setOrder(1);
        exampleOrderRequest.setId(200L);
        exampleOrderRequests.add(exampleOrderRequest);
        exampleOrderRequest = new ExampleOrderRequest();
        exampleOrderRequest.setId(200L);
        exampleOrderRequest.setOrder(2);

        return exampleOrderRequests;
    }

    private CreateExampleRequest getCreateExampleRequest()
    {
        CreateExampleRequest createExampleRequest = new CreateExampleRequest();
        createExampleRequest.setName("ddd");
        createExampleRequest.setExerciseSheetId(100L);
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
        updateExampleRequest.setId(100L);

        return updateExampleRequest;
    }

    private Example getExampleValidator()
    {
        Example example = new Example();
        example.setValidator("test.txt");
        example.setValidatorContent("test".getBytes());
        return example;
    }

}
