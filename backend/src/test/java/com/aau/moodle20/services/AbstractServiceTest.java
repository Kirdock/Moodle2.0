package com.aau.moodle20.services;

import com.aau.moodle20.entity.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractServiceTest {

    protected String adminMatriculationNumber ="00000000";
    protected String normalMatriculationNumber = "12345678";

    protected  final Long COURSE_ID = 200L;
    protected  final Long EXERCISE_SHEET_ID = 230L;
    protected  final Long EXAMPLE_ID = 230L;
    protected  final String COURSE_NUMBER= "123.123";
    protected  final Long SEMESTER_ID = 111L;
    protected  final String OWNER_MATRICULATION_NUMBER = "12345678";


    protected void mockSecurityContext_WithUserDetails(UserDetailsImpl userDetails)
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    protected UserDetailsImpl getUserDetails_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("admin","admin", Boolean.TRUE, adminMatriculationNumber,"admin","admin" );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("Admin"));
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    protected UserDetailsImpl getUserDetails_Not_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("normal","normal", Boolean.FALSE, normalMatriculationNumber,"normal","normal" );
        List<GrantedAuthority> authorities = new ArrayList<>();
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    protected Course getTestCourse()
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

    protected Course getTestCourseWithExerciseSheet()
    {
        Course course = getTestCourse();

        Example example = new Example() ;
        example.setId(EXAMPLE_ID);
        example.setValidator("test.txt");
        example.setExamplesFinishedByUser(new HashSet<>());


        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(course);
        exerciseSheet.setId(EXERCISE_SHEET_ID);
        exerciseSheet.setIncludeThird(Boolean.FALSE);
        exerciseSheet.setDescription("dd");
        exerciseSheet.setExamples(new HashSet<>());
        exerciseSheet.getExamples().add(example);
        course.getExerciseSheets().add(exerciseSheet);
        return course;
    }
}
