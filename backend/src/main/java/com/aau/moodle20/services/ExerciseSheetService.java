package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.SupportFileTypeRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    SupportFileTypeRepository supportFileTypeRepository;



    public void createExerciseSheet(CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        Long courseId = createExerciseSheetRequest.getCourseId();

        if (!courseRepository.existsById(courseId))
            throw new ServiceValidationException("Error: the referenced course does not exists", HttpStatus.NOT_FOUND);

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(new Course(createExerciseSheetRequest.getCourseId()));
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

        if (!exerciseSheetRepository.existsById(updateExerciseSheetRequest.getId()))
            throw new EntityNotFoundException("Error: Exercise sheet not found!");

        ExerciseSheet exerciseSheet = exerciseSheetRepository.findById(updateExerciseSheetRequest.getId()).get();

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

        Optional<ExerciseSheet> exerciseSheetOptional = exerciseSheetRepository.findById(id);
        if(!exerciseSheetOptional.isPresent())
            throw new ServiceValidationException("Error: ExerciseSheet not found!",HttpStatus.NOT_FOUND);

        UserDetailsImpl userDetails = getUserDetails();
        if(!userDetails.getAdmin() && !exerciseSheetOptional.get().getCourse().getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber()))
            throw new ServiceValidationException("Error: Not an Admin or Owner",HttpStatus.UNAUTHORIZED);

        return exerciseSheetOptional.get().getResponseObject(null);
    }

    public ExerciseSheetResponseObject getExerciseSheetAssigned(Long exerciseSheetId) throws ServiceValidationException
    {
        Optional<ExerciseSheet> exerciseSheetOptional = exerciseSheetRepository.findById(exerciseSheetId);
        if(!exerciseSheetOptional.isPresent())
            throw new ServiceValidationException("Error: ExerciseSheet not found!",HttpStatus.NOT_FOUND);

        UserDetailsImpl userDetails = getUserDetails();
        Boolean isAssignedUser =  exerciseSheetOptional.get().getCourse().getStudents().stream()
                .anyMatch(userInCourse -> userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        if(isAssignedUser)
           responseObject = exerciseSheetOptional.get().getResponseObject(userDetails.getMatriculationNumber());

        return responseObject;
    }

    public void deleteExerciseSheet(Long id) throws EntityNotFoundException {
        if (!exerciseSheetRepository.existsById(id))
            throw new EntityNotFoundException("Exercise Sheet not found");
        exerciseSheetRepository.deleteById(id);
    }


    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(Long courseId) throws ServiceValidationException {
        if (!courseRepository.existsById(courseId))
            throw new EntityNotFoundException("Course not found");
        UserDetailsImpl userDetails = getUserDetails();
        Course course = courseRepository.findById(courseId).get();
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

    public UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
