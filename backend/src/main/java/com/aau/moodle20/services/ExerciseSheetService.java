package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExerciseSheetService extends AbstractService{

    public void createExerciseSheet(CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        Course course = readCourse(createExerciseSheetRequest.getCourseId());

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(course);
        exerciseSheet.setMinKreuzel(createExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(createExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(createExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(createExerciseSheetRequest.getSubmissionDate());
        exerciseSheet.setIssueDate(createExerciseSheetRequest.getIssueDate());
        exerciseSheet.setDescription(createExerciseSheetRequest.getDescription());
        exerciseSheet.setIncludeThird(createExerciseSheetRequest.getIncludeThird());
        exerciseSheet.setUploadCount(0);

        exerciseSheetRepository.save(exerciseSheet);
    }

    public void updateExerciseSheet(UpdateExerciseSheetRequest updateExerciseSheetRequest) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(updateExerciseSheetRequest.getId());
        List<ExerciseSheet> courseExerciseSheets = exerciseSheetRepository.findByCourse_Id(exerciseSheet.getCourse().getId());
        courseExerciseSheets.removeIf(sheet -> sheet.getId().equals(updateExerciseSheetRequest.getId()));

        exerciseSheet.setMinKreuzel(updateExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(updateExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(updateExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(updateExerciseSheetRequest.getSubmissionDate());
        exerciseSheet.setIssueDate(updateExerciseSheetRequest.getIssueDate());
        exerciseSheet.setDescription(updateExerciseSheetRequest.getDescription());
        exerciseSheet.setIncludeThird(updateExerciseSheetRequest.getIncludeThird());
        if (updateExerciseSheetRequest.getUploadCount() != null)
            exerciseSheet.setUploadCount(updateExerciseSheetRequest.getUploadCount());

        exerciseSheetRepository.save(exerciseSheet);
    }

    public ExerciseSheetResponseObject getExerciseSheet(Long id) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(id);
        if(!isAdmin() && !isOwner(exerciseSheet.getCourse()))
            throw new ServiceValidationException("Error: Not an Admin or Owner",HttpStatus.UNAUTHORIZED);

        return exerciseSheet.getResponseObject(null);
    }

    public ExerciseSheetResponseObject getExerciseSheetAssigned(Long exerciseSheetId) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        UserDetailsImpl userDetails = getUserDetails();
        Boolean isAssignedUser = exerciseSheet.getCourse().getStudents().stream()
                .anyMatch(userInCourse -> userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        if (isAssignedUser)
            responseObject = exerciseSheet.getResponseObject(userDetails.getMatriculationNumber());

        return responseObject;
    }

    public void deleteExerciseSheet(Long id) throws EntityNotFoundException {
        ExerciseSheet exerciseSheet = readExerciseSheet(id);
        exerciseSheetRepository.delete(exerciseSheet);
    }

    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(Long courseId) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        Course course = readCourse(courseId);
        if (!userDetails.getAdmin()) {
            boolean isOwner = course.getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber());
            boolean isStudentInCourse = course.getStudents().stream()
                    .anyMatch(userInCourse -> userInCourse.getRole().equals(ECourseRole.Student) && userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
            if (!isOwner && !isStudentInCourse)
                throw new ServiceValidationException("Error: not authorized to access exerciseSheets", HttpStatus.UNAUTHORIZED);
        }

        List<ExerciseSheet> exerciseSheets = exerciseSheetRepository.findByCourse_Id(courseId);
        List<ExerciseSheetResponseObject> responseObjects = new ArrayList<>();
        for (ExerciseSheet sheet : exerciseSheets) {
            ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
            responseObject.setName(sheet.getName());
            responseObject.setSubmissionDate(sheet.getSubmissionDate());
            responseObject.setId(sheet.getId());
            responseObjects.add(responseObject);
        }
        return responseObjects;
    }
}
