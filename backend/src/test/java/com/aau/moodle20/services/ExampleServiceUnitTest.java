package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ESemesterType;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.repository.*;
import com.aau.moodle20.validation.ValidatorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @Mock
    private ValidatorHandler validatorHandler;

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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);

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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
        exerciseSheet.setExamples(new HashSet<>());
        request.setSupportedFileTypes(new ArrayList<>());
        request.getSupportedFileTypes().add(FILE_TYPE_ID);
        request.setParentId(PARENT_EXAMPLE_ID);
        Example parentExample = getParentExample();
        Example parentExample_copy = getParentExample();


        Example example = getExampleFromCreateRequest(request);
        example.setId(400L);

        List<FinishesExample> finishesExampleList = getFinisExamples(parentExample);

        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(exerciseSheet));
        when(exampleRepository.findById(PARENT_EXAMPLE_ID)).thenReturn(Optional.of(parentExample));
        when(fileTypeRepository.findById(FILE_TYPE_ID)).thenReturn(Optional.of(getFileType()));
        when(exampleRepository.save(getExampleFromCreateRequest(request))).thenReturn(example);
        when(finishesExampleRepository.findByExampleId(PARENT_EXAMPLE_ID)).thenReturn(finishesExampleList);


        List<SupportFileType> supportFileTypes = new ArrayList<>();
        supportFileTypes.add(getSupportedFileType(getFileType(),getExampleFromCreateRequest(request)));

        ExampleResponseObject expected_responseObject = new ExampleResponseObject();
        expected_responseObject.setId(400L);

        ExampleResponseObject responseObject = exampleService.createExample(request);

        verify(finishesExampleRepository).deleteAll(finishesExampleList);

        // support File type
        ArgumentCaptor<HashSet> argument = ArgumentCaptor.forClass(HashSet.class);
        verify(supportFileTypeRepository).deleteAll(argument.capture());
        HashSet<SupportFileType> capturedArguments = argument.<HashSet<SupportFileType>> getValue();
        assertEquals(1, capturedArguments.size());
        assertEquals(parentExample_copy.getId(), capturedArguments.iterator().next().getExample().getId());
        assertEquals(getFileType().getId(), capturedArguments.iterator().next().getFileType().getId());
        verify(supportFileTypeRepository).saveAll(supportFileTypes);
        verify(supportFileTypeRepository).flush();

        verify(exampleRepository,atLeast(2)).save(any(Example.class));
        verify(exampleRepository).flush();
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
        when(exerciseSheetRepository.findById(request.getExerciseSheetId())).thenReturn(Optional.of(getTestExerciseSheet(request.getExerciseSheetId())));

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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);

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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
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
        ExerciseSheet exerciseSheet = getTestExerciseSheet(EXERCISE_SHEET_ID);
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

    @Test
    public void deleteExample_example_not_found()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.deleteExample(EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void deleteExample_no_validator_no_parentExample_no_examples_in_exerciseSheet() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());

        Example example = getTestExample(EXAMPLE_ID);
        example.getExerciseSheet().setExamples(new HashSet<>());
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        exampleService.deleteExample(EXAMPLE_ID);

        verify(exampleRepository).deleteById(EXAMPLE_ID);
        verify(exampleRepository).saveAll(new ArrayList<>());
        verify(exampleRepository,times(2)).flush();
    }


    @Test
    public void deleteExample_no_validator_no_parentExample_with_examples_in_exerciseSheet() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);
        Example example = getTestExample(EXAMPLE_ID);
        example.setOrder(0);
        Example example1 = getTestExample(EXAMPLE_ID+10);
        example1.setOrder(1);
        Example example2 = getTestExample(EXAMPLE_ID+20);
        example2.setOrder(2);
        Example example3 = getTestExample(EXAMPLE_ID+30);
        example3.setOrder(3);

        List<Example> examples = Arrays.asList(example, example1, example2, example3);
        List<Example> examples_expected = Arrays.asList(example,example2, example3);

        examples.forEach(example4 -> {
            example4.setExerciseSheet(exerciseSheet);
            example4.setValidator(null);
        });
        exerciseSheet.getExamples().addAll(examples);

        when(exampleRepository.findById(EXAMPLE_ID+10)).thenReturn(Optional.of(example1));
        for (int i = 0; i < examples_expected.size(); i++)
            examples_expected.get(i).setOrder(i);
        Set<Example> exampleSet = new HashSet<>();
        exampleSet.addAll(examples_expected);

        when(exerciseSheet.getExamples()).thenReturn(exampleSet);
        exampleService.deleteExample(EXAMPLE_ID+10);

        verify(exampleRepository,times(1)).findById(EXAMPLE_ID+10);
        verify(exampleRepository).deleteById(EXAMPLE_ID+10);
        verify(exampleRepository).saveAll(examples_expected);
        verify(exampleRepository,times(2)).flush();
    }

    @Test
    public void deleteExample_no_validator_with_parentExample_no_examples_in_exerciseSheet() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);

        Example parentExample = getTestExample(PARENT_EXAMPLE_ID);
        Example example = getTestExample(EXAMPLE_ID);
        example.setOrder(0);
        Example example1 = getTestExample(EXAMPLE_ID+10);
        example1.setOrder(1);
        Example example2 = getTestExample(EXAMPLE_ID+20);
        example2.setOrder(2);
        Example example3 = getTestExample(EXAMPLE_ID+30);
        example3.setOrder(3);

        parentExample.setSubExamples(new HashSet<>());
        List<Example> examples_expected = Arrays.asList(example,example2, example3);

        parentExample.getSubExamples().addAll(examples_expected);
        example1.setParentExample(parentExample);
        example1.setValidator(null);

        when(exampleRepository.findById(EXAMPLE_ID+10)).thenReturn(Optional.of(example1));
        for (int i = 0; i < examples_expected.size(); i++)
            examples_expected.get(i).setOrder(i);
        Set<Example> exampleSet = new HashSet<>();
        exampleSet.addAll(examples_expected);

        when(exerciseSheet.getExamples()).thenReturn(exampleSet);
        exampleService.deleteExample(EXAMPLE_ID+10);

        verify(exampleRepository,times(1)).findById(EXAMPLE_ID+10);
        verify(exampleRepository).deleteById(EXAMPLE_ID+10);
        verify(exampleRepository).saveAll(examples_expected);
        verify(exampleRepository,times(2)).flush();
    }

    @Test
    public void deleteExample_with_validator_with_parentExample() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ExerciseSheet exerciseSheet = mock(ExerciseSheet.class);

        Example parentExample = getTestExample(PARENT_EXAMPLE_ID);
        Example example = getTestExample(EXAMPLE_ID);
        example.setOrder(0);
        Example example1 = getTestExample(EXAMPLE_ID+10);
        example1.setOrder(1);
        Example example2 = getTestExample(EXAMPLE_ID+20);
        example2.setOrder(2);
        Example example3 = getTestExample(EXAMPLE_ID+30);
        example3.setOrder(3);

        parentExample.setSubExamples(new HashSet<>());
        List<Example> examples_expected = Arrays.asList(example,example2, example3);

        parentExample.getSubExamples().addAll(examples_expected);
        example1.setParentExample(parentExample);

        Example example1_copy = example1.copy();
        example1_copy.setId(EXAMPLE_ID+10);
        example1_copy.setValidator(null);

        when(exampleRepository.findById(EXAMPLE_ID+10)).thenReturn(Optional.of(example1));
        for (int i = 0; i < examples_expected.size(); i++)
            examples_expected.get(i).setOrder(i);
        Set<Example> exampleSet = new HashSet<>();
        exampleSet.addAll(examples_expected);

        when(exerciseSheet.getExamples()).thenReturn(exampleSet);
        exampleService.deleteExample(EXAMPLE_ID+10);

        verify(exampleRepository,times(2)).findById(EXAMPLE_ID+10);
        verify(exampleRepository).save(example1_copy);
        verify(exampleRepository).deleteById(EXAMPLE_ID+10);
        verify(exampleRepository).saveAll(examples_expected);
        verify(exampleRepository,times(2)).flush();
    }

    @Test
    public void getFileTypes_empty() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        when(fileTypeRepository.findAll()).thenReturn(new ArrayList<>());
        List<FileTypeResponseObject> responseObjectList = exampleService.getFileTypes();
        assertEquals(new ArrayList<>(), responseObjectList);
    }

    @Test
    public void getFileTypes() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<FileType> fileTypes= new ArrayList<>();
        fileTypes.add(getFileType());
        when(fileTypeRepository.findAll()).thenReturn(fileTypes);
        List<FileTypeResponseObject> expectedResponseObjects = fileTypes.stream().map(this::createFileTypeResponseObject)
                .collect(Collectors.toList());
        List<FileTypeResponseObject> responseObjectList = exampleService.getFileTypes();
        assertEquals(expectedResponseObjects, responseObjectList);
    }

    @Test
    public void getExample_example_notExits() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.getExample(EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getExample() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = mock(Example.class);
        ExampleResponseObject expectedResponseObject = new ExampleResponseObject();
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        when(example.createExampleResponseObject(null)).thenReturn(expectedResponseObject);
        ExampleResponseObject responseObject = exampleService.getExample(EXAMPLE_ID);
        assertEquals(expectedResponseObject,responseObject);
    }

    @Test
    public void updateExampleOrder_empty_List() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<ExampleOrderRequest> orderRequests = new ArrayList<>();
        exampleService.updateExampleOrder(orderRequests);
        verify(exampleRepository,times(0)).save(any(Example.class));
        verify(exampleRepository,times(0)).findById(anyLong());
    }

    @Test
    public void updateExampleOrder_example_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<ExampleOrderRequest> orderRequests = new ArrayList<>();
        ExampleOrderRequest request = new ExampleOrderRequest();
        request.setOrder(2);
        request.setId(EXAMPLE_ID);
        orderRequests.add(request);
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExampleOrder(orderRequests);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
        verify(exampleRepository,times(0)).save(any(Example.class));
        verify(exampleRepository,times(1)).findById(anyLong());
    }


    @Test
    public void updateExampleOrder_example_not_found_2() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<ExampleOrderRequest> orderRequests = new ArrayList<>();
        ExampleOrderRequest request = new ExampleOrderRequest();
        request.setOrder(2);
        request.setId(EXAMPLE_ID);
        orderRequests.add(request);
        request = new ExampleOrderRequest();
        request.setId(EXAMPLE_ID+10);
        request.setOrder(4);
        orderRequests.add(request);

        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(getTestExample(EXAMPLE_ID)));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.updateExampleOrder(orderRequests);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
        verify(exampleRepository,times(1)).save(any(Example.class));
        verify(exampleRepository,times(2)).findById(anyLong());
    }

    @Test
    public void updateExampleOrder() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        List<ExampleOrderRequest> orderRequests = new ArrayList<>();
        ExampleOrderRequest request = new ExampleOrderRequest();
        request.setOrder(2);
        request.setId(EXAMPLE_ID);
        orderRequests.add(request);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(getTestExample(EXAMPLE_ID)));

        exampleService.updateExampleOrder(orderRequests);
        Example expectedExample = getTestExample(EXAMPLE_ID);
        expectedExample.setOrder(2);
        verify(exampleRepository,times(1)).save(expectedExample);
        verify(exampleRepository,times(1)).findById(anyLong());
    }

    @Test
    public void setExampleValidator_example_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jar", MediaType.IMAGE_JPEG_VALUE, "test.jar".getBytes());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.setExampleValidator(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void setExampleValidator_not_jar_file() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jar".getBytes());
        Example example = getTestExample(EXAMPLE_ID);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.setExampleValidator(file,EXAMPLE_ID);
        });
        String expectedMessage = "Error: Not a jar File!";
        assertEquals(expectedMessage,exception.getMessage());
    }

    @Test
    public void setExampleValidator() throws IOException, ClassNotFoundException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        MockMultipartFile file = new MockMultipartFile("file", "test.jar", MediaType.IMAGE_JPEG_VALUE, "test.jar".getBytes());
        Example example = getTestExample(EXAMPLE_ID);
        Example expectedExample = getTestExample(EXAMPLE_ID);

        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        doNothing().when(validatorHandler).checkValidatorFile(any(MultipartFile.class));
        exampleService.setExampleValidator(file,EXAMPLE_ID);

        expectedExample.setValidator("test.jar");
        expectedExample.setSubmitFile(Boolean.TRUE);
        verify(exampleRepository).save(expectedExample);
    }

    @Test
    public void getExampleValidator_example_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.getExampleValidator(EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void getExampleValidator_not_jar_file() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        Example example_validator = exampleService.getExampleValidator(EXAMPLE_ID);
        assertNotNull(example_validator.getValidatorContent());
    }

    @Test
    public void copyValidator_original_example_no_validator() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        example.setValidator(null);
        Example copiedExample = example.copy();
        exampleService.copyValidator(example,copiedExample);
        verify(exampleRepository,times(0)).save(any(Example.class));
    }

    @Test
    public void copyValidator() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        Example copiedExample = example.copy();
        copiedExample.setId(EXAMPLE_ID+12);
        copiedExample.setExerciseSheet(example.getExerciseSheet());
        exampleService.copyValidator(example,copiedExample);
        Example expectedExample = example.copy();
        expectedExample.setValidator(example.getValidator());
        verify(exampleRepository,times(1)).save(expectedExample);
    }

    @Test
    public void deleteExampleValidator_example_not_found() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            exampleService.deleteExampleValidator(EXAMPLE_ID);
        });
        String expectedMessage = "Error: Example not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getHttpStatus());
    }

    @Test
    public void deleteExampleValidator_example_no_validator() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        example.setValidator(null);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        exampleService.deleteExampleValidator(EXAMPLE_ID);
        verify(exampleRepository,times(0)).save(any(Example.class));

        example.setValidator("");
        exampleService.deleteExampleValidator(EXAMPLE_ID);
        verify(exampleRepository,times(0)).save(any(Example.class));
    }

    @Test
    public void deleteExampleValidator() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Example example = getTestExample(EXAMPLE_ID);
        when(exampleRepository.findById(EXAMPLE_ID)).thenReturn(Optional.of(example));
        exampleService.deleteExampleValidator(EXAMPLE_ID);

        Example expectedExample = getTestExample(EXAMPLE_ID);
        expectedExample.setValidator(null);
        verify(exampleRepository,times(1)).save(expectedExample);
    }
    private FileTypeResponseObject createFileTypeResponseObject(FileType fileType)
    {
        FileTypeResponseObject responseObject = new FileTypeResponseObject();
        responseObject.setId(fileType.getId());
        responseObject.setName(fileType.getName());
        responseObject.setValue(fileType.getValue());

        return responseObject;
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

    private Example getParentExample() {
        return getTestExample(PARENT_EXAMPLE_ID);
    }


    @Override
    protected Example getTestExample(Long exampleId) {
        Example example = new Example(exampleId);
        example.setValidator("test.jar");
        example.setPoints(30);
        example.setSubmitFile(Boolean.TRUE);
        example.setSupportFileTypes(new HashSet<>());
        example.getSupportFileTypes().add(getSupportedFileType(getFileType(), example));
        example.setCustomFileTypes("test.java,hhh");

        Semester semester = new Semester();
        semester.setYear(2020);
        semester.setType(ESemesterType.S);

        Course course = new Course(1222L);
        course.setNumber("333444");

        ExerciseSheet exerciseSheet = new ExerciseSheet(43434L);
        exerciseSheet.setCourse(course);
        course.setSemester(semester);

        example.setExerciseSheet(exerciseSheet);

        return example;
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
