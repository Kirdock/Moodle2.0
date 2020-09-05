package com.aau.moodle20;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class CourseServiceUnitTests {

    private static final Long COURSE_ID = 200L;
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



    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
        mockSecurityContext();
    }

    @Test
    public void getCourse()  {
        Course course = getTestCourse();
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        CourseResponseObject responseObject = course.createCourseResponseObject_GetCourse();
        responseObject.setPresented(new ArrayList<>());

        CourseResponseObject courseResponseObject = courseService.getCourse(200L);
        assertEquals(responseObject,courseResponseObject);
    }

    @Test
    public void createCourse()  {
        Course course = getTestCourse();
        when(userRepository.findByMatriculationNumber(OWNER_MATRICULATION_NUMBER)).thenReturn(Optional.of(new User(OWNER_MATRICULATION_NUMBER)));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(new Semester(SEMESTER_ID)));
        when(courseRepository.save(any(Course.class))).thenReturn(course);


        CreateCourseRequest createCourseRequest = new CreateCourseRequest();
        createCourseRequest.setOwner(OWNER_MATRICULATION_NUMBER);
        createCourseRequest.setDescription(course.getDescription());
        createCourseRequest.setMinKreuzel(course.getMinKreuzel());
        createCourseRequest.setMinPoints(course.getMinPoints());
        createCourseRequest.setName(course.getName());
        createCourseRequest.setNumber(course.getNumber());
        createCourseRequest.setSemesterId(SEMESTER_ID);

        CourseResponseObject courseResponseObject = courseService.createCourse(createCourseRequest);
        assertEquals(new CourseResponseObject(course.getId()),courseResponseObject);
    }

    private void mockSecurityContext()
    {
        UserDetailsImpl applicationUser = mock(UserDetailsImpl.class);
        applicationUser.setAdmin(Boolean.FALSE);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
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
        course.setOwner(new User(OWNER_MATRICULATION_NUMBER));
        course.setSemester(new Semester(SEMESTER_ID));
        course.setExerciseSheets(new HashSet<>());
        return course;
    }
}
