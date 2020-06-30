package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetKreuzelResponse;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.KreuzelCourseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExerciseSheetService extends AbstractService{

    private PdfService pdfService;

    public ExerciseSheetService(PdfService pdfService)
    {
        this.pdfService = pdfService;
    }

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

        exerciseSheetRepository.save(exerciseSheet);
    }

    public ExerciseSheetResponseObject getExerciseSheet(Long id) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(id);
        if(!isAdmin() && !isOwner(exerciseSheet.getCourse()))
            throw new ServiceValidationException("Error: Not an Admin or Owner",HttpStatus.UNAUTHORIZED);

        return exerciseSheet.getResponseObject(null);
    }

    public ExerciseSheetKreuzelResponse getExerciseSheetKreuzel(Long exerciseSheetId) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        ExerciseSheetKreuzelResponse response = new ExerciseSheetKreuzelResponse();
        response.setIncludeThird(exerciseSheet.getIncludeThird());

        List<Example> sortedExampleList = getSortedExampleList(exerciseSheet);
        for(Example example:sortedExampleList)
            response.getExamples().add(createExampleResponseObject(example));

        Comparator<UserInCourse> userInCourseComparatorSureName = Comparator.comparing(userInCourse -> userInCourse.getUser().getSurname());
        Comparator<UserInCourse> userInCourseComparatorForename = Comparator.comparing(userInCourse -> userInCourse.getUser().getForename());
        Comparator<UserInCourse> userInCourseComparatorMatriculationNumber = Comparator.comparing(userInCourse -> userInCourse.getUser().getMatriculationNumber());

        List<UserInCourse> sortedUserInCourse = exerciseSheet.getCourse().getStudents().stream()
                .sorted(userInCourseComparatorSureName.thenComparing(userInCourseComparatorForename).thenComparing(userInCourseComparatorMatriculationNumber))
                .collect(Collectors.toList());
        for(UserInCourse userInCourse: sortedUserInCourse)
        {
            User user = userInCourse.getUser();
            KreuzelCourseResponse kreuzelCourseResponse = new KreuzelCourseResponse();
            kreuzelCourseResponse.setMatriculationNumber(user.getMatriculationNumber());
            kreuzelCourseResponse.setSurname(user.getSurname());
            kreuzelCourseResponse.setForename(user.getForename());
            kreuzelCourseResponse.setStates(getExampleStatesOfExerciseSheet(sortedExampleList,user));

            response.getKreuzel().add(kreuzelCourseResponse);
        }

    return response;
    }

    protected List<String> getExampleStatesOfExerciseSheet(List<Example> examplesOfExerciseSheet,User user)
    {
        List<String> states = new ArrayList<>();
        for(Example example: examplesOfExerciseSheet)
        {
           Optional<FinishesExample> optFinishesExample = example.getExamplesFinishedByUser().stream()
                   .filter(finishesExample -> finishesExample.getUser().getMatriculationNumber().equals(user.getMatriculationNumber()))
                   .findFirst();
           if(optFinishesExample.isPresent())
               states.add(optFinishesExample.get().getState().getRole());
           else
               states.add(EFinishesExampleState.NO.getRole());
        }
        return states;
    }

    protected ExampleResponseObject createExampleResponseObject(Example example)
    {
        ExampleResponseObject exampleResponseObject = new ExampleResponseObject();
        exampleResponseObject.setName(example.getName());
        exampleResponseObject.setId(example.getId());
        exampleResponseObject.setSubExamples(null);

        return exampleResponseObject;
    }

    protected List<Example> getSortedExampleList(ExerciseSheet exerciseSheet)
    {
        List<Example> sortedExampleList = new ArrayList<>();

        List<Example> examples = exerciseSheet.getExamples().stream()
                .filter(example -> example.getParentExample() == null)
                .sorted(Comparator.comparing(Example::getOrder)).collect(Collectors.toList());
        for (Example example : examples) {
            if (example.getSubExamples().isEmpty())
                sortedExampleList.add(example);
            else {
                List<Example> subExamples = example.getSubExamples().stream()
                        .sorted(Comparator.comparing(Example::getOrder)).collect(Collectors.toList());
                sortedExampleList.addAll(subExamples);
            }
        }

        return sortedExampleList;
    }

    public ExerciseSheetResponseObject getExerciseSheetAssigned(Long exerciseSheetId) throws ServiceValidationException {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        UserDetailsImpl userDetails = getUserDetails();
        Boolean isAssignedUser = exerciseSheet.getCourse().getStudents().stream()
                .anyMatch(userInCourse -> userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        if (isAssignedUser) {
            responseObject = exerciseSheet.getResponseObject(userDetails.getMatriculationNumber());
        }

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
        exerciseSheets.sort(Comparator.comparing(ExerciseSheet::getSubmissionDate).thenComparing(ExerciseSheet::getName));
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
