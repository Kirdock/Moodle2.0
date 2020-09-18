package com.aau.moodle20.controller;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.response.ViolationHistoryResponse;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.services.ExampleService;
import com.aau.moodle20.services.FinishesExampleService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FinishesExampleControllerTests  extends AbstractControllerTest{

    @MockBean
    private FinishesExampleService finishesExampleService;

    @MockBean
    private ExampleService exampleService;

    @MockBean
    private ExampleRepository exampleRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private ExerciseSheetRepository exerciseSheetRepository;

    private final Long exampleId = 200L;
    private final String matriculationNumber = "12345678";
    private final Long exerciseSheetId = 100L;


    @Before
    public void mockExampleService_Methods() throws IOException, ClassNotFoundException {
        when(finishesExampleService.getKreuzelAttachment(anyLong())).thenReturn(getMockFinishExample());
        when(finishesExampleService.getKreuzelUserCourse(any(String.class),anyLong())).thenReturn(new ArrayList<>());
        when(finishesExampleService.setKreuzelUserAttachment(any(MultipartFile.class),anyLong())).thenReturn(new ViolationHistoryResponse());
        doNothing().when(finishesExampleService).setKreuzelUser(anyList());
        doNothing().when(finishesExampleService).setKreuzelUserMulti(anyList());
        doNothing().when(finishesExampleService).setUserExamplePresented(any(UserExamplePresentedRequest.class));
    }

    @Test
    public void check_all_apis_unauthorized_no_jwt_token() throws Exception {
        //post /user/kreuzel  		setKreuzelUser
        this.mvc.perform(post("/api/user/kreuzel").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /user/kreuzelMulti		setKreuzelUserMulti
        this.mvc.perform(post("/api/user/kreuzelMulti").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /user/kreuzel/attachment	setKreuzelUserAttachment
        this.mvc.perform(post("/api/user/kreuzel/attachment").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        this.mvc.perform(get("/api/user/kreuzel/attachment/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //post /user/examplePresented			setUserExamplePresented
        this.mvc.perform(post("/api/user/examplePresented").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        this.mvc.perform(get("/api/user/12345678/kreuzel/200").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_authorized_admin() throws Exception {
        String jwtToken = prepareAdminUser();

        //post /user/kreuzel  		setKreuzelUser
        perform_Post("/api/user/kreuzel",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isOk());
        //post /user/kreuzelMulti		setKreuzelUserMulti
        perform_Post("/api/user/kreuzelMulti",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isOk());
        //post /user/kreuzel/attachment	setKreuzelUserAttachment
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/user/kreuzel/attachment")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isOk());

        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        perform_Get("/api/user/kreuzel/attachment/200",jwtToken).andExpect(status().isOk());
        //post /user/examplePresented			setUserExamplePresented
        perform_Post("/api/user/examplePresented",jwtToken,mapToJson(createUserExamplePresentedRequest())).andExpect(status().isOk());
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        perform_Get("/api/user/12345678/kreuzel/200",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_all_apis_unauthorized_invalid_jwt_token() throws Exception {
        String jwtToken = "prepareAdminUser();";

        //post /user/kreuzel  		setKreuzelUser
        perform_Post("/api/user/kreuzel",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //post /user/kreuzelMulti		setKreuzelUserMulti
        perform_Post("/api/user/kreuzelMulti",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //post /user/kreuzel/attachment	setKreuzelUserAttachment
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/user/kreuzel/attachment")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());

        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        perform_Get("/api/user/kreuzel/attachment/200",jwtToken).andExpect(status().isUnauthorized());
        //post /user/examplePresented			setUserExamplePresented
        perform_Post("/api/user/examplePresented",jwtToken,mapToJson(createUserExamplePresentedRequest())).andExpect(status().isUnauthorized());
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        perform_Get("/api/user/12345678/kreuzel/200",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_all_apis_unauthorized_expired_jwt_token() throws Exception {
        String jwtToken = generateExpiredAdminJWToken();

        //post /user/kreuzel  		setKreuzelUser
        perform_Post("/api/user/kreuzel",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //post /user/kreuzelMulti		setKreuzelUserMulti
        perform_Post("/api/user/kreuzelMulti",jwtToken,mapToJson(new ArrayList<>())).andExpect(status().isUnauthorized());
        //post /user/kreuzel/attachment	setKreuzelUserAttachment
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test.jpg".getBytes());
        this.mvc.perform(multipart("/api/user/kreuzel/attachment")
                .file(file)
                .param("id","200")
                .header("Authorization",jwtToken)).andExpect(status().isUnauthorized());

        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        perform_Get("/api/user/kreuzel/attachment/200",jwtToken).andExpect(status().isUnauthorized());
        //post /user/examplePresented			setUserExamplePresented
        perform_Post("/api/user/examplePresented",jwtToken,mapToJson(createUserExamplePresentedRequest())).andExpect(status().isUnauthorized());
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        perform_Get("/api/user/12345678/kreuzel/200",jwtToken).andExpect(status().isUnauthorized());
    }

    @Test
    public void check_getUserKreuzelAttachment_user_in_course_ok() throws Exception {
        String jwtToken = prepareUserWhoIsInCourse();
        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        perform_Get("/api/user/kreuzel/attachment/200",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_getUserKreuzelAttachment_not_user_in_course_forbidden() throws Exception {
        String jwtToken = prepareUserWhoIsNotInCourse();
        //get /user/kreuzel/attachment/{exampleId}		getUserKreuzelAttachment
        perform_Get("/api/user/kreuzel/attachment/200",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_setUserExamplePresented_owner_ok() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //post /user/examplePresented			setUserExamplePresented
        perform_Post("/api/user/examplePresented",jwtToken,mapToJson(createUserExamplePresentedRequest())).andExpect(status().isOk());
    }

    @Test
    public void check_setUserExamplePresented_not_owner_forbidden() throws Exception {
        String jwtToken = prepareForUserWhoIsNoOwner();
        //post /user/examplePresented			setUserExamplePresented
        perform_Post("/api/user/examplePresented",jwtToken,mapToJson(createUserExamplePresentedRequest())).andExpect(status().isForbidden());
    }

    @Test
    public void check_getKreuzelUserCourse_owner_ok() throws Exception {
        String jwtToken = prepareForAuthorizedOwner();
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        perform_Get("/api/user/12345678/kreuzel/200",jwtToken).andExpect(status().isOk());
    }

    @Test
    public void check_getKreuzelUserCourse_not_owner_forbidden() throws Exception {
        String jwtToken = prepareForUserWhoIsNoOwner();
        //get /user/{matriculationNumber}/kreuzel/{courseId}	getKreuzelUserCourse
        perform_Get("/api/user/12345678/kreuzel/200",jwtToken).andExpect(status().isForbidden());
    }

    @Test
    public void check_setUserExamplePresented_MatriculationNumber_invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        //post /user/examplePresented			setUserExamplePresented
        UserExamplePresentedRequest userExamplePresentedRequest = createUserExamplePresentedRequest();
        userExamplePresentedRequest.setMatriculationNumber(null);

        perform_Post("/api/user/examplePresented", jwtToken, mapToJson(userExamplePresentedRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("matriculationNumber: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));

        userExamplePresentedRequest.setMatriculationNumber("");

        perform_Post("/api/user/examplePresented", jwtToken, mapToJson(userExamplePresentedRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("matriculationNumber: must not be blank"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_setUserExamplePresented_ExampleId_invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        //post /user/examplePresented			setUserExamplePresented
        UserExamplePresentedRequest userExamplePresentedRequest = createUserExamplePresentedRequest();
        userExamplePresentedRequest.setExampleId(null);

        perform_Post("/api/user/examplePresented", jwtToken, mapToJson(userExamplePresentedRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("exampleId: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    @Test
    public void check_setUserExamplePresented_HasPresented_invalid_Parameter() throws Exception {
        String jwtToken = prepareAdminUser();
        //post /user/examplePresented			setUserExamplePresented
        UserExamplePresentedRequest userExamplePresentedRequest = createUserExamplePresentedRequest();
        userExamplePresentedRequest.setHasPresented(null);

        perform_Post("/api/user/examplePresented", jwtToken, mapToJson(userExamplePresentedRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("hasPresented: must not be null"))
                .andExpect(jsonPath("$.errorResponseCode").value(ApiErrorResponseCodes.INVALID_METHOD_PARAMETER));
    }

    private UserExamplePresentedRequest createUserExamplePresentedRequest() {
        UserExamplePresentedRequest userExamplePresentedRequest = new UserExamplePresentedRequest();
        userExamplePresentedRequest.setExampleId(exampleId);
        userExamplePresentedRequest.setHasPresented(Boolean.FALSE);
        userExamplePresentedRequest.setMatriculationNumber(matriculationNumber);

        return userExamplePresentedRequest;
    }

    private FinishesExample getMockFinishExample() {
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setFileName("test.txt");
        finishesExample.setAttachmentContent("test".getBytes());

        return finishesExample;
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
        when(userRepository.findByMatriculationNumber(user1.getMatriculationNumber())).thenReturn(Optional.of(user1));

        return jwtToken;
    }


    private String prepareUserWhoIsInCourse() {
        User user1 = getUser1();
        Course course = getTestCourse(user1);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(course);
        Example example = getTestExample(exerciseSheet);

        UserInCourse userInCourse = new UserInCourse();
        UserCourseKey key = new UserCourseKey();
        key.setCourseId(course.getId());
        key.setMatriculationNumber(user1.getMatriculationNumber());
        userInCourse.setUser(user1);
        userInCourse.setCourse(course);
        userInCourse.setRole(ECourseRole.STUDENT);
        userInCourse.setId(key);


        user1.setCourses(new HashSet<>(Arrays.asList(userInCourse)));

        String jwtToken = generateValidUserJWToken(user1);
        when(exampleRepository.findById(exampleId)).thenReturn(Optional.of(example));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(exerciseSheetRepository.findById(exerciseSheetId)).thenReturn(Optional.of(exerciseSheet));

        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByMatriculationNumber(user1.getMatriculationNumber())).thenReturn(Optional.of(user1));

        return jwtToken;
    }

    private String prepareUserWhoIsNotInCourse() {
        User user1 = getUser1();
        Course course = getTestCourse(user1);
        ExerciseSheet exerciseSheet = getTestExerciseSheet(course);
        Example example = getTestExample(exerciseSheet);

        UserInCourse userInCourse = new UserInCourse();
        UserCourseKey key = new UserCourseKey();
        key.setCourseId(course.getId());
        key.setMatriculationNumber(user1.getMatriculationNumber());
        userInCourse.setUser(user1);
        userInCourse.setCourse(course);
        userInCourse.setRole(ECourseRole.NONE);
        userInCourse.setId(key);


        user1.setCourses(new HashSet<>(Arrays.asList(userInCourse)));

        String jwtToken = generateValidUserJWToken(user1);
        when(exampleRepository.findById(exampleId)).thenReturn(Optional.of(example));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(exerciseSheetRepository.findById(exerciseSheetId)).thenReturn(Optional.of(exerciseSheet));

        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(userRepository.findByMatriculationNumber(user1.getMatriculationNumber())).thenReturn(Optional.of(user1));

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
