package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ESemesterType;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ExampleServiceUnitTest extends AbstractServiceTest{

    @InjectMocks
    private ExampleService exampleService;
    @Mock
    private FileTypeRepository fileTypeRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private ExampleRepository exampleRepository;
    @Mock
    private SupportFileTypeRepository supportFileTypeRepository;
    @Mock
    private FinishesExampleRepository finishesExampleRepository;

    private final Long FILE_TYPE_ID = 455L;
    private final Long PARENT_EXAMPLE_ID = 545L;

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void createExample_submitFile_noFileType()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.TRUE);
        request.setSupportedFileTypes(new ArrayList<>());
        request.setCustomFileTypes(new ArrayList<>());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        String expectedMessage = "Error:either supported file types or custom file types  must be specified!";
        assertEquals(expectedMessage,exception.getMessage());

        request.setSupportedFileTypes(null);
        request.setCustomFileTypes(null);

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());
        request.setSupportedFileTypes(new ArrayList<>());
        request.setCustomFileTypes(null);

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());

        request.setSupportedFileTypes(null);
        request.setCustomFileTypes(new ArrayList<>());

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void createExample_parent_Example_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setParentId(300L);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }
    @Test
    public void createExample_exercise_sheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void createExample_name_already_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        Example example = new Example(EXAMPLE_ID+30);
        example.setName(request.getName());
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().add(example);
        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        String expectedMessage = "Error: example with this name already exists in exerciseSheet";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS, exception.getErrorResponseCode());
    }

    @Test
    public void createSubExample_name_already_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setParentId(EXAMPLE_ID+10);
        ExerciseSheet exerciseSheet = getTestExerciseSheet();

        Example example = new Example(EXAMPLE_ID+30);
        example.setName(request.getName());

        Example parentExample = new Example(request.getParentId());
        parentExample.setSubExamples(new HashSet<>());
        parentExample.getSubExamples().add(example);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(exampleRepository.findById(request.getParentId())).thenReturn(Optional.of(parentExample));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.createExample(request);
        });
        String expectedMessage = "Error: sub example with this name already exists in exerciseSheet";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.SUB_EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS, exception.getErrorResponseCode());
    }

    @Test
    public void createExample_not_as_SubExample() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setSupportedFileTypes(new ArrayList<>());
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        exerciseSheet.setExamples(new HashSet<>());
        request.setSupportedFileTypes(new ArrayList<>());
        request.getSupportedFileTypes().add(FILE_TYPE_ID);

        Example example = getExampleFromCreateRequest(request);
        example.setId(400L);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(fileTypeRepository.findById(FILE_TYPE_ID)).thenReturn(Optional.of(getFileType()));
        when(exampleRepository.save(getExampleFromCreateRequest(request))).thenReturn(example);

        List<SupportFileType> supportFileTypes = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),getExampleFromCreateRequest(request)));

        ExampleResponseObject expected_responseObject = new ExampleResponseObject();
        expected_responseObject.setId(400L);

        ExampleResponseObject responseObject = exampleService.createExample(request);
        verify(exampleRepository).save(getExampleFromCreateRequest(request));
        verify(supportFileTypeRepository).saveAll(supportFileTypes);
        assertEquals(expected_responseObject.getId(),responseObject.getId());
    }

    @Test
    public void createExample_as_SubExample() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CreateExampleRequest request = getCreateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setSupportedFileTypes(new ArrayList<>());
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        exerciseSheet.setExamples(new HashSet<>());
        request.setSupportedFileTypes(new ArrayList<>());
        request.getSupportedFileTypes().add(FILE_TYPE_ID);
        request.setParentId(PARENT_EXAMPLE_ID);
        Example parentExample = getParentExample();

        Example example = getExampleFromCreateRequest(request);
        example.setId(400L);

        List<FinishesExample> finishesExampleList = getFinisExamples(parentExample);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(exampleRepository.findById(PARENT_EXAMPLE_ID)).thenReturn(Optional.of(parentExample));
        when(fileTypeRepository.findById(FILE_TYPE_ID)).thenReturn(Optional.of(getFileType()));
        when(exampleRepository.save(getExampleFromCreateRequest(request))).thenReturn(example);
        when(finishesExampleRepository.findByExample_Id(PARENT_EXAMPLE_ID)).thenReturn(finishesExampleList);


        List<SupportFileType> supportFileTypes = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),getExampleFromCreateRequest(request)));

        List<SupportFileType> supportFileTypes_Parent = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),parentExample));

        ExampleResponseObject expected_responseObject = new ExampleResponseObject();
        expected_responseObject.setId(400L);

        ExampleResponseObject responseObject = exampleService.createExample(request);

        verify(supportFileTypeRepository).flush();
        verify(supportFileTypeRepository).deleteAll(supportFileTypes_Parent);
        verify(exampleRepository).save(getExampleFromCreateRequest(request));
        verify(exampleRepository).save(getParentExample_after_SubExample_created());
        verify(finishesExampleRepository).deleteAll(finishesExampleList);
        verify(supportFileTypeRepository).saveAll(supportFileTypes);
        assertEquals(expected_responseObject.getId(),responseObject.getId());
    }


    @Test
    public void updateExample_submitFile_noFileType()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.TRUE);
        request.setSupportedFileTypes(new ArrayList<>());
        request.setCustomFileTypes(new ArrayList<>());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        String expectedMessage = "Error:either supported file types or custom file types  must be specified!";
        assertEquals(expectedMessage,exception.getMessage());

        request.setSupportedFileTypes(null);
        request.setCustomFileTypes(null);

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());
        request.setSupportedFileTypes(new ArrayList<>());
        request.setCustomFileTypes(null);

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());

        request.setSupportedFileTypes(null);
        request.setCustomFileTypes(new ArrayList<>());

        exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void updateExample_exercise_sheet_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        String expectedMessage = "Error: ExerciseSheet not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void updateExample_parent_Example_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setParentId(300L);
        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(getTestExerciseSheet()));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void updateExample_name_already_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        Example example = new Example(EXAMPLE_ID+30);
        example.setName(request.getName());
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().add(example);
        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        String expectedMessage = "Error: example with this name already exists in exerciseSheet";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS, exception.getErrorResponseCode());
    }

    @Test
    public void updateSubExample_name_already_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setParentId(EXAMPLE_ID+10);
        ExerciseSheet exerciseSheet = getTestExerciseSheet();

        Example example = new Example(EXAMPLE_ID+30);
        example.setName(request.getName());

        Example parentExample = new Example(request.getParentId());
        parentExample.setSubExamples(new HashSet<>());
        parentExample.getSubExamples().add(example);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(exampleRepository.findById(request.getParentId())).thenReturn(Optional.of(parentExample));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExample(request);
        });
        String expectedMessage = "Error: sub example with this name already exists in exerciseSheet";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(ApiErrorResponseCodes.SUB_EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS, exception.getErrorResponseCode());
    }

    @Test
    public void updateExample_no_validator() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setSupportedFileTypes(new ArrayList<>());
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        exerciseSheet.setExamples(new HashSet<>());
        request.setSupportedFileTypes(new ArrayList<>());
        request.getSupportedFileTypes().add(FILE_TYPE_ID);

        Example example = getExampleFromUpdateRequest(request);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(fileTypeRepository.findById(FILE_TYPE_ID)).thenReturn(Optional.of(getFileType()));
        when(exampleRepository.findById(request.getId())).thenReturn(Optional.of(example));

        List<SupportFileType> supportFileTypes = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),getExampleFromUpdateRequest(request)));

        ExampleResponseObject expected_responseObject = new ExampleResponseObject();
        expected_responseObject.setId(400L);

        exampleService.updateExample(request);
        verify(exampleRepository).saveAndFlush(getExampleFromUpdateRequest(request));
        verify(supportFileTypeRepository).saveAll(supportFileTypes);
    }

    @Test
    public void updateExample() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        UpdateExampleRequest request = getUpdateExampleRequest();
        request.setSubmitFile(Boolean.FALSE);
        request.setSupportedFileTypes(new ArrayList<>());
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        exerciseSheet.setExamples(new HashSet<>());
        request.setSupportedFileTypes(new ArrayList<>());
        request.getSupportedFileTypes().add(FILE_TYPE_ID);

        Example example = getExampleFromUpdateRequest(request);
        example.setSubmitFile(Boolean.TRUE);
        example.setValidator("test.jar");

        Course course = getTestCourse();
        Semester semester = getTestSemester();
        course.setSemester(semester);
        exerciseSheet.setCourse(course);

        example.setExerciseSheet(exerciseSheet);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(fileTypeRepository.findById(FILE_TYPE_ID)).thenReturn(Optional.of(getFileType()));
        when(exampleRepository.findById(request.getId())).thenReturn(Optional.of(example));

        List<SupportFileType> supportFileTypes = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),getExampleFromUpdateRequest(request)));

        exampleService.updateExample(request);
        Example exampleWithoutValidator = getExampleFromUpdateRequest(request);
        exampleWithoutValidator.setValidator(null);

        verify(exampleRepository).save(exampleWithoutValidator);
        verify(exampleRepository).saveAndFlush(getExampleFromUpdateRequest(request));
        verify(supportFileTypeRepository).saveAll(supportFileTypes);
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

    private SupportFileType getSupportedFileType(FileType fileType, Example example) {

        SupportFileTypeKey supportFileTypeKey = new SupportFileTypeKey();
        supportFileTypeKey.setExampleId(example.getId());
        supportFileTypeKey.setFileTypeId(fileType.getId());

        SupportFileType supportFileType = new SupportFileType();
        supportFileType.setId(supportFileTypeKey);
        supportFileType.setExample(example);
        supportFileType.setFileType(fileType);
        return supportFileType;
    }

    private FileType getFileType()
    {
       FileType type =  new FileType("Archiv-Dateien", "*.zip, *.rar");
       type.setId(FILE_TYPE_ID);
       return type;
    }

    private Example getParentExample()
    {
        Example parentExample = new Example(PARENT_EXAMPLE_ID);
        parentExample.setValidator("test.jar");
        parentExample.setPoints(30);
        parentExample.setSubmitFile(Boolean.TRUE);
        parentExample.setSupportFileTypes(new HashSet<>());
        parentExample.getSupportFileTypes().add(getSupportedFileType(getFileType(),parentExample));
        parentExample.setCustomFileTypes("test.java,hhh");

        Semester semester = new Semester();
        semester.setYear(2020);
        semester.setType(ESemesterType.S);

        Course course = new Course(1222L);
        course.setNumber("333444");

        ExerciseSheet exerciseSheet = new ExerciseSheet(43434L);
        exerciseSheet.setCourse(course);
        course.setSemester(semester);

        parentExample.setExerciseSheet(exerciseSheet);

        return parentExample;
    }

    private Example getParentExample_after_SubExample_created()
    {
        Example parentExample = getParentExample();
        parentExample.setValidator(null);
        parentExample.setPoints(0);
        parentExample.setSubmitFile(Boolean.FALSE);
        parentExample.setSupportFileTypes(new HashSet<>());
        parentExample.setCustomFileTypes(null);

        return parentExample;
    }

    private List<FinishesExample> getFinisExamples(Example example)
    {
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setExample(example);

        FinishesExample finishesExample1 = new FinishesExample();
        finishesExample1.setExample(example);

        List<FinishesExample> finishExamples = new ArrayList<>();
        finishExamples.add(finishesExample);
        finishExamples.add(finishesExample1);

        return finishExamples;
    }
}
