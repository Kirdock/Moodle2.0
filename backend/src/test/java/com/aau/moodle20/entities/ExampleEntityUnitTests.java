package com.aau.moodle20.entities;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ExampleEntityUnitTests {

    private final Long EXERCISE_SHEET_ID = 500L;
    private final Long EXAMPLE_ID = 340L;


    @Test
    public void getCustomFileTypesList()  {
        Example example = new Example();
        example.setCustomFileTypes("ddd,fff");

        List<String> fileTypes = new ArrayList<>();
        fileTypes.add("ddd");
        fileTypes.add("fff");
        assertEquals(fileTypes,example.getCustomFileTypesList());
    }

    @Test
    public void setCustomFileTypesList()  {
        List<String> fileTypes = new ArrayList<>();
        fileTypes.add("ddd");
        fileTypes.add("fff");
        Example example = new Example();
        example.setCustomFileTypesList(fileTypes);
        assertEquals("ddd,fff",example.getCustomFileTypes());
    }

    @Test
    public void fillValuesFromRequestObject_Update()  {
        UpdateExampleRequest request = getUpdateExampleRequest();
        Example example = new Example();
        example.fillValuesFromRequestObject(request);

        Example exampleFromUpdateRequest = getExampleFromUpdateRequest(request);
        assertEquals(exampleFromUpdateRequest,example);
    }

    @Test
    public void fillValuesFromRequestObject_Create()  {
        CreateExampleRequest request = getCreateExampleRequest();
        Example example = new Example();
        example.fillValuesFromRequestObject(request);

        Example exampleFromUpdateRequest = getExampleFromCreateRequest(request);
        assertEquals(exampleFromUpdateRequest,example);
    }

    @Test
    public void copy()  {
        Example example = getTestExample(EXAMPLE_ID);
        Example copiedExample = example.copy();

        assertEquals(example.getSubmitFile(),copiedExample.getSubmitFile());
        assertEquals(example.getWeighting(),copiedExample.getWeighting());
        assertEquals(example.getPoints(),copiedExample.getPoints());
        assertEquals(example.getOrder(),copiedExample.getOrder());
        assertEquals(example.getCustomFileTypes(),copiedExample.getCustomFileTypes());
        assertEquals(example.getMandatory(),copiedExample.getMandatory());
        assertEquals(example.getName(),copiedExample.getName());
        assertEquals(example.getDescription(),copiedExample.getDescription());
        assertEquals(example.getUploadCount(),copiedExample.getUploadCount());
    }


    @Test
    public void createExampleResponseObject() {
        Example example = getTestExample(EXAMPLE_ID);
        ExampleResponseObject responseObject = example.createExampleResponseObject("12345678");
        example.setExamplesFinishedByUser(new HashSet<>());

        compareExampleWithResponseObject(example, responseObject);
        assertEquals(EFinishesExampleState.NO,responseObject.getState());
    }

    @Test
    public void createExampleResponseObject_check_finishExample() {
        Example example = getTestExample(EXAMPLE_ID);
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setUser(new User("12345678"));
        finishesExample.setState(EFinishesExampleState.MAYBE);
        finishesExample.setDescription("dd");
        finishesExample.setRemainingUploadCount(3);
        finishesExample.setViolationHistories(new HashSet<>());
        example.setExamplesFinishedByUser(new HashSet<>());
        example.getExamplesFinishedByUser().add(finishesExample);


        ExampleResponseObject responseObject = example.createExampleResponseObject("12345678");
        assertEquals(EFinishesExampleState.MAYBE,responseObject.getState());
        assertEquals(finishesExample.getDescription(),responseObject.getSubmitDescription());
        assertEquals(Boolean.FALSE,responseObject.getHasAttachment());
        assertEquals(finishesExample.getRemainingUploadCount(),responseObject.getRemainingUploadCount());
        assertEquals(new ArrayList<>(),responseObject.getResult());
    }


    private void compareExampleWithResponseObject(Example example, ExampleResponseObject responseObject)
    {
        assertEquals(example.getId(),responseObject.getId());
        if (example.getParentExample() != null)
            assertEquals(example.getParentExample().getId(), responseObject.getParentId());
        assertEquals(example.getValidator(),responseObject.getValidator());
        assertEquals(example.getSubmitFile(),responseObject.getSubmitFile());
        assertEquals(example.getWeighting(),responseObject.getWeighting());
        assertEquals(example.getPoints(),responseObject.getPoints());
        assertEquals(example.getOrder(),responseObject.getOrder());
        assertEquals(example.getCustomFileTypesList(),responseObject.getCustomFileTypes());
        assertEquals(example.getMandatory(),responseObject.getMandatory());
        assertEquals(example.getName(),responseObject.getName());
        assertEquals(example.getDescription(),responseObject.getDescription());
        assertEquals(example.getUploadCount(),responseObject.getUploadCount());
        assertEquals(example.getExerciseSheet().getId(),responseObject.getExerciseSheetId());

        if(example.getParentExample()!=null) {
            List<Long> fileTypeIds = new ArrayList<>();
            fileTypeIds.add(30L);
            assertEquals(fileTypeIds, responseObject.getSupportedFileTypes());
        }

        if(example.getSubExamples()!=null && !example.getSubExamples().isEmpty()) {
            Example subExample = example.getSubExamples().iterator().next();
            ExampleResponseObject subExampleResponseObject = responseObject.getSubExamples().get(0);
            compareExampleWithResponseObject(subExample,subExampleResponseObject);
        }



    }

    protected Example getTestExample(Long exampleId) {
        Example example = new Example(exampleId);
        example.setValidator("test.jar");
        example.setPoints(30);
        example.setSubmitFile(Boolean.TRUE);
        example.setSupportFileTypes(new HashSet<>());
        example.setCustomFileTypes("test.java,hhh");
        example.setUploadCount(2);
        example.setWeighting(10);
        example.setDescription("dd");
        example.setOrder(2);
        example.setCustomFileTypes("asdf.java");
        example.setMandatory(Boolean.FALSE);
        example.setName("name");
        example.setExamplesFinishedByUser(new HashSet<>());
        example.setParentExample(new Example(500L));
        example.setSubExamples(new HashSet<>());
        example.getSubExamples().add(getTestSubExample(EXAMPLE_ID+10));
        example.setSupportFileTypes(new HashSet<>());

        SupportFileType fileType = new SupportFileType();
        FileType fileType1 = new FileType();
        fileType1.setId(30L);
        fileType.setFileType(fileType1);
        example.getSupportFileTypes().add(fileType);
        example.setExerciseSheet(new ExerciseSheet(EXERCISE_SHEET_ID));

        return example;
    }


    protected Example getTestSubExample(Long exampleId) {
        Example example = new Example(exampleId);
        example.setValidator("test.jar");
        example.setPoints(30);
        example.setSubmitFile(Boolean.TRUE);
        example.setSupportFileTypes(new HashSet<>());
        example.setCustomFileTypes("test.java,hhh");
        example.setUploadCount(2);
        example.setWeighting(10);
        example.setDescription("dd");
        example.setOrder(2);
        example.setCustomFileTypes("asdf.java");
        example.setMandatory(Boolean.FALSE);
        example.setName("name");
        example.setExamplesFinishedByUser(new HashSet<>());
        example.setSubExamples(new HashSet<>());
        example.setSupportFileTypes(new HashSet<>());
        example.setExerciseSheet(new ExerciseSheet(EXERCISE_SHEET_ID));

        return example;
    }

    private UpdateExampleRequest getUpdateExampleRequest() {
        UpdateExampleRequest updateExampleRequest = new UpdateExampleRequest();
        updateExampleRequest.setName("ddd");
        updateExampleRequest.setExerciseSheetId(EXERCISE_SHEET_ID);
        updateExampleRequest.setCustomFileTypes(new ArrayList<>());
        updateExampleRequest.setDescription("asdf");
        updateExampleRequest.setMandatory(Boolean.FALSE);
        updateExampleRequest.setOrder(1);
        updateExampleRequest.setPoints(10);
        updateExampleRequest.setWeighting(20);
        updateExampleRequest.setUploadCount(20);
        updateExampleRequest.setSupportedFileTypes(new ArrayList<>());
        updateExampleRequest.setSubmitFile(Boolean.FALSE);
        updateExampleRequest.setId(EXAMPLE_ID);

        return updateExampleRequest;
    }

    private Example getExampleFromUpdateRequest(UpdateExampleRequest request) {
        Example example = new Example();
        example.setId(request.getId());
        example.setName(request.getName());
        example.setDescription(request.getDescription());
        example.setOrder(request.getOrder());
        example.setPoints(request.getPoints());
        example.setWeighting(request.getWeighting());
        example.setUploadCount(request.getUploadCount());
        example.setSubmitFile(request.getSubmitFile());
        example.setMandatory(request.getMandatory());
        if (request.getParentId() != null)
            example.setParentExample(new Example(request.getParentId()));
        if (request.getExerciseSheetId() != null)
            example.setExerciseSheet(new ExerciseSheet(request.getExerciseSheetId()));

        return example;
    }

    private Example getExampleFromCreateRequest(CreateExampleRequest request) {
        Example example = new Example();
        example.setName(request.getName());
        example.setDescription(request.getDescription());
        example.setOrder(request.getOrder());
        example.setPoints(request.getPoints());
        example.setWeighting(request.getWeighting());
        example.setUploadCount(request.getUploadCount());
        example.setSubmitFile(request.getSubmitFile());
        example.setMandatory(request.getMandatory());
        if (request.getParentId() != null)
            example.setParentExample(new Example(request.getParentId()));
        if (request.getExerciseSheetId() != null)
            example.setExerciseSheet(new ExerciseSheet(request.getExerciseSheetId()));

        return example;
    }

    private CreateExampleRequest getCreateExampleRequest() {
        CreateExampleRequest createExampleRequest = new CreateExampleRequest();
        createExampleRequest.setName("ddd");
        createExampleRequest.setExerciseSheetId(EXERCISE_SHEET_ID);
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
}
