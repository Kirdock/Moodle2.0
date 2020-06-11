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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExampleService extends AbstractService{

    @Transactional
    public ExampleResponseObject createExample (ExampleRequest createExampleRequest) throws ServiceValidationException
    {
        List<SupportFileType> supportFileTypes;
        Example example = new Example();

        example.fillValuesFromRequestObject(createExampleRequest);
        exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example,createExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);

        // if this is an subExample remove all finishesExample entries of parentExample
        if(createExampleRequest.getParentId()!=null)
        {
            List<FinishesExample> finishesExamples = finishesExampleRepository.findByExample_Id(createExampleRequest.getParentId());
            finishesExampleRepository.deleteAll(finishesExamples);
        }

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
    public void updateExample(ExampleRequest updateExampleRequest) throws ServiceValidationException {
        List<SupportFileType> supportFileTypes;
        if (updateExampleRequest.getSubmitFile() != null && updateExampleRequest.getSubmitFile()) {
            if (updateExampleRequest.getSupportedFileTypes() == null || updateExampleRequest.getSupportedFileTypes().isEmpty())
                throw new ServiceValidationException("Error: supported file types must not be null!");
        }
        Example example = readExample(updateExampleRequest.getId());
        example.fillValuesFromRequestObject(updateExampleRequest);
        supportFileTypes = createSupportedFileTypesEntries(example, updateExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);
        example.setSupportFileTypes(new HashSet<>(supportFileTypes));
        exampleRepository.saveAndFlush(example);
    }

    @Transactional
    public void deleteExample(Long exampleId) throws ServiceValidationException {
        Example example = readExample(exampleId);
        ExerciseSheet exerciseSheet = example.getExerciseSheet();
        Example parentExample = example.getParentExample();
        exampleRepository.deleteById(exampleId);
        exampleRepository.flush();

        // remove order gap caused by delete of example
        List<Example> examples = null;
        if (parentExample != null)
            examples = new ArrayList<>(parentExample.getSubExamples());
        else {
            examples = exerciseSheet.getExamples().stream()
                    .filter(example1 -> example1.getParentExample() == null).collect(Collectors.toList());
        }
        examples.sort(Comparator.comparing(Example::getOrder));
        for (int i = 0; i < examples.size(); i++)
            examples.get(i).setOrder(i);

        exampleRepository.saveAll(examples);
        exampleRepository.flush();
    }

    public List<FileTypeResponseObject> getFileTypes() {
        return fileTypeRepository.findAll().stream().map(FileType::createFileTypeResponseObject).collect(Collectors.toList());
    }

    public ExampleResponseObject getExample(Long id) throws ServiceValidationException {
        Example example = readExample(id);
        return example.createExampleResponseObject(null);
    }

    public void updateExampleOrder(List<ExampleOrderRequest> exampleOrderRequests) throws ServiceValidationException {
        for (ExampleOrderRequest exampleOrderRequest : exampleOrderRequests) {
            Example example = readExample(exampleOrderRequest.getId());
            example.setOrder(exampleOrderRequest.getOrder());
            exampleRepository.save(example);
        }
    }
}
