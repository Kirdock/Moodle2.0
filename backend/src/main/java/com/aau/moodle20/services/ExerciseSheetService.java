package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    ExampleRepository exampleRepository;

    @Autowired
    SupportFileTypeRepository supportFileTypeRepository;

    @Autowired
    FileTypeRepository fileTypeRepository;


    public void createExerciseSheet(CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        Long courseId = createExerciseSheetRequest.getCourseId();

        if (!courseRepository.existsById(courseId))
            throw new ServiceValidationException("Error: the referenced course does not exists", ApiErrorResponseCodes.COURSE_NOT_FOUND, HttpStatus.NOT_FOUND);

        ExerciseSheet exerciseSheet = new ExerciseSheet();
        exerciseSheet.setCourse(new Course(createExerciseSheetRequest.getCourseId()));
        exerciseSheet.setMinKreuzel(createExerciseSheetRequest.getMinKreuzel());
        exerciseSheet.setMinPoints(createExerciseSheetRequest.getMinPoints());
        exerciseSheet.setName(createExerciseSheetRequest.getName());
        exerciseSheet.setSubmissionDate(createExerciseSheetRequest.getSubmissionDate());
        exerciseSheet.setIssueDate(createExerciseSheetRequest.getIssueDate());
        exerciseSheet.setDescription(createExerciseSheetRequest.getDescription());

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

        exerciseSheetRepository.save(exerciseSheet);
    }

    public ExerciseSheetResponseObject getExerciseSheet(Long id) throws ServiceValidationException {
        if (!exerciseSheetRepository.existsById(id))
            throw new EntityNotFoundException("Exercise Sheet not found");
        Optional<ExerciseSheet> exerciseSheetOptional = exerciseSheetRepository.findById(id);
        return exerciseSheetOptional.get().getResponseObject();
    }

    public void deleteExerciseSheet(Long id) throws EntityNotFoundException {
        if (!exerciseSheetRepository.existsById(id))
            throw new EntityNotFoundException("Exercise Sheet not found");
        exerciseSheetRepository.deleteById(id);
    }


    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(Long courseId) throws ServiceValidationException {
        if (!courseRepository.existsById(courseId))
            throw new EntityNotFoundException("Course not found");
        List<ExerciseSheet> exerciseSheets = exerciseSheetRepository.findByCourse_Id(courseId);
        return exerciseSheets.stream().map(ExerciseSheet::getResponseObject).collect(Collectors.toList());
    }

    @Transactional
    public void createExample (CreateExampleRequest createExampleRequest) throws ServiceValidationException
    {
        if (createExampleRequest.getSubExamples() == null || createExampleRequest.getSubExamples().isEmpty()) {
            if (createExampleRequest.getPoints() == null)
                throw new ServiceValidationException("Error: points must not be null");
            if (createExampleRequest.getWeighting() == null)
                throw new ServiceValidationException("Error: weighting must not be null");
        } else {
            if (createExampleRequest.getDescription() == null)
                throw new ServiceValidationException("Error: Description must not be null");
        }


        List<SupportFileType> supportFileTypes;
        Example example = new Example();
        example.fillValuesFromRequestObject(createExampleRequest);
        exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example,createExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);

        if(createExampleRequest.getSubExamples()!=null && !createExampleRequest.getSubExamples().isEmpty())
        {
            for(CreateExampleRequest subExampleRequest: createExampleRequest.getSubExamples())
            {
                Example subExample = new Example();
                subExample.fillValuesFromRequestObject(subExampleRequest);
                subExample.setParentExample(example);
                exampleRepository.save(subExample);

                supportFileTypes = createSupportedFileTypesEntries(subExample,subExampleRequest);
                supportFileTypeRepository.saveAll(supportFileTypes);

            }
        }
    }


    protected List<SupportFileType> createSupportedFileTypesEntries(Example example, CreateExampleRequest createExampleRequest)
    {
        List<SupportFileType> supportFileTypes = new ArrayList<>();
        for (Long fileTypeId : createExampleRequest.getSupportedFileTypes()) {
            Optional<FileType> optionalFileType = fileTypeRepository.findById(fileTypeId);
            if (optionalFileType.isPresent()) {
                SupportFileTypeKey supportFileTypeKey = new SupportFileTypeKey();
                supportFileTypeKey.setExampleId(example.getId());
                supportFileTypeKey.setFileTypeId(optionalFileType.get().getId());

                SupportFileType supportFileType = new SupportFileType();
                supportFileType.setId(supportFileTypeKey);
                supportFileType.setExample(example);
                supportFileType.setFileType(optionalFileType.get());

                supportFileTypes.add(supportFileType);
            }
        }
        return supportFileTypes;
    }
}
