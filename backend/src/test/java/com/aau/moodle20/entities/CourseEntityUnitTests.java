package com.aau.moodle20.entities;

import com.aau.moodle20.entity.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@RunWith(SpringRunner.class)
public class CourseEntityUnitTests {

    private final Long exampleId = 100L;
    private final Long exerciseSheetId = 120L;

    protected  final Long COURSE_ID = 200L;
    protected  final Long EXAMPLE_ID = 210L;
    protected  final Long EXERCISE_SHEET_ID = 220L;
    protected  final String COURSE_NUMBER= "123.123";
    protected  final Long SEMESTER_ID = 111L;
    protected  final String OWNER_MATRICULATION_NUMBER = "12345678";


    @Test
    public void test_course_copy()  {
        Course course = getTestCourse();
        Course copyCourse = course.copy();
        assertEquals(course.getMinKreuzel(),copyCourse.getMinKreuzel());
        assertEquals(course.getMinPoints(),copyCourse.getMinPoints());
        assertEquals(course.getName(),copyCourse.getName());
        assertEquals(course.getNumber(),copyCourse.getNumber());
        assertEquals(course.getOwner(),copyCourse.getOwner());
        assertEquals(course.getDescriptionTemplate(),copyCourse.getDescriptionTemplate());
        assertEquals(course.getDescription(),copyCourse.getDescription());
        assertEquals(course.getUploadCount(),copyCourse.getUploadCount());
    }

    @Test
    public void test_course_createCourseResponseObject()  {
        Course course = getTestCourse();
        CourseResponseObject responseObject = course.createCourseResponseObject();

        assertEquals(course.getId(),responseObject.getId());
        assertEquals(course.getName(),responseObject.getName());
        assertEquals(course.getNumber(),responseObject.getNumber());
        assertEquals(course.getOwner().getMatriculationNumber(),responseObject.getOwner());
    }
    @Test
    public void test_course_createCourseResponseObjectGetCourse_without_exerciseSheets()  {
        Course course = getTestCourse();
        CourseResponseObject responseObject = course.createCourseResponseObjectGetCourse();

        assertEquals(course.getId(),responseObject.getId());
        assertEquals(course.getName(),responseObject.getName());
        assertEquals(course.getNumber(),responseObject.getNumber());
        assertEquals(course.getMinKreuzel(),responseObject.getMinKreuzel());
        assertEquals(course.getMinPoints(),responseObject.getMinPoints());
        assertEquals(course.getDescriptionTemplate(),responseObject.getDescriptionTemplate());
        assertEquals(course.getDescription(),responseObject.getDescription());
        assertEquals(course.getUploadCount(),responseObject.getUploadCount());
        assertEquals(course.getSemester().getId(),responseObject.getSemesterId());
    }

    @Test
    public void test_course_createCourseResponseObjectGetCourse_wit_exerciseSheets()  {
        Course course = getTestCourseWithExerciseSheets();
        CourseResponseObject responseObject = course.createCourseResponseObjectGetCourse();

        assertEquals(course.getId(),responseObject.getId());
        assertEquals(course.getName(),responseObject.getName());
        assertEquals(course.getNumber(),responseObject.getNumber());
        assertEquals(course.getMinKreuzel(),responseObject.getMinKreuzel());
        assertEquals(course.getMinPoints(),responseObject.getMinPoints());
        assertEquals(course.getDescriptionTemplate(),responseObject.getDescriptionTemplate());
        assertEquals(course.getDescription(),responseObject.getDescription());
        assertEquals(course.getUploadCount(),responseObject.getUploadCount());
        assertEquals(course.getSemester().getId(),responseObject.getSemesterId());

        assertEquals(3,responseObject.getExerciseSheets().size());
        assertEquals(EXERCISE_SHEET_ID+20,responseObject.getExerciseSheets().get(0).getId());
        assertEquals(EXERCISE_SHEET_ID+10,responseObject.getExerciseSheets().get(1).getId());
        assertEquals(EXERCISE_SHEET_ID,responseObject.getExerciseSheets().get(2).getId());
    }


    @Test
    public void test_course_createCourseResponseObjectGetAssignedCourse_without_exerciseSheets()  {
        Course course = getTestCourse();
        CourseResponseObject responseObject = course.createCourseResponseObjectGetAssignedCourse("dd");

        assertEquals(course.getId(),responseObject.getId());
        assertEquals(course.getName(),responseObject.getName());
        assertEquals(course.getNumber(),responseObject.getNumber());
        assertEquals(course.getMinKreuzel(),responseObject.getMinKreuzel());
        assertEquals(course.getMinPoints(),responseObject.getMinPoints());
        assertEquals(course.getDescriptionTemplate(),responseObject.getDescriptionTemplate());
        assertEquals(course.getDescription(),responseObject.getDescription());
        assertEquals(course.getUploadCount(),responseObject.getUploadCount());
        assertEquals(course.getSemester().getId(),responseObject.getSemesterId());
    }

    @Test
    public void test_course_createCourseResponseObjectGetAssignedCourse_wit_exerciseSheets()  {
        Course course = getTestCourseWithExerciseSheets();
        CourseResponseObject responseObject = course.createCourseResponseObjectGetAssignedCourse("dd");

        assertEquals(course.getId(),responseObject.getId());
        assertEquals(course.getName(),responseObject.getName());
        assertEquals(course.getNumber(),responseObject.getNumber());
        assertEquals(course.getMinKreuzel(),responseObject.getMinKreuzel());
        assertEquals(course.getMinPoints(),responseObject.getMinPoints());
        assertEquals(course.getDescriptionTemplate(),responseObject.getDescriptionTemplate());
        assertEquals(course.getDescription(),responseObject.getDescription());
        assertEquals(course.getUploadCount(),responseObject.getUploadCount());
        assertEquals(course.getSemester().getId(),responseObject.getSemesterId());

        assertEquals(3,responseObject.getExerciseSheets().size());
        assertEquals(EXERCISE_SHEET_ID+20,responseObject.getExerciseSheets().get(0).getId());
        assertEquals(EXERCISE_SHEET_ID+10,responseObject.getExerciseSheets().get(1).getId());
        assertEquals(EXERCISE_SHEET_ID,responseObject.getExerciseSheets().get(2).getId());
    }

    protected Course getTestCourseWithExerciseSheets()
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
}
