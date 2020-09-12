package com.aau.moodle20;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class CourseServiceUnitTest {

    private static final Long COURSE_ID = 200L;
    private static final String COURSE_NUMBER= "123.123";
    private static final Long SEMESTER_ID = 12L;
    private static final String OWNER_MATRICULATION_NUMBER = "12345678";

    @InjectMocks
    private CourseService courseService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SemesterRepository semesterRepository;
    private String adminMatriculationNumber ="00000000";
    private String normalMatriculationNumber = "12345678";


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



    private void mockSecurityContext_WithUserDetails(UserDetailsImpl userDetails)
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    private UserDetailsImpl getUserDetails_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("admin","admin", Boolean.TRUE, adminMatriculationNumber,"admin","admin" );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("Admin"));
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    private UserDetailsImpl getUserDetails_Not_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("normal","normal", Boolean.FALSE, normalMatriculationNumber,"normal","normal" );
        List<GrantedAuthority> authorities = new ArrayList<>();
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    private Course getTestCourse()
    {
        Course course = new Course();
        course.setId((long) 200);
        course.setDescription("dd");
        course.setDescriptionTemplate("dd");
        course.setId((long) 200);
        course.setMinKreuzel(20);
        course.setMinPoints(20);
        course.setName("dd");
        course.setNumber(COURSE_NUMBER);
        course.setOwner(new User(OWNER_MATRICULATION_NUMBER));
        course.setSemester(new Semester(SEMESTER_ID));
        course.setExerciseSheets(new HashSet<>());
        return course;
    }
}
