package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExerciseSheetService extends AbstractService {

    private ExampleService exampleService;

    public ExerciseSheetService(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public void createExerciseSheet(CreateExerciseSheetRequest createExerciseSheetRequest) {
        Course course = readCourse(createExerciseSheetRequest.getCourseId());

        checkExerciseSheetName(course, createExerciseSheetRequest.getName(), null);

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

    protected void checkExerciseSheetName(Course course, String name, ExerciseSheet exerciseSheet) {
        boolean exists;
        if (exerciseSheet != null) {
            exists = course.getExerciseSheets().stream()
                    .filter(exerciseSheet1 -> !exerciseSheet.getId().equals(exerciseSheet1.getId()))
                    .anyMatch(exerciseSheet1 -> exerciseSheet1.getName().equals(name));
        } else {
            exists = course.getExerciseSheets().stream().anyMatch(exerciseSheet1 -> exerciseSheet1.getName().equals(name));
        }
        if (exists)
            throw new ServiceException("Error Exercise Sheet with this name already exists in given course",null, ApiErrorResponseCodes.EXERCISE_SHEET_WITH_THIS_NAME_ALREADY_EXISTS,null,null);
    }


    public void updateExerciseSheet(UpdateExerciseSheetRequest updateExerciseSheetRequest) {
        ExerciseSheet exerciseSheet = readExerciseSheet(updateExerciseSheetRequest.getId());

        checkExerciseSheetName(exerciseSheet.getCourse(), updateExerciseSheetRequest.getName(), exerciseSheet);

        exerciseSheet.setMinKreuzel(updateExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(updateExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(updateExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(updateExerciseSheetRequest.getSubmissionDate());
        exerciseSheet.setIssueDate(updateExerciseSheetRequest.getIssueDate());
        exerciseSheet.setDescription(updateExerciseSheetRequest.getDescription());
        exerciseSheet.setIncludeThird(updateExerciseSheetRequest.getIncludeThird());

        exerciseSheetRepository.save(exerciseSheet);
    }

    public ExerciseSheetResponseObject getExerciseSheet(Long id) {
        ExerciseSheet exerciseSheet = readExerciseSheet(id);
        return exerciseSheet.getResponseObject(null);
    }

    public ExerciseSheetKreuzelResponse getExerciseSheetKreuzel(Long exerciseSheetId) {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        ExerciseSheetKreuzelResponse response = new ExerciseSheetKreuzelResponse();
        response.setIncludeThird(exerciseSheet.getIncludeThird());

        List<Example> sortedExampleList = getSortedExampleList(exerciseSheet);
        for (Example example : sortedExampleList)
            response.getExamples().add(createExampleResponseObject(example));

        List<UserInCourse> sortedUserInCourse = exerciseSheet.getCourse().getStudents().stream()
                .filter(userInCourse -> ECourseRole.STUDENT.equals(userInCourse.getRole()))
                .collect(Collectors.toList());
        for (UserInCourse userInCourse : sortedUserInCourse) {
            User user = userInCourse.getUser();
            KreuzelCourseResponse kreuzelCourseResponse = new KreuzelCourseResponse();
            kreuzelCourseResponse.setMatriculationNumber(user.getMatriculationNumber());
            kreuzelCourseResponse.setSurname(user.getSurname());
            kreuzelCourseResponse.setForename(user.getForename());
            kreuzelCourseResponse.setStates(getExampleStatesOfExerciseSheet(sortedExampleList, user));

            response.getKreuzel().add(kreuzelCourseResponse);
        }

        response.getKreuzel().sort(Comparator.comparing(KreuzelCourseResponse::getMatriculationNumber));
        return response;
    }

    protected List<KreuzelCourseState> getExampleStatesOfExerciseSheet(List<Example> examplesOfExerciseSheet, User user) {
        List<KreuzelCourseState> states = new ArrayList<>();
        for (Example example : examplesOfExerciseSheet) {
            KreuzelCourseState state = new KreuzelCourseState();

            Optional<FinishesExample> optFinishesExample = example.getExamplesFinishedByUser().stream()
                    .filter(finishesExample -> finishesExample.getUser().getMatriculationNumber().equals(user.getMatriculationNumber()))
                    .findFirst();
            if (optFinishesExample.isPresent()) {
                state.setType(optFinishesExample.get().getState().getRole());
                state.setDescription(optFinishesExample.get().getDescription());

                List<ViolationHistoryResponse> violationHistoryResponses = optFinishesExample.get().getViolationHistoryList().stream()
                        .map(ViolationHistory::createViolationHistoryResponse).collect(Collectors.toList());
                state.setResult(violationHistoryResponses);
            } else {
                state.setType(EFinishesExampleState.NO.getRole());
                state.setDescription("");
            }
            states.add(state);
        }
        return states;
    }

    protected ExampleResponseObject createExampleResponseObject(Example example) {
        ExampleResponseObject exampleResponseObject = new ExampleResponseObject();
        exampleResponseObject.setName(example.getName());
        exampleResponseObject.setId(example.getId());
        exampleResponseObject.setSubExamples(null);

        return exampleResponseObject;
    }

    protected List<Example> getSortedExampleList(ExerciseSheet exerciseSheet) {
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

    public ExerciseSheetResponseObject getExerciseSheetAssigned(Long exerciseSheetId) {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        UserDetailsImpl userDetails = getUserDetails();
        Boolean isAssignedUser = exerciseSheet.getCourse().getStudents().stream()
                .anyMatch(userInCourse -> userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
        ExerciseSheetResponseObject responseObject = new ExerciseSheetResponseObject();
        if (Boolean.TRUE.equals(isAssignedUser)) {
            responseObject = exerciseSheet.getResponseObject(userDetails.getMatriculationNumber());
        }

        return responseObject;
    }

    @Transactional
    public void deleteExerciseSheet(Long id) throws IOException {
        ExerciseSheet exerciseSheet = readExerciseSheet(id);
        for (Example example : exerciseSheet.getExamples()) {
            exampleService.deleteExampleValidator(example.getId());
        }
        exampleRepository.flush();
        exerciseSheetRepository.delete(exerciseSheet);
    }

    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(Long courseId) {
        UserDetailsImpl userDetails = getUserDetails();
        Course course = readCourse(courseId);
        if (Boolean.FALSE.equals(userDetails.getAdmin())) {
            boolean isOwner = course.getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber());
            boolean isStudentInCourse = course.getStudents().stream()
                    .anyMatch(userInCourse -> userInCourse.getRole().equals(ECourseRole.STUDENT) && userInCourse.getUser().getMatriculationNumber().equals(userDetails.getMatriculationNumber()));
            if (Boolean.FALSE.equals(isOwner) && Boolean.FALSE.equals(isStudentInCourse))
                throw new ServiceException("Error: not authorized to access exerciseSheets", null, null, null, HttpStatus.UNAUTHORIZED);
        }

        List<ExerciseSheet> exerciseSheets = exerciseSheetRepository.findByCourseId(courseId);
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
