package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.validation.ValidatorHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExampleService extends AbstractService {

    private ValidatorHandler validatorHandler;

    public ExampleService(ValidatorHandler validatorHandler)
    {
        this.validatorHandler = validatorHandler;
    }

    @Transactional
    public ExampleResponseObject createExample(CreateExampleRequest createExampleRequest) throws IOException {
        List<SupportFileType> supportFileTypes;
        Example example = new Example();
        Example parentExample = null;

        if (createExampleRequest.getSubmitFile() != null && createExampleRequest.getSubmitFile()) {
            if ((createExampleRequest.getSupportedFileTypes() == null || createExampleRequest.getSupportedFileTypes().isEmpty()) &&
                    (createExampleRequest.getCustomFileTypes() == null || createExampleRequest.getCustomFileTypes().isEmpty()))
                throw new ServiceException("Error:either supported file types or custom file types  must be specified!");
        }
        if (createExampleRequest.getParentId() != null)
            parentExample = readExample(createExampleRequest.getParentId());

        ExerciseSheet exerciseSheet = readExerciseSheet(createExampleRequest.getExerciseSheetId());
        checkIfExampleNameAlreadyUsed(exerciseSheet, createExampleRequest.getName(), null, parentExample);

        // if this is an subExample remove all finishesExample entries of parentExample
        // and set points to 0
        if (createExampleRequest.getParentId() != null && parentExample != null) {
            List<FinishesExample> finishesExamples = finishesExampleRepository.findByExample_Id(createExampleRequest.getParentId());
            finishesExampleRepository.deleteAll(finishesExamples);
            parentExample.setPoints(0);
            if (parentExample.getValidator() != null && !parentExample.getValidator().isEmpty())
                deleteExampleValidator(parentExample.getId());

            parentExample.setSubmitFile(Boolean.FALSE);
            if (parentExample.getSupportFileTypes() != null)
                supportFileTypeRepository.deleteAll(parentExample.getSupportFileTypes());
            parentExample.setCustomFileTypes(null);

            exampleRepository.save(parentExample);
            exampleRepository.flush();
            supportFileTypeRepository.flush();
        }

        example.fillValuesFromRequestObject(createExampleRequest);
        example = exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example, createExampleRequest.getSupportedFileTypes());
        supportFileTypeRepository.saveAll(supportFileTypes);

        ExampleResponseObject responseObject = new ExampleResponseObject();
        responseObject.setId(example.getId());
        responseObject.setSubExamples(null);

        return responseObject;
    }

    protected void checkIfExampleNameAlreadyUsed(ExerciseSheet exerciseSheet, String exampleName, Long exampleId, Example parentExample) {
        if (parentExample == null) {
            List<Example> examples = exerciseSheet.getExamples().stream()
                    .filter(example -> example.getParentExample() == null)
                    .filter(example -> !example.getId().equals(exampleId)).collect(Collectors.toList());
            boolean alreadyExists = examples.stream().anyMatch(example -> example.getName().equals(exampleName));
            if (alreadyExists)
                throw new ServiceException("Error: example with this name already exists in exerciseSheet", ApiErrorResponseCodes.EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS);
        } else {
            boolean alreadyExists = parentExample.getSubExamples().stream()
                    .filter(example -> !example.getId().equals(exampleId))
                    .anyMatch(example -> example.getName().equals(exampleName));
            if (alreadyExists)
                throw new ServiceException("Error: sub example with this name already exists in exerciseSheet", ApiErrorResponseCodes.SUB_EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS);
        }
    }


    protected List<SupportFileType> createSupportedFileTypesEntries(Example example, List<Long> supportedFileTypes) {
        List<SupportFileType> supportFileTypes = new ArrayList<>();
        if (supportedFileTypes == null)
            return supportFileTypes;

        for (Long fileTypeId : supportedFileTypes) {
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
    public void updateExample(UpdateExampleRequest updateExampleRequest) throws IOException {
        List<SupportFileType> supportFileTypes;
        if (updateExampleRequest.getSubmitFile() != null && updateExampleRequest.getSubmitFile()) {
            if ((updateExampleRequest.getSupportedFileTypes() == null || updateExampleRequest.getSupportedFileTypes().isEmpty()) &&
                    (updateExampleRequest.getCustomFileTypes() == null || updateExampleRequest.getCustomFileTypes().isEmpty()))
                throw new ServiceException("Error:either supported file types or custom file types  must be specified!");
        }
        ExerciseSheet exerciseSheet = readExerciseSheet(updateExampleRequest.getExerciseSheetId());
        Example parentExample = updateExampleRequest.getParentId() != null ? readExample(updateExampleRequest.getParentId()) : null;
        checkIfExampleNameAlreadyUsed(exerciseSheet, updateExampleRequest.getName(), updateExampleRequest.getId(), parentExample);

        Example example = readExample(updateExampleRequest.getId());
        if ((updateExampleRequest.getSubmitFile() != null && !updateExampleRequest.getSubmitFile()) && (example.getValidator() != null && !example.getValidator().isEmpty()))
            deleteExampleValidator(example.getId());

        example.fillValuesFromRequestObject(updateExampleRequest);
        supportFileTypes = createSupportedFileTypesEntries(example, updateExampleRequest.getSupportedFileTypes());
        supportFileTypeRepository.saveAll(supportFileTypes);
        example.setSupportFileTypes(new HashSet<>(supportFileTypes));
        exampleRepository.saveAndFlush(example);
    }

    @Transactional
    public void deleteExample(Long exampleId) throws IOException {
        Example example = readExample(exampleId);
        ExerciseSheet exerciseSheet = example.getExerciseSheet();
        Example parentExample = example.getParentExample();
        if (example.getValidator() != null && !example.getValidator().isEmpty())
            deleteExampleValidator(exampleId);

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

    public ExampleResponseObject getExample(Long id) {
        Example example = readExample(id);
        return example.createExampleResponseObject(null);
    }

    @Transactional
    public void updateExampleOrder(List<ExampleOrderRequest> exampleOrderRequests) {
        for (ExampleOrderRequest exampleOrderRequest : exampleOrderRequests) {
            Example example = readExample(exampleOrderRequest.getId());
            example.setOrder(exampleOrderRequest.getOrder());
            exampleRepository.save(example);
        }
    }

    public void setExampleValidator(MultipartFile validatorFile, Long exampleId) throws IOException, ClassNotFoundException {
        Example example = readExample(exampleId);
        String fileName = validatorFile.getOriginalFilename();
        if (fileName != null) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!FileConstants.JAR_FILE_EXTENSION.equals(extension))
                throw new ServiceException("Error: Not a jar File!");
        }
        validatorHandler.checkValidatorFile(validatorFile);

        String validatorFilePath = getValidatorFilePath(example);
        clearDirectory(validatorFilePath);
        saveFile(validatorFilePath, validatorFile);
        example.setValidator(validatorFile.getOriginalFilename());
        example.setSubmitFile(Boolean.TRUE);
        exampleRepository.save(example);
    }

    protected void clearDirectory(String filePath) throws IOException {
        File fileToBeDeleted = new File(filePath);
        // if path does not exists create it
        if (fileToBeDeleted.exists()) {
            FileUtils.cleanDirectory(fileToBeDeleted);
        }
    }

    public Example getExampleValidator(Long exampleId) throws IOException {
        Example example = readExample(exampleId);
        String validatorFilePath = getValidatorFilePath(example);
        byte[] content = readFileFromDisk(validatorFilePath, example.getValidator());
        example.setValidatorContent(content);

        return example;
    }

    public void copyValidator(Example originalExample, Example copiedExample) throws IOException {
        if (originalExample.getValidator() == null || originalExample.getValidator().isEmpty())
            return;

        String originalValidatorFilePath = getValidatorFilePath(originalExample);
        String copiedValidatorFilePath = getValidatorFilePath(copiedExample);
        File source = new File(originalValidatorFilePath);
        File dest = new File(copiedValidatorFilePath);
        FileUtils.copyDirectory(source, dest);
        copiedExample.setValidator(originalExample.getValidator());
        exampleRepository.save(copiedExample);
    }

    public void deleteExampleValidator(Long exampleId) throws IOException {
        Example example = readExample(exampleId);
        if (example.getValidator() == null || example.getValidator().isEmpty())
            return;
        String validatorFilePath = getValidatorFilePath(example);
        deleteFileFromDisk(validatorFilePath, example.getValidator());

        example.setValidator(null);
        exampleRepository.save(example);
    }


    protected String getValidatorFilePath(Example example) {
        return FileConstants.VALIDATOR_DIR + createExampleAttachmentDir(example);
    }
}
