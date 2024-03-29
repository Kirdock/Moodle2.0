package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService extends AbstractService {


    private ExampleService exampleService;

    public CourseService(@Autowired ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public CourseResponseObject createCourse(CreateCourseRequest createCourseRequest) {

        Semester semester = readSemester(createCourseRequest.getSemesterId());
        // check if owner exists
        readUser(createCourseRequest.getOwner());
        if (Boolean.TRUE.equals(courseRepository.existsByNumberAndSemesterId(createCourseRequest.getNumber(), createCourseRequest.getSemesterId())))
            throw new ServiceException("Course in Semester already exists",null, ApiErrorResponseCodes.COURSE_IN_SEMESTER_ALREADY_EXISTS,null,null);

        Course course = new Course();
        course.setMinKreuzel(createCourseRequest.getMinKreuzel());
        course.setMinPoints(createCourseRequest.getMinPoints());
        course.setName(createCourseRequest.getName());
        course.setNumber(createCourseRequest.getNumber());
        course.setSemester(semester);
        course.setOwner(new User(createCourseRequest.getOwner()));
        course.setDescription(createCourseRequest.getDescription());
        course.setUploadCount(0);
        course = courseRepository.save(course);

        return new CourseResponseObject(course.getId());
    }

    public Course updateCourse(UpdateCourseRequest updateCourseRequest) {
        UserDetailsImpl userDetails = getUserDetails();
        Course course = readCourse(updateCourseRequest.getId());
        if (Boolean.TRUE.equals(userDetails.getAdmin()) && updateCourseRequest.getOwner() != null &&
                Boolean.FALSE.equals(userRepository.existsByMatriculationNumber(updateCourseRequest.getOwner())))
            throw new ServiceException("Error: Owner cannot be updated because the given matriculationNumber those not exists!", null, null, null, HttpStatus.NOT_FOUND);

        // if number is updated check if given number already exists in semester
        if (!updateCourseRequest.getNumber().equals(course.getNumber())) {
            if (Boolean.TRUE.equals(courseRepository.existsByNumberAndSemesterId(updateCourseRequest.getNumber(), course.getSemester().getId())))
                throw new ServiceException("Error: A Course with this number already exists", null, ApiErrorResponseCodes.CHANGED_COURSE_NUMBER_ALREADY_EXISTS, null, null);
        }

        course.setMinKreuzel(updateCourseRequest.getMinKreuzel());
        course.setMinPoints(updateCourseRequest.getMinPoints());
        course.setName(updateCourseRequest.getName());
        course.setNumber(updateCourseRequest.getNumber());
        course.setDescription(updateCourseRequest.getDescription());
        if (Boolean.TRUE.equals(userDetails.getAdmin()) && updateCourseRequest.getOwner() != null)
            course.setOwner(new User(updateCourseRequest.getOwner()));

        return courseRepository.save(course);
    }

    public void updateCoursePresets(UpdateCoursePresets updateCoursePresets) {
        Course course = readCourse(updateCoursePresets.getId());
        course.setDescriptionTemplate(updateCoursePresets.getDescription());
        course.setUploadCount(updateCoursePresets.getUploadCount());
        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) throws IOException {
        Course course = readCourse(courseId);
        Set<ExerciseSheet> exerciseSheets = course.getExerciseSheets();
        for (ExerciseSheet exerciseSheet : exerciseSheets) {
            for (Example example : exerciseSheet.getExamples())
                exampleService.deleteExampleValidator(example.getId());
        }

        courseRepository.delete(course);
    }

    public CourseResponseObject getCourse(long courseId) {
        UserDetailsImpl userDetails = getUserDetails();
        Course course = readCourse(courseId);
        CourseResponseObject responseObject = course.createCourseResponseObjectGetCourse();
        responseObject.setPresented(createCoursePresentedList(course));

        if (Boolean.TRUE.equals(userDetails.getAdmin()))
            responseObject.setOwner(course.getOwner().getMatriculationNumber());

        return responseObject;
    }

    public List<FinishesExampleResponse> getCoursePresented(Long courseId) {
        Course course = readCourse(courseId);
        return createCoursePresentedList(course);
    }

    protected List<FinishesExampleResponse> createCoursePresentedList(Course course) {
        List<FinishesExampleResponse> finishesExampleResponses = new ArrayList<>();
        Comparator<Example> exampleComparator = Comparator.comparing(Example::getOrder);
        List<ExerciseSheet> sortedExerciseSheets = course.getExerciseSheets().stream()
                .sorted(Comparator.comparing(ExerciseSheet::getSubmissionDate).thenComparing(ExerciseSheet::getName))
                .collect(Collectors.toList());

        for (ExerciseSheet exerciseSheet : sortedExerciseSheets) {

            List<Example> sortedExamples = exerciseSheet.getExamples().stream()
                    .filter(example -> example.getParentExample() == null)
                    .sorted(exampleComparator).collect(Collectors.toList());
            for (Example example : sortedExamples) {
                if (example.getSubExamples() == null || example.getSubExamples().isEmpty())
                    finishesExampleResponses.addAll(createFinishesExampleResponses(example, exerciseSheet));
                else {
                    List<Example> sortedSubExamples = example.getSubExamples().stream().sorted(exampleComparator).collect(Collectors.toList());
                    sortedSubExamples.forEach(subExample -> finishesExampleResponses.addAll(createFinishesExampleResponses(subExample, exerciseSheet)));
                }
            }
        }

        return finishesExampleResponses;
    }

    protected List<FinishesExampleResponse> createFinishesExampleResponses(Example example, ExerciseSheet exerciseSheet) {
        List<FinishesExampleResponse> finishesExampleResponses = new ArrayList<>();
        for (FinishesExample finishesExample : example.getExamplesFinishedByUser()) {

            if (Boolean.TRUE.equals(finishesExample.getHasPresented())) {
                FinishesExampleResponse finishesExampleResponse = new FinishesExampleResponse();
                finishesExampleResponse.setMatriculationNumber(finishesExample.getUser().getMatriculationNumber());
                finishesExampleResponse.setSurname(finishesExample.getUser().getSurname());
                finishesExampleResponse.setForename(finishesExample.getUser().getForename());
                finishesExampleResponse.setExampleId(finishesExample.getExample().getId());
                finishesExampleResponse.setExampleName(finishesExample.getExample().getName());
                finishesExampleResponse.setExerciseSheetName(exerciseSheet.getName());
                finishesExampleResponse.setExerciseSheetId(exerciseSheet.getId());
                finishesExampleResponses.add(finishesExampleResponse);
            }
        }
        return finishesExampleResponses;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CourseResponseObject copyCourse(CopyCourseRequest copyCourseRequest) throws IOException {

        Semester semester = readSemester(copyCourseRequest.getSemesterId());
        Course originalCourse = readCourse(copyCourseRequest.getCourseId());

        if (Boolean.TRUE.equals(courseRepository.existsByNumberAndSemesterId(originalCourse.getNumber(), semester.getId())))
            throw new ServiceException("Error: A course with this number already exists in given semester!", null, ApiErrorResponseCodes.COPIED_COURSE_NUMBER_ALREADY_EXISTS, null, null);

        // first copy course
        Course copiedCourse = originalCourse.copy();
        copiedCourse.setSemester(semester);
        copiedCourse = courseRepository.save(copiedCourse);

        if (originalCourse.getExerciseSheets() == null || originalCourse.getExerciseSheets().isEmpty()) {
            CourseResponseObject courseResponseObject = new CourseResponseObject();
            courseResponseObject.setId(copiedCourse.getId());
            return courseResponseObject;
        }

        for (ExerciseSheet exerciseSheet : originalCourse.getExerciseSheets()) {
            // second copy exercise sheet
            ExerciseSheet copiedExerciseSheet = exerciseSheet.copy();
            copiedExerciseSheet.setCourse(copiedCourse);
            exerciseSheetRepository.save(copiedExerciseSheet);

            if (exerciseSheet.getExamples() == null)
                continue;

            // copy examples
            for (Example example : exerciseSheet.getExamples()) {

                // if true example is subExample and this is handled below
                if (example.getParentExample() != null)
                    continue;

                Example copiedExample = example.copy();
                copiedExample.setExerciseSheet(copiedExerciseSheet);
                exampleRepository.save(copiedExample);
                exampleService.copyValidator(example, copiedExample);


                if (example.getSubExamples() != null) {
                    for (Example subExample : example.getSubExamples()) {
                        Example copiedSubExample = subExample.copy();
                        copiedSubExample.setExerciseSheet(copiedExerciseSheet);
                        copiedSubExample.setParentExample(copiedExample);
                        exampleRepository.save(copiedSubExample);
                        exampleService.copyValidator(subExample, copiedSubExample);
                        if (subExample.getSupportFileTypes() != null) {
                            copySupportFileType(subExample.getSupportFileTypes(), copiedSubExample);
                        }
                    }
                }

                if (example.getSupportFileTypes() != null) {
                    copySupportFileType(example.getSupportFileTypes(), copiedExample);
                }
            }
        }
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(copiedCourse.getId());
        return responseObject;
    }

    protected void copySupportFileType(Set<SupportFileType> supportFileTypes, Example copiedExample) {
        for (SupportFileType supportFileType : supportFileTypes) {

            SupportFileType copiedSupportFileType = new SupportFileType();

            SupportFileTypeKey supportFileTypeKey = new SupportFileTypeKey();
            supportFileTypeKey.setExampleId(copiedExample.getId());
            supportFileTypeKey.setFileTypeId(supportFileType.getFileType().getId());

            copiedSupportFileType.setExample(copiedExample);
            copiedSupportFileType.setId(supportFileTypeKey);
            copiedSupportFileType.setFileType(supportFileType.getFileType());
            supportFileTypeRepository.save(copiedSupportFileType);
        }
    }
}
