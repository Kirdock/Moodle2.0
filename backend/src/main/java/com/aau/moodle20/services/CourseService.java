package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCourseDescriptionTemplate;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.KreuzelCourseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService extends AbstractService {


    public CourseResponseObject createCourse(CreateCourseRequest createCourseRequest) throws ServiceValidationException {

        Semester semester = readSemester(createCourseRequest.getSemesterId());
        if(courseRepository.existsByNameAndNumberAndSemester_Id(createCourseRequest.getName(),createCourseRequest.getNumber(),createCourseRequest.getSemesterId()))
            throw new ServiceValidationException("Course in Semester already exists", ApiErrorResponseCodes.COURSE_IN_SEMESTER_ALREADY_EXISTS);

        Course course = new Course();
        course.setMinKreuzel(createCourseRequest.getMinKreuzel());
        course.setMinPoints(createCourseRequest.getMinPoints());
        course.setName(createCourseRequest.getName());
        course.setNumber(createCourseRequest.getNumber());
        course.setSemester(semester);
        course.setOwner(new User(createCourseRequest.getOwner()));
        course.setDescription(createCourseRequest.getDescription());
        courseRepository.save(course);

        return new CourseResponseObject(course.getId());
    }

    public void updateCourse(UpdateCourseRequest updateCourseRequest) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        Course course = readCourse(updateCourseRequest.getId());
        if (userDetails.getAdmin() && updateCourseRequest.getOwner() != null && !userRepository.existsByMatriculationNumber(updateCourseRequest.getOwner()))
            throw new ServiceValidationException("Error: Owner cannot be updated because the given matriculationNumber those not exists!", HttpStatus.NOT_FOUND);

        if (!userDetails.getAdmin() && !isOwner(course))
            throw new ServiceValidationException("Error: User is not owner of this course and thus cannot update this course!", HttpStatus.UNAUTHORIZED);

        course.setMinKreuzel(updateCourseRequest.getMinKreuzel());
        course.setMinPoints(updateCourseRequest.getMinPoints());
        course.setName(updateCourseRequest.getName());
        course.setNumber(updateCourseRequest.getNumber());
        course.setDescription(updateCourseRequest.getDescription());
        if (userDetails.getAdmin() && updateCourseRequest.getOwner() != null)
            course.setOwner(new User(updateCourseRequest.getOwner()));

        courseRepository.save(course);
    }

    public void updateCourseDescriptionTemplate(UpdateCourseDescriptionTemplate updateCourseDescriptionTemplate) throws ServiceValidationException
    {
        Course course = readCourse(updateCourseDescriptionTemplate.getId());
        UserDetailsImpl userDetails = getUserDetails();
        if(!userDetails.getAdmin() && !isOwner(course))
            throw new ServiceValidationException("Error: not authorized to update course",HttpStatus.UNAUTHORIZED);

        course.setDescriptionTemplate(updateCourseDescriptionTemplate.getDescription());
        courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) throws EntityNotFoundException {
        Course course = readCourse(courseId);
        courseRepository.delete(course);
    }

    public CourseResponseObject getCourse(long courseId) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();

        Course course = readCourse(courseId);
        if (!userDetails.getAdmin() && !isOwner(course))
            throw new ServiceValidationException("Error: neither admin or owner", HttpStatus.UNAUTHORIZED);

        CourseResponseObject responseObject = course.createCourseResponseObject_GetCourse();
        responseObject.setPresented(createCoursePresentedList(course));
        responseObject.setKreuzelList(createKreuzelCourseResponses(course));

        if (userDetails.getAdmin())
            responseObject.setOwner(course.getOwner().getMatriculationNumber());

        return responseObject;
    }

    protected List<FinishesExampleResponse> createCoursePresentedList(Course course)
    {
        List<FinishesExampleResponse> finishesExampleResponses = new ArrayList<>();
        Comparator<Example> exampleComparator = Comparator.comparing(Example::getOrder);
        List<ExerciseSheet> sortedExerciseSheets = course.getExerciseSheets().stream()
                .sorted(Comparator.comparing(ExerciseSheet::getSubmissionDate)).collect(Collectors.toList());

        for (ExerciseSheet exerciseSheet : sortedExerciseSheets) {

            List<Example> sortedExamples = exerciseSheet.getExamples().stream()
                    .filter(example -> example.getParentExample()==null)
                    .sorted(exampleComparator).collect(Collectors.toList());
            for (Example example : sortedExamples) {
                if (example.getSubExamples() == null || example.getSubExamples().isEmpty())
                    finishesExampleResponses.addAll(createFinishesExampleResponses(example, exerciseSheet));
                else {
                    List<Example> sortedSubExamples = example.getSubExamples().stream().sorted(exampleComparator).collect(Collectors.toList());
                    sortedSubExamples.forEach(subExample -> finishesExampleResponses.addAll(createFinishesExampleResponses(subExample,exerciseSheet)));
                }
            }
        }

        return finishesExampleResponses;
    }

    protected List<KreuzelCourseResponse> createKreuzelCourseResponses(Course course) {
        List<KreuzelCourseResponse> kreuzelCourseResponses = new ArrayList<>();
        List<Example> examplesInCourse = new ArrayList<>();

        for (ExerciseSheet exerciseSheet : course.getExerciseSheets()) {
            examplesInCourse.addAll(exerciseSheet.getExamples());
        }
        List<Long> exampleIds = examplesInCourse.stream().map(example -> example.getId()).collect(Collectors.toList());
        List<FinishesExample> finishesExamples = finishesExampleRepository.findByExample_IdIn(exampleIds);
        finishesExamples.removeIf(finishesExample -> EFinishesExampleState.NO.equals(finishesExample.getState()));

        for (FinishesExample finishesExample : finishesExamples) {
            KreuzelCourseResponse kreuzelCourseResponse = new KreuzelCourseResponse();
            kreuzelCourseResponse.setExampleId(finishesExample.getExample().getId());
            kreuzelCourseResponse.setExampleName(finishesExample.getExample().getName());
            kreuzelCourseResponse.setExerciseSheetName(finishesExample.getExample().getExerciseSheet().getName());
            kreuzelCourseResponse.setForename(finishesExample.getUser().getForename());
            kreuzelCourseResponse.setSurname(finishesExample.getUser().getSurname());
            kreuzelCourseResponse.setMatriculationName(finishesExample.getUser().getMatriculationNumber());
            kreuzelCourseResponse.setState(finishesExample.getState());

            kreuzelCourseResponses.add(kreuzelCourseResponse);
        }
        return kreuzelCourseResponses;
    }

    protected List<FinishesExampleResponse> createFinishesExampleResponses(Example example, ExerciseSheet exerciseSheet)
    {
        List<FinishesExampleResponse> finishesExampleResponses = new ArrayList<>();
        for (FinishesExample finishesExample : example.getExamplesFinishedByUser()) {

            if(finishesExample.getHasPresented()) {
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

    @Transactional
    public CourseResponseObject copyCourse(CopyCourseRequest copyCourseRequest) throws ServiceValidationException {

        Semester semester = readSemester(copyCourseRequest.getSemesterId());
        Course originalCourse = readCourse(copyCourseRequest.getCourseId());

        // first copy course
        Course copiedCourse = originalCourse.copy();
        copiedCourse.setSemester(semester);
        courseRepository.save(copiedCourse);

        if (originalCourse.getExerciseSheets() == null || originalCourse.getExerciseSheets().isEmpty())
        {
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

            for (Example example : exerciseSheet.getExamples()) {

                // if true example is subExample and this is handled below
                if (example.getParentExample() != null)
                    continue;

                Example copiedExample = example.copy();
                copiedExample.setExerciseSheet(copiedExerciseSheet);
                exampleRepository.save(copiedExample);

                if (example.getSubExamples() != null) {
                    for (Example subExample : example.getSubExamples()) {
                        Example copiedSubExample = subExample.copy();
                        copiedSubExample.setExerciseSheet(copiedExerciseSheet);
                        copiedSubExample.setParentExample(copiedExample);
                        exampleRepository.save(copiedSubExample);
                        if (subExample.getSupportFileTypes() != null) {
                            copySupportFileType(subExample.getSupportFileTypes(), copiedSubExample);
                        }
                    }
                }

                if (example.getSupportFileTypes() == null)
                    continue;

                copySupportFileType(example.getSupportFileTypes(), copiedExample);
            }
        }
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(copiedCourse.getId());
        return responseObject;
    }

    protected  void copySupportFileType(Set<SupportFileType> supportFileTypes, Example copiedExample)
    {
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
