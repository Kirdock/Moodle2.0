package com.aau.moodle20.entities;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ExerciseSheetEntityUnitTests {

    @Test
    public void getResponseObject()  {

        ExerciseSheet exerciseSheet = getTestExerciseSheet(340L);
        Example example = mock(Example.class);
        Example example1 = mock(Example.class);
        Example example2 = mock(Example.class);

        ExampleResponseObject exampleResponseObject1 = new ExampleResponseObject(123L);
        exampleResponseObject1.setOrder(0);
        ExampleResponseObject exampleResponseObject2 = new ExampleResponseObject(321L);
        exampleResponseObject2.setOrder(1);

        when(example1.getParentExample()).thenReturn(new Example(400L));
        when(example.createExampleResponseObject(anyString())).thenReturn(exampleResponseObject1);
        when(example2.createExampleResponseObject(anyString())).thenReturn(exampleResponseObject2);

        exerciseSheet.getExamples().add(example);
        exerciseSheet.getExamples().add(example1);
        exerciseSheet.getExamples().add(example2);

        Course course = getTestCourse();
        exerciseSheet.setCourse(course);

        ExerciseSheetResponseObject responseObject = exerciseSheet.getResponseObject("12345678");

        assertEquals(responseObject.getCourseId(),course.getId());
        assertEquals(responseObject.getMinKreuzel(),exerciseSheet.getMinKreuzel());
        assertEquals(responseObject.getMinPoints(),exerciseSheet.getMinPoints());
        assertEquals(responseObject.getName(),exerciseSheet.getName());
        assertEquals(responseObject.getSubmissionDate(),exerciseSheet.getSubmissionDate());
        assertEquals(responseObject.getIssueDate(),exerciseSheet.getIssueDate());
        assertEquals(responseObject.getDescription(),exerciseSheet.getDescription());
        assertEquals(responseObject.getIncludeThird(),exerciseSheet.getIncludeThird());
        assertEquals(responseObject.getCourseName(),course.getName());
        assertEquals(responseObject.getCourseNumber(),course.getNumber());
        assertEquals(responseObject.getUploadCount(),course.getUploadCount());
        assertEquals(2,responseObject.getExamples().size());
        assertEquals(123L, responseObject.getExamples().get(0).getId());
        assertEquals(321L, responseObject.getExamples().get(1).getId());
        assertEquals(340L, responseObject.getId());
    }


    @Test
    public void getResponseObjectLessInfo()  {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(340L);
        ExerciseSheetResponseObject responseObject = exerciseSheet.getResponseObjectLessInfo();

        assertNull(responseObject.getCourseId());
        assertNull(responseObject.getIssueDate());
        assertNull(responseObject.getDescription());
        assertNull(responseObject.getIncludeThird());
        assertNull(responseObject.getCourseName());
        assertNull(responseObject.getCourseNumber());
        assertNull(responseObject.getUploadCount());
        assertNull(responseObject.getExamples());

        assertEquals(responseObject.getMinKreuzel(),exerciseSheet.getMinKreuzel());
        assertEquals(responseObject.getMinPoints(),exerciseSheet.getMinPoints());
        assertEquals(responseObject.getSubmissionDate(),exerciseSheet.getSubmissionDate());
        assertEquals(responseObject.getName(),exerciseSheet.getName());
        assertEquals(340L,responseObject.getId());
    }



    @Test
    public void getResponseObjectLessInfo_WithExampleInfo()  {

        ExerciseSheet exerciseSheet = getTestExerciseSheet(340L);
        Example example = mock(Example.class);
        Example example1 = mock(Example.class);
        Example example2 = mock(Example.class);

        Set<Example> subExamples = new HashSet<>();
        subExamples.add(new Example(333L));

        exerciseSheet.getExamples().add(example);
        exerciseSheet.getExamples().add(example1);
        exerciseSheet.getExamples().add(example2);

        for(Example example3:exerciseSheet.getExamples())
        {
            when(example3.getPoints()).thenReturn(10);
            when(example3.getWeighting()).thenReturn(2);
        }

        when(example.getSubExamples()).thenReturn(new HashSet<>());
        when(example1.getSubExamples()).thenReturn(subExamples);
        when(example2.getSubExamples()).thenReturn(new HashSet<>());

        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setId(new FinishesExampleKey("12345678",456L));
        finishesExample.setState(EFinishesExampleState.YES);

        Set<FinishesExample> finishesExampleSet = new HashSet<>();
        finishesExampleSet.add(finishesExample);

        when(example.getExamplesFinishedByUser()).thenReturn(finishesExampleSet);
        when(example1.getExamplesFinishedByUser()).thenReturn(new HashSet<>());
        when(example2.getExamplesFinishedByUser()).thenReturn(new HashSet<>());

        ExerciseSheetResponseObject responseObject = exerciseSheet.getResponseObjectLessInfoWithExampleInfo("12345678");

        assertNull(responseObject.getCourseId());
        assertNull(responseObject.getIssueDate());
        assertNull(responseObject.getDescription());
        assertNull(responseObject.getIncludeThird());
        assertNull(responseObject.getCourseName());
        assertNull(responseObject.getCourseNumber());
        assertNull(responseObject.getUploadCount());
        assertNull(responseObject.getExamples());

        assertEquals(responseObject.getMinKreuzel(),exerciseSheet.getMinKreuzel());
        assertEquals(responseObject.getMinPoints(),exerciseSheet.getMinPoints());
        assertEquals(responseObject.getSubmissionDate(),exerciseSheet.getSubmissionDate());
        assertEquals(responseObject.getName(),exerciseSheet.getName());
        assertEquals(340L,responseObject.getId());
        assertEquals(60,responseObject.getPointsTotal());
        assertEquals(2,responseObject.getExampleCount());
        assertEquals(1,responseObject.getKreuzel());
        assertEquals(20, responseObject.getPoints());
    }


    @Test
    public void getTotalPoints() {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(340L);
        Example example = mock(Example.class);
        Example example1 = mock(Example.class);
        Example example2 = mock(Example.class);

        exerciseSheet.getExamples().add(example);
        exerciseSheet.getExamples().add(example1);
        exerciseSheet.getExamples().add(example2);

        for (Example example3 : exerciseSheet.getExamples()) {
            when(example3.getPoints()).thenReturn(10);
            when(example3.getWeighting()).thenReturn(2);
        }

        Integer totalPoints = exerciseSheet.getTotalPoints();
        assertEquals(60, totalPoints);
    }

    @Test
    public void copy()  {
        ExerciseSheet exerciseSheet = getTestExerciseSheet(340L);
        ExerciseSheet copiedExerciseSheet = exerciseSheet.copy();

        assertEquals(exerciseSheet.getDescription(), copiedExerciseSheet.getDescription());
        assertEquals(exerciseSheet.getIssueDate(),copiedExerciseSheet.getIssueDate());
        assertEquals(exerciseSheet.getMinKreuzel(),copiedExerciseSheet.getMinKreuzel());
        assertEquals(exerciseSheet.getMinPoints(),copiedExerciseSheet.getMinPoints());
        assertEquals(exerciseSheet.getIncludeThird(),copiedExerciseSheet.getIncludeThird());
        assertEquals(exerciseSheet.getSubmissionDate(),copiedExerciseSheet.getSubmissionDate());
        assertEquals(exerciseSheet.getName(),copiedExerciseSheet.getName());
    }

    protected ExerciseSheet getTestExerciseSheet(Long exerciseSheetId)
    {
        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setId(exerciseSheetId);
        exerciseSheet.setMinPoints(20);
        exerciseSheet.setMinKreuzel(30);
        exerciseSheet.setIssueDate(LocalDateTime.now());
        exerciseSheet.setSubmissionDate(LocalDateTime.now());
        exerciseSheet.setName("exerciseSheet");
        exerciseSheet.setDescription("DD");
        exerciseSheet.setIncludeThird(Boolean.FALSE);
        exerciseSheet.setExamples(new HashSet<>());

        return exerciseSheet;
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
        course.setNumber("123.456");
        course.setOwner(new User("12345678"));
        course.setSemester(new Semester(300L));
        course.setExerciseSheets(new HashSet<>());
        return course;
    }
}
