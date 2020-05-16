package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.domain.*;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public ExerciseSheetResponseObject getExerciseSheet(Long id) throws ServiceValidationException
    {
        if(!exerciseSheetRepository.existsById(id))
            throw new ServiceValidationException("Error: exercise Sheet not found", ApiErrorResponseCodes.EXERCISE_SHEET_NOT_FOUND);

        Optional<ExerciseSheet> exerciseSheetOptional = exerciseSheetRepository.findById(id);
        ExerciseSheet exerciseSheet = exerciseSheetOptional.get();
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        responseObject.setId(exerciseSheet.getId());
        responseObject.setCourseId(exerciseSheet.getCourse().getId());
        responseObject.setMinKreuzel(exerciseSheet.getMinKreuzel());
        responseObject.setMinPoints(exerciseSheet.getMinPoints());
        responseObject.setName(exerciseSheet.getName());
        responseObject.setSubmissionDate(exerciseSheet.getSubmissionDate());
        responseObject.setOrder(exerciseSheet.getSortOrder());

        return responseObject;
    }
}