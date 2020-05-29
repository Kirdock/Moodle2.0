package com.aau.moodle20.services;

import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExampleResponseObject;
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
    public ExampleResponseObject createExample (ExampleRequest createExampleRequest) throws ServiceValidationException
    {
        List<SupportFileType> supportFileTypes;
        Example example = new Example();
        if(createExampleRequest.getExerciseSheetId()==null)
            throw new ServiceValidationException("error: exerciseSheet must not be null");

        example.fillValuesFromRequestObject(createExampleRequest);
        exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example,createExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);

        ExampleResponseObject responseObject = new ExampleResponseObject();
        responseObject.setId(example.getId());
        responseObject.setSubExamples(null);

        return responseObject;
    }


    protected List<SupportFileType> createSupportedFileTypesEntries(Example example, ExampleRequest abstractExampleRequest) {
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
    public ExampleResponseObject updateExample(ExampleRequest updateExampleRequest) throws ServiceValidationException {

        List<SupportFileType> supportFileTypes;

        if (updateExampleRequest.getSubmitFile()) {
            if (updateExampleRequest.getSupportedFileTypes() == null || updateExampleRequest.getSupportedFileTypes().isEmpty())
                throw new ServiceValidationException("Error: supported file types must not be null!");
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

        return example.createExampleResponseObject();
    }



    public void deleteExample(Long exampleId) throws ServiceValidationException
    {
        if (!exampleRepository.existsById(exampleId))
            throw new ServiceValidationException("Example not found",HttpStatus.NOT_FOUND);
        exampleRepository.deleteById(exampleId);
    }

    public List<FileTypeResponseObject> getFileTypes() {
        return fileTypeRepository.findAll().stream().map(FileType::createFileTypeResponseObject).collect(Collectors.toList());
    }

    public ExampleResponseObject getExample(Long id) throws ServiceValidationException {
        if (!exampleRepository.existsById(id))
            throw new ServiceValidationException("Error: example not found!", HttpStatus.NOT_FOUND);
        return exampleRepository.findById(id).get().createExampleResponseObject();
    }

    public void updateExampleOrder(List<ExampleOrderRequest> exampleOrderRequests) throws ServiceValidationException
    {
        for(ExampleOrderRequest exampleOrderRequest: exampleOrderRequests)
        {
            if(!exampleRepository.existsById(exampleOrderRequest.getId()))
                throw new ServiceValidationException("Error: Example with id:"+exampleOrderRequest.getId()+" not found!",HttpStatus.NOT_FOUND);
        }

        for(ExampleOrderRequest exampleOrderRequest: exampleOrderRequests)
        {
           Optional<Example> optionalExample = exampleRepository.findById(exampleOrderRequest.getId());
           if(optionalExample.isPresent())
           {
               Example example = optionalExample.get();
               example.setOrder(exampleOrderRequest.getOrder());
               exampleRepository.save(example);
           }
        }
    }
}
