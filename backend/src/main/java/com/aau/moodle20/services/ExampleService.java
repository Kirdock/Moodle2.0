package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExampleService {

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


    @Transactional
    public ExampleResponseObject createExample (CreateExampleRequest createExampleRequest) throws ServiceValidationException
    {
        List<SupportFileType> supportFileTypes;
        Example example = new Example();
        if(createExampleRequest.getExerciseSheetId()==null)
            throw new ServiceValidationException("error: exerciseSheet must not be null");

        validateExampleRequest(createExampleRequest);
        example.setExerciseSheet(new ExerciseSheet(createExampleRequest.getExerciseSheetId()));
        example.fillValuesFromRequestObject(createExampleRequest);
        exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example,createExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);
        example.setSupportFileTypes(new HashSet<>(supportFileTypes));



        return example.createExampleResponseObject();
    }


    protected List<SupportFileType> createSupportedFileTypesEntries(Example example, AbstractExampleRequest abstractExampleRequest)
    {
        List<SupportFileType> supportFileTypes = new ArrayList<>();
        for (Long fileTypeId : abstractExampleRequest.getSupportedFileTypes()) {
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

    @Transactional
    public ExampleResponseObject updateExample(UpdateExampleRequest updateExampleRequest) throws ServiceValidationException {
       //TODO refactor

        List<SupportFileType> supportFileTypes;
        validateExampleRequest(updateExampleRequest);
        List<Long> currentSubExamplesIds =new ArrayList<>();
        if (updateExampleRequest.getSubmitFile()) {
            if (updateExampleRequest.getSupportedFileTypes() == null || updateExampleRequest.getSupportedFileTypes().isEmpty())
                throw new ServiceValidationException("Error: supported file types must not be null!");
            if (updateExampleRequest.getValidator() == null || updateExampleRequest.getValidator().isEmpty())
                throw new ServiceValidationException("Error: validator must not be null!");
        }

        Optional<Example> optionalExample = exampleRepository.findById(updateExampleRequest.getId());

        if (!optionalExample.isPresent())
            throw new ServiceValidationException("Error: example not found", HttpStatus.NOT_FOUND);

        Example example = optionalExample.get();
        example.fillValuesFromRequestObject(updateExampleRequest);
        supportFileTypes = createSupportedFileTypesEntries(example, updateExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);
        example.setSupportFileTypes(new HashSet<>(supportFileTypes));
        exampleRepository.saveAndFlush(example);

        if (updateExampleRequest.getSubExamples() != null && !updateExampleRequest.getSubExamples().isEmpty()) {
            for (UpdateExampleRequest subExampleRequest : updateExampleRequest.getSubExamples()) {
                // update subexample
                if (subExampleRequest.getId() != null) {
                    Optional<Example> optionalSubExample = exampleRepository.findById(subExampleRequest.getId());

                    if (!optionalSubExample.isPresent())
                        throw new ServiceValidationException("Error: subExample not found", HttpStatus.NOT_FOUND);

                    Example subExample = optionalSubExample.get();
                    subExample.fillValuesFromRequestObject(subExampleRequest);
                    supportFileTypes = createSupportedFileTypesEntries(subExample, subExampleRequest);
                    supportFileTypeRepository.saveAll(supportFileTypes);
                    subExample.setSupportFileTypes(new HashSet<>(supportFileTypes));
                    subExample.setExerciseSheet(example.getExerciseSheet());
                    exampleRepository.saveAndFlush(subExample);
                    currentSubExamplesIds.add(subExample.getId());
                }
                // create subexample
                else {
                    Example subExample = new Example();
                    subExample.fillValuesFromRequestObject(subExampleRequest);
                    subExample.setParentExample(example);
                    subExample.setExerciseSheet(example.getExerciseSheet());
                    exampleRepository.saveAndFlush(subExample);

                    supportFileTypes = createSupportedFileTypesEntries(subExample, subExampleRequest);
                    supportFileTypeRepository.saveAll(supportFileTypes);
                    currentSubExamplesIds.add(subExample.getId());
                }
            }
        }

        // delete sub examples
        List<Example> allSubExample = exampleRepository.findByParentExample_id(example.getId());
        List<Long> allSubExamplesInDatabaseIds = allSubExample.stream().map(Example::getId).collect(Collectors.toList());
        allSubExamplesInDatabaseIds.removeIf(currentSubExamplesIds::contains);

        for (Long id : allSubExamplesInDatabaseIds) {
            example.getSubExamples().stream()
                    .filter(example1 -> id.equals(example1.getId()))
                    .forEach(example1 -> example1.setParentExample(null));
            exampleRepository.deleteById(id);
        }


        return exampleRepository.findById(example.getId()).get().createExampleResponseObject();
    }

    protected void validateExampleRequest(AbstractExampleRequest abstractExampleRequest) throws ServiceValidationException
    {
        if (((IExampleRequest) abstractExampleRequest).getSubExamples() == null || ((IExampleRequest) abstractExampleRequest).getSubExamples().isEmpty()) {
            if (abstractExampleRequest.getPoints() == null)
                throw new ServiceValidationException("Error: points must not be null");
            if (abstractExampleRequest.getWeighting() == null)
                throw new ServiceValidationException("Error: weighting must not be null");
        } else {
            if (abstractExampleRequest.getDescription() == null)
                throw new ServiceValidationException("Error: Description must not be null");
        }
    }

    public void deleteExample(Long exampleId) throws ServiceValidationException
    {
        if (!exampleRepository.existsById(exampleId))
            throw new ServiceValidationException("Example not found",HttpStatus.NOT_FOUND);
        exampleRepository.deleteById(exampleId);
    }

    public List<FileTypeResponseObject> getFileTypes()
    {
        return fileTypeRepository.findAll().stream().map(FileType::createFileTypeResponseObject).collect(Collectors.toList());
    }

    public ExampleResponseObject getExample(Long id) throws ServiceValidationException
    {
        if(!exampleRepository.existsById(id))
            throw new ServiceValidationException("Error: example not found!",HttpStatus.NOT_FOUND);
        return exampleRepository.findById(id).get().createExampleResponseObject();
    }
}
