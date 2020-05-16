package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.domain.*;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExerciseSheetService {

    @Autowired
    ExerciseSheetRepository exerciseSheetRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;


    public void createExerciseSheet(CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        Long courseId = createExerciseSheetRequest.getCourseId();

        if (!courseRepository.existsById(courseId))
            throw new ServiceValidationException("Error: the referenced course does not exists", ApiErrorResponseCodes.COURSE_NOT_FOUND);
        if (exerciseSheetRepository.existsByCourse_IdAndSortOrder(courseId, createExerciseSheetRequest.getOrder()))
            throw new ServiceValidationException("Error: An exercise Sheet for this course and this order number exists already",
                    ApiErrorResponseCodes.EXERCISE_SHEET_COURSE_WITH_ORDER_ALREADY_EXISTS);

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(new Course(createExerciseSheetRequest.getCourseId()));
        exerciseSheet.setSortOrder(createExerciseSheetRequest.getOrder());
        exerciseSheet.setMinKreuzel(createExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(createExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(createExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(createExerciseSheetRequest.getSubmissionDate());

        exerciseSheetRepository.save(exerciseSheet);
    }

    public void updateExerciseSheet(UpdateExerciseSheetRequest updateExerciseSheetRequest) throws ServiceValidationException {

        if (!exerciseSheetRepository.existsById(updateExerciseSheetRequest.getId()))
            throw new EntityNotFoundException("Error: Exercise sheet not found!");

        ExerciseSheet exerciseSheet = exerciseSheetRepository.findById(updateExerciseSheetRequest.getId()).get();

        List<ExerciseSheet> courseExerciseSheets = exerciseSheetRepository.findByCourse_Id(exerciseSheet.getCourse().getId());
        courseExerciseSheets.removeIf(sheet -> sheet.getId().equals(updateExerciseSheetRequest.getId()));
        if (courseExerciseSheets.stream().anyMatch(sheet -> sheet.getSortOrder().equals(updateExerciseSheetRequest.getOrder()) ))
            throw new ServiceValidationException("Error: An exercise Sheet for this course and this order number exists already",
                    ApiErrorResponseCodes.EXERCISE_SHEET_COURSE_WITH_ORDER_ALREADY_EXISTS);

        exerciseSheet.setSortOrder(updateExerciseSheetRequest.getOrder());
        exerciseSheet.setMinKreuzel(updateExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(updateExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(updateExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(updateExerciseSheetRequest.getSubmissionDate());

        exerciseSheetRepository.save(exerciseSheet);
    }

    public ExerciseSheetResponseObject getExerciseSheet(Long id) throws ServiceValidationException {
        if (!exerciseSheetRepository.existsById(id))
            throw new EntityNotFoundException("Exercise Sheet not found");
        Optional<ExerciseSheet> exerciseSheetOptional = exerciseSheetRepository.findById(id);
        return exerciseSheetOptional.get().getResponseObject();
    }


    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(Long courseId) throws ServiceValidationException {
        if (!courseRepository.existsById(courseId))
            throw new EntityNotFoundException("Course not found");
        List<ExerciseSheet> exerciseSheets = exerciseSheetRepository.findByCourse_Id(courseId);
        return exerciseSheets.stream().map(ExerciseSheet::getResponseObject).collect(Collectors.toList());
    }
}
