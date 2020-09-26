package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.repository.*;
import org.assertj.core.api.OptionalAssert;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class CourseServiceUnitTest extends AbstractServiceTest{

    @InjectMocks
    private CourseService courseService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private ExampleService exampleService;
    @Mock
    private ExampleRepository exampleRepository;
    @Mock
    private SupportFileTypeRepository supportFileTypeRepository;




    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCourse_Admin()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = course.createCourseResponseObjectGetCourse();
        responseObject.setPresented(new ArrayList<>());
        responseObject.setOwner(course.getOwner().getMatriculationNumber());

        CourseResponseObject courseResponseObject = courseService.getCourse(200L);
        assertEquals(responseObject,courseResponseObject);
    }

    @Test
    public void getCourse_Not_Admin()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = course.createCourseResponseObjectGetCourse();
        responseObject.setPresented(new ArrayList<>());

        CourseResponseObject courseResponseObject = courseService.getCourse(200L);
        assertEquals(responseObject,courseResponseObject);
    }

    @Test
    public void createCourse()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(userRepository.findByMatriculationNumber(OWNER_MATRICULATION_NUMBER)).thenReturn(Optional.of(new User(OWNER_MATRICULATION_NUMBER)));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(new Semester(SEMESTER_ID)));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CreateCourseRequest createCourseRequest = getCourseCreateRequest(course);

        CourseResponseObject courseResponseObject = courseService.createCourse(createCourseRequest);
        assertEquals(new CourseResponseObject(course.getId()),courseResponseObject);
    }

    @Test
    public void createCourse_where_Semester_does_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(userRepository.findByMatriculationNumber(OWNER_MATRICULATION_NUMBER)).thenReturn(Optional.of(new User(OWNER_MATRICULATION_NUMBER)));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CreateCourseRequest createCourseRequest = getCourseCreateRequest(course);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.createCourse(createCourseRequest);
        });
        String expectedMessage = "Error: Semester not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND
        );
    }

    @Test
    public void createCourse_where_Owner_does_not_exists()  {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(new Semester(SEMESTER_ID)));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CreateCourseRequest createCourseRequest = getCourseCreateRequest(course);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.createCourse(createCourseRequest);
        });
        String expectedMessage = "Error: User not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND
        );
    }

    @Test
    public void createCourse_where_Course_already_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        CreateCourseRequest createCourseRequest = getCourseCreateRequest(course);

        when(userRepository.findByMatriculationNumber(OWNER_MATRICULATION_NUMBER)).thenReturn(Optional.of(new User(OWNER_MATRICULATION_NUMBER)));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(new Semester(SEMESTER_ID)));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseRepository.existsByNumberAndSemester_Id(COURSE_NUMBER, SEMESTER_ID)).thenReturn(Boolean.TRUE);


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.createCourse(createCourseRequest);
        });
        String expectedMessage = "Course in Semester already exists";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getErrorResponseCode(), ApiErrorResponseCodes.COURSE_IN_SEMESTER_ALREADY_EXISTS);
    }

    @Test
    public void updateCourse_where_Course_does_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        UpdateCourseRequest updateCourseRequest = getUpdateCourseRequest(course);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.updateCourse(updateCourseRequest);
        });
        String expectedMessage = "Error: Course not found!";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateCourse_where_new_course_number_already_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.existsByNumberAndSemester_Id("333.333", course.getSemester().getId())).thenReturn(Boolean.TRUE);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.existsByMatriculationNumber(course.getOwner().getMatriculationNumber())).thenReturn(Boolean.TRUE);
        UpdateCourseRequest updateCourseRequest = getUpdateCourseRequest(course);
        updateCourseRequest.setNumber("333.333");

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.updateCourse(updateCourseRequest);
        });
        String expectedMessage = "Error: A Course with this number already exists";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getErrorResponseCode(), ApiErrorResponseCodes.CHANGED_COURSE_NUMBER_ALREADY_EXISTS);
    }

    @Test
    public void updateCourse_where_updated_Owner_does_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        UpdateCourseRequest updateCourseRequest = getUpdateCourseRequest(course);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.updateCourse(updateCourseRequest);
        });
        String expectedMessage = "Error: Owner cannot be updated because the given matriculationNumber those not exists!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }


    @Test
    public void updateCourse_Admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        UpdateCourseRequest updateCourseRequest = getUpdateCourseRequest(course);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(userRepository.existsByMatriculationNumber(course.getOwner().getMatriculationNumber())).thenReturn(Boolean.TRUE);

        Course updatedCourse = courseService.updateCourse(updateCourseRequest);
        assertEquals(course,updatedCourse);
    }

    @Test
    public void updateCoursePresets() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        UpdateCoursePresets updateCoursePresets = new UpdateCoursePresets();
        updateCoursePresets.setId(course.getId());
        updateCoursePresets.setDescription("testDescription");
        updateCoursePresets.setUploadCount(10);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        course.setDescription("testDescription");
        course.setUploadCount(10);

        when(courseRepository.save(any(Course.class))).thenReturn(course);
        courseService.updateCoursePresets(updateCoursePresets);
    }


    @Test
    public void updateCoursePresets_course_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        UpdateCoursePresets updateCoursePresets = new UpdateCoursePresets();
        updateCoursePresets.setId(course.getId());
        updateCoursePresets.setDescription(course.getDescription());
        updateCoursePresets.setUploadCount(course.getUploadCount());

        when(courseRepository.save(any(Course.class))).thenReturn(course);


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.updateCoursePresets(updateCoursePresets);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteCourse_course_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        doNothing().when(courseRepository).delete(any(Course.class));


        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.deleteCourse(COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteCourse_without_ExerciseSheets() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        doNothing().when(courseRepository).delete(any(Course.class));
        courseService.deleteCourse(COURSE_ID);
    }

    @Test
    public void deleteCourse_with_ExerciseSheets() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourseWithExerciseSheet();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        doNothing().when(courseRepository).delete(any(Course.class));
        doNothing().when(exampleService).deleteExampleValidator(anyLong());

        courseService.deleteCourse(COURSE_ID);
    }

    @Test
    public void getCourse_course_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.getCourse(COURSE_ID);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void getCourse_without_ExerciseSheets_admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        CourseResponseObject testResponseObject = getCourseResponseObject(course);
        testResponseObject.setPresented(new ArrayList<>());
        testResponseObject.setOwner(course.getOwner().getMatriculationNumber());
        testResponseObject.setExerciseSheets(new ArrayList<>());

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = courseService.getCourse(COURSE_ID);
        assertEquals(testResponseObject,responseObject);
    }

    @Test
    public void getCourse_without_ExerciseSheets_non_admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Not_Admin());
        Course course = getTestCourse();
        CourseResponseObject testResponseObject = getCourseResponseObject(course);
        testResponseObject.setPresented(new ArrayList<>());
        testResponseObject.setExerciseSheets(new ArrayList<>());

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = courseService.getCourse(COURSE_ID);
        assertEquals(testResponseObject,responseObject);
    }

    @Test
    public void getCourse_with_ExerciseSheets_admin() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourseWithMultipleExerciseSheets();
        CourseResponseObject testResponseObject = getCourseResponseObject(course);
        testResponseObject.setPresented(new ArrayList<>());
        testResponseObject.setOwner(course.getOwner().getMatriculationNumber());
        testResponseObject.setExerciseSheets(new ArrayList<>());
        testResponseObject.getExerciseSheets().addAll(course.getExerciseSheets().stream()
                .map(ExerciseSheet::getResponseObjectLessInfo)
                .sorted(Comparator.comparing(ExerciseSheetResponseObject::getSubmissionDate).thenComparing(ExerciseSheetResponseObject::getName))
                .collect(Collectors.toList()));

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = courseService.getCourse(COURSE_ID);
        assertEquals(testResponseObject,responseObject);
    }

    @Test
    public void getCoursePresented_without_ExerciseSheets() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<FinishesExampleResponse> finishesExampleResponses = courseService.getCoursePresented(COURSE_ID);
        assertEquals(0,finishesExampleResponses.size());
    }

    @Test
    public void getCoursePresented_with_ExerciseSheet() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Course course = getTestCourse();

        User adminUser = new User();
        adminUser.setMatriculationNumber(adminMatriculationNumber);
        adminUser.setForename("admin");
        adminUser.setSurname("admin");

        List<FinishesExample> finishesExamples = new ArrayList<>();
        List<FinishesExampleResponse> testFinishExampleResponses = new ArrayList<>();

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(course);
        exerciseSheet.setName("aaaa");
        exerciseSheet.setId(EXERCISE_SHEET_ID);
        exerciseSheet.setIncludeThird(Boolean.FALSE);
        exerciseSheet.setDescription("dd");
        exerciseSheet.setSubmissionDate(LocalDateTime.now().plusDays(10));
        exerciseSheet.setExamples(new HashSet<>());
        course.getExerciseSheets().add(exerciseSheet);

        Example example1 = new Example();
        example1.setId(200L);
        example1.setExerciseSheet(exerciseSheet);
        example1.setOrder(0);
        example1.setExamplesFinishedByUser(new HashSet<>());

        Example parentExample = new Example();
        parentExample.setId(210L);
        parentExample.setExerciseSheet(exerciseSheet);
        parentExample.setSubExamples(new HashSet<>());
        parentExample.setOrder(1);

        Example subExample = new Example();
        subExample.setId(220L);
        subExample.setExerciseSheet(exerciseSheet);
        subExample.setOrder(2);

        Example subExample2 = new Example();
        subExample2.setId(230L);
        subExample2.setExerciseSheet(exerciseSheet);
        subExample2.setOrder(0);

        parentExample.getSubExamples().add(subExample);
        parentExample.getSubExamples().add(subExample2);

        subExample.setParentExample(parentExample);
        subExample2.setParentExample(parentExample);

        exerciseSheet.getExamples().add(example1);
        exerciseSheet.getExamples().add(parentExample);
        exerciseSheet.getExamples().add(subExample);
        exerciseSheet.getExamples().add(subExample2);

        FinishesExample finishesExample = getTextFinisExample(adminUser);
        example1.setExamplesFinishedByUser(new HashSet<>());
        finishesExample.setExample(example1);
        example1.getExamplesFinishedByUser().add(finishesExample);
        finishesExamples.add(finishesExample);

        finishesExample = getTextFinisExample(adminUser);
        subExample2.setExamplesFinishedByUser(new HashSet<>());
        finishesExample.setExample(subExample2);
        subExample2.getExamplesFinishedByUser().add(finishesExample);
        finishesExamples.add(finishesExample);

         finishesExample = getTextFinisExample(adminUser);
        subExample.setExamplesFinishedByUser(new HashSet<>());
        finishesExample.setExample(subExample);
        subExample.getExamplesFinishedByUser().add(finishesExample);
        finishesExamples.add(finishesExample);


        for (FinishesExample finishesExample1 : finishesExamples ) {

            FinishesExampleResponse finishesExampleResponse = new FinishesExampleResponse();
            finishesExampleResponse.setMatriculationNumber(finishesExample1.getUser().getMatriculationNumber());
            finishesExampleResponse.setSurname(finishesExample1.getUser().getSurname());
            finishesExampleResponse.setForename(finishesExample1.getUser().getForename());
            finishesExampleResponse.setExampleId(finishesExample1.getExample().getId());
            finishesExampleResponse.setExampleName(finishesExample1.getExample().getName());
            finishesExampleResponse.setExerciseSheetName(exerciseSheet.getName());
            finishesExampleResponse.setExerciseSheetId(exerciseSheet.getId());
            testFinishExampleResponses.add(finishesExampleResponse);
        }

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<FinishesExampleResponse> finishesExampleResponses = courseService.getCoursePresented(COURSE_ID);
        assertEquals(3,finishesExampleResponses.size());
        assertEquals(testFinishExampleResponses, finishesExampleResponses);
    }

    @Test
    public void copyCourse_semester_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        CopyCourseRequest copyCourseRequest = new CopyCourseRequest();
        copyCourseRequest.setSemesterId(SEMESTER_ID);
        copyCourseRequest.setCourseId(COURSE_ID);
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.copyCourse(copyCourseRequest);
        });
        String expectedMessage = "Error: Semester not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void copyCourse_course_not_exists() {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        CopyCourseRequest copyCourseRequest = new CopyCourseRequest();
        copyCourseRequest.setSemesterId(SEMESTER_ID);
        copyCourseRequest.setCourseId(COURSE_ID);
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            courseService.copyCourse(copyCourseRequest);
        });
        String expectedMessage = "Error: Course not found!";
        assertEquals(expectedMessage,exception.getMessage());
        assertEquals(exception.getHttpStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void copyCourse_course_no_exerciseSheets() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        CopyCourseRequest copyCourseRequest = getCopyCourseRequest();
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        Course copiedCourse = new Course();
        copiedCourse.setId(455L);
        when(courseRepository.save(any(Course.class))).thenReturn(copiedCourse);

        CourseResponseObject courseResponseObject_expected = new CourseResponseObject();
        courseResponseObject_expected.setId(copiedCourse.getId());

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        assertEquals(courseResponseObject_expected,responseObject);
    }

    @Test
    public void copyCourse_course_exerciseSheets_no_examples() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        course.getExerciseSheets().add(exerciseSheet);
        exerciseSheet.setCourse(course);
        CopyCourseRequest copyCourseRequest = getCopyCourseRequest();
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        Course copiedCourse = new Course();
        copiedCourse.setId(455L);
        when(courseRepository.save(any(Course.class))).thenReturn(copiedCourse);

        CourseResponseObject courseResponseObject_expected = new CourseResponseObject();
        courseResponseObject_expected.setId(copiedCourse.getId());

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        assertEquals(courseResponseObject_expected,responseObject);
    }

    @Test
    public void copyCourse_course_exerciseSheets_with_examples() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course = getTestCourseWithExerciseSheet();
        CopyCourseRequest copyCourseRequest = getCopyCourseRequest();
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        Course copiedCourse = new Course();
        copiedCourse.setId(455L);
        when(courseRepository.save(any(Course.class))).thenReturn(copiedCourse);

        CourseResponseObject courseResponseObject_expected = new CourseResponseObject();
        courseResponseObject_expected.setId(copiedCourse.getId());

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        assertEquals(courseResponseObject_expected,responseObject);
    }

    @Test
    public void copyCourse_course_exerciseSheets_with_subExamples() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        Example example = getTestExample();
        Example subExample = getTestExample();
        course.getExerciseSheets().add(exerciseSheet);
        exerciseSheet.getExamples().add(example);
        exerciseSheet.getExamples().add(subExample);
        subExample.setParentExample(example);
        example.getSubExamples().add(subExample);


        CopyCourseRequest copyCourseRequest = getCopyCourseRequest();
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        Course copiedCourse = new Course();
        copiedCourse.setId(455L);
        when(courseRepository.save(any(Course.class))).thenReturn(copiedCourse);

        CourseResponseObject courseResponseObject_expected = new CourseResponseObject();
        courseResponseObject_expected.setId(copiedCourse.getId());

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        assertEquals(courseResponseObject_expected,responseObject);
    }

    @Test
    public void copyCourse_course_exerciseSheets_with_subExamples_supportFileTypes() throws IOException {
        mockSecurityContext_WithUserDetails(getUserDetails_Admin());
        Semester semester = getTestSemester();
        Course course = getTestCourse();
        ExerciseSheet exerciseSheet = getTestExerciseSheet();
        Example example = getTestExample();
        Example subExample = getTestExample();
        course.getExerciseSheets().add(exerciseSheet);
        exerciseSheet.getExamples().add(example);
        exerciseSheet.getExamples().add(subExample);
        subExample.setParentExample(example);
        example.getSubExamples().add(subExample);

        SupportFileType supportFileType = getTestSupportFileType(example);
        SupportFileType supportFileTypeSubExample = getTestSupportFileType(subExample);

        example.getSupportFileTypes().add(supportFileType);
        subExample.getSupportFileTypes().add(supportFileTypeSubExample);

        CopyCourseRequest copyCourseRequest = getCopyCourseRequest();
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        Course copiedCourse = new Course();
        copiedCourse.setId(455L);
        when(courseRepository.save(any(Course.class))).thenReturn(copiedCourse);

        CourseResponseObject courseResponseObject_expected = new CourseResponseObject();
        courseResponseObject_expected.setId(copiedCourse.getId());

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        assertEquals(courseResponseObject_expected,responseObject);
    }

    private CopyCourseRequest getCopyCourseRequest()
    {
        CopyCourseRequest copyCourseRequest = new CopyCourseRequest();
        copyCourseRequest.setSemesterId(SEMESTER_ID);
        copyCourseRequest.setCourseId(COURSE_ID);

        return copyCourseRequest;
    }

    private SupportFileType getTestSupportFileType(Example example) {
        SupportFileType supportFileType = new SupportFileType();
        supportFileType.setExample(example);
        supportFileType.setFileType(getTestFileType());

        return supportFileType;
    }

    private FileType getTestFileType() {
        FileType fileType = new FileType();
        fileType.setName("word");
        fileType.setValue("*.docx");
        fileType.setId(200L);

        return fileType;
    }

    private Example getTestExample()
    {
        Example example = new Example() ;
        example.setId(EXAMPLE_ID);
        example.setValidator("test.txt");
        example.setExamplesFinishedByUser(new HashSet<>());
        example.setSupportFileTypes(new HashSet<>());

        return example;
    }

    private FinishesExample getTextFinisExample(User user)
    {
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setHasPresented(Boolean.TRUE);
        finishesExample.setDescription("dd");
        finishesExample.setFileName("dd");
        finishesExample.setValid(Boolean.TRUE);
        finishesExample.setUser(user);

        return finishesExample;
    }

    private CourseResponseObject getCourseResponseObject (Course course)
    {
        CourseResponseObject testResponseObject = new CourseResponseObject();
        testResponseObject.setId(course.getId());
        testResponseObject.setName(course.getName());
        testResponseObject.setNumber(course.getNumber());
        testResponseObject.setMinKreuzel(course.getMinKreuzel());
        testResponseObject.setMinPoints(course.getMinPoints());
        testResponseObject.setDescriptionTemplate(course.getDescriptionTemplate());
        testResponseObject.setSemesterId(course.getSemester().getId());
        testResponseObject.setDescription(course.getDescription());
        testResponseObject.setUploadCount(course.getUploadCount());

        return testResponseObject;
    }

    private CreateCourseRequest getCourseCreateRequest(Course course)
    {
        CreateCourseRequest createCourseRequest = new CreateCourseRequest();
        createCourseRequest.setOwner(course.getOwner().getMatriculationNumber());
        createCourseRequest.setDescription(course.getDescription());
        createCourseRequest.setMinKreuzel(course.getMinKreuzel());
        createCourseRequest.setMinPoints(course.getMinPoints());
        createCourseRequest.setName(course.getName());
        createCourseRequest.setNumber(course.getNumber());
        createCourseRequest.setSemesterId(course.getSemester().getId());

        return createCourseRequest;
    }

    private UpdateCourseRequest getUpdateCourseRequest(Course course)
    {
        UpdateCourseRequest updateCourseRequest = new UpdateCourseRequest();
        updateCourseRequest.setOwner(course.getOwner().getMatriculationNumber());
        updateCourseRequest.setDescription(course.getDescription());
        updateCourseRequest.setMinKreuzel(course.getMinKreuzel());
        updateCourseRequest.setMinPoints(course.getMinPoints());
        updateCourseRequest.setName(course.getName());
        updateCourseRequest.setNumber(course.getNumber());
        updateCourseRequest.setId(course.getId());

        return updateCourseRequest;
    }

    protected Course getTestCourseWithMultipleExerciseSheets()
    {
        Course course = getTestCourse();

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(course);
        exerciseSheet.setName("aaaa");
        exerciseSheet.setId(EXERCISE_SHEET_ID);
        exerciseSheet.setIncludeThird(Boolean.FALSE);
        exerciseSheet.setDescription("dd");
        exerciseSheet.setSubmissionDate(LocalDateTime.now().plusDays(10));
        exerciseSheet.setExamples(new HashSet<>());
        course.getExerciseSheets().add(exerciseSheet);


        LocalDateTime now = LocalDateTime.now();

        ExerciseSheet exerciseSheet1 = new ExerciseSheet();
        exerciseSheet1.setCourse(course);
        exerciseSheet1.setId(EXERCISE_SHEET_ID+10);
        exerciseSheet1.setName("bbbb");
        exerciseSheet1.setIncludeThird(Boolean.FALSE);
        exerciseSheet1.setDescription("dd");
        exerciseSheet1.setSubmissionDate(now);
        exerciseSheet1.setExamples(new HashSet<>());
        course.getExerciseSheets().add(exerciseSheet1);

        ExerciseSheet exerciseSheet2 = new ExerciseSheet();
        exerciseSheet2.setCourse(course);
        exerciseSheet2.setId(EXERCISE_SHEET_ID+20);
        exerciseSheet2.setName("aaaa");
        exerciseSheet2.setIncludeThird(Boolean.FALSE);
        exerciseSheet2.setSubmissionDate(now);
        exerciseSheet2.setDescription("dd");
        exerciseSheet2.setExamples(new HashSet<>());
        course.getExerciseSheets().add(exerciseSheet2);
        return course;
    }
}
