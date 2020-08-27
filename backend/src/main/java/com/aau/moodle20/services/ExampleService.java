package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.ExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExampleService extends AbstractService{


    @Transactional
    public ExampleResponseObject createExample (ExampleRequest createExampleRequest) throws ServiceException, IOException {
        List<SupportFileType> supportFileTypes;
        Example example = new Example();
        Example parentExample = null;

        if (createExampleRequest.getSubmitFile() != null && createExampleRequest.getSubmitFile()) {
            if ((createExampleRequest.getSupportedFileTypes() == null || createExampleRequest.getSupportedFileTypes().isEmpty()) &&
                    (createExampleRequest.getCustomFileTypes() == null || createExampleRequest.getCustomFileTypes().isEmpty()))
                throw new ServiceException("Error:either supported file types or custom file types  must be specified!");
        }
        if(createExampleRequest.getParentId()!=null)
            parentExample = readExample(createExampleRequest.getParentId());

        ExerciseSheet exerciseSheet = readExerciseSheet(createExampleRequest.getExerciseSheetId());
        checkIfExampleNameAlreadyUsed(exerciseSheet,createExampleRequest.getName(),null,parentExample);

        // if this is an subExample remove all finishesExample entries of parentExample
        // and set points to 0
        if(createExampleRequest.getParentId()!=null)
        {
            List<FinishesExample> finishesExamples = finishesExampleRepository.findByExample_Id(createExampleRequest.getParentId());
            finishesExampleRepository.deleteAll(finishesExamples);
            parentExample.setPoints(0);
            if(parentExample.getValidator()!=null && !parentExample.getValidator().isEmpty())
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
        exampleRepository.save(example);
        supportFileTypes = createSupportedFileTypesEntries(example,createExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);

        ExampleResponseObject responseObject = new ExampleResponseObject();
        responseObject.setId(example.getId());
        responseObject.setSubExamples(null);

        return responseObject;
    }

    protected void checkIfExampleNameAlreadyUsed(ExerciseSheet exerciseSheet,String exampleName,Long exampleId, Example parentExample) throws ServiceException
    {
        if(parentExample==null)
        {
            List<Example> examples = exerciseSheet.getExamples().stream()
                    .filter(example -> example.getParentExample()==null)
                    .filter(example -> !example.getId().equals(exampleId)).collect(Collectors.toList());
            boolean alreadyExists = examples.stream().anyMatch(example -> example.getName().equals(exampleName));
            if(alreadyExists)
                throw new ServiceException("Error: example with this name already exists in exerciseSheet", ApiErrorResponseCodes.EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS);
        }else
        {
            boolean alreadyExists = parentExample.getSubExamples().stream()
                    .filter(example -> !example.getId().equals(exampleId))
                    .anyMatch(example -> example.getName().equals(exampleName));
            if(alreadyExists)
                throw new ServiceException("Error: sub example with this name already exists in exerciseSheet", ApiErrorResponseCodes.SUB_EXAMPLE_WITH_THIS_NAME_ALREADY_EXISTS);
        }
    }


    protected List<SupportFileType> createSupportedFileTypesEntries(Example example, ExampleRequest abstractExampleRequest) {
        List<SupportFileType> supportFileTypes = new ArrayList<>();
        if(abstractExampleRequest.getSupportedFileTypes()==null)
            return supportFileTypes;

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
    public void updateExample(ExampleRequest updateExampleRequest) throws ServiceException, IOException {
        List<SupportFileType> supportFileTypes;
        if (updateExampleRequest.getSubmitFile() != null && updateExampleRequest.getSubmitFile()) {
            if ((updateExampleRequest.getSupportedFileTypes() == null || updateExampleRequest.getSupportedFileTypes().isEmpty()) &&
                    (updateExampleRequest.getCustomFileTypes() == null || updateExampleRequest.getCustomFileTypes().isEmpty()))
                throw new ServiceException("Error:either supported file types or custom file types  must be specified!");
        }
        ExerciseSheet exerciseSheet = readExerciseSheet(updateExampleRequest.getExerciseSheetId());
        Example parentExample = updateExampleRequest.getParentId()!=null?readExample(updateExampleRequest.getParentId()):null;
        checkIfExampleNameAlreadyUsed(exerciseSheet,updateExampleRequest.getName(),updateExampleRequest.getId(),parentExample);

        Example example = readExample(updateExampleRequest.getId());
        if(!updateExampleRequest.getSubmitFile() && (example.getValidator()!=null && !example.getValidator().isEmpty()) )
            deleteExampleValidator(example.getId());

        example.fillValuesFromRequestObject(updateExampleRequest);
        supportFileTypes = createSupportedFileTypesEntries(example, updateExampleRequest);
        supportFileTypeRepository.saveAll(supportFileTypes);
        example.setSupportFileTypes(new HashSet<>(supportFileTypes));
        exampleRepository.saveAndFlush(example);
    }

    @Transactional
    public void deleteExample(Long exampleId) throws ServiceException, IOException {
        Example example = readExample(exampleId);
        ExerciseSheet exerciseSheet = example.getExerciseSheet();
        Example parentExample = example.getParentExample();
        if(example.getValidator()!=null && !example.getValidator().isEmpty())
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

    public ExampleResponseObject getExample(Long id) throws ServiceException {
        Example example = readExample(id);
        return example.createExampleResponseObject(null);
    }

    public void updateExampleOrder(List<ExampleOrderRequest> exampleOrderRequests) throws ServiceException {
        for (ExampleOrderRequest exampleOrderRequest : exampleOrderRequests) {
            Example example = readExample(exampleOrderRequest.getId());
            example.setOrder(exampleOrderRequest.getOrder());
            exampleRepository.save(example);
        }
    }

    public void setExampleValidator(MultipartFile validatorFile, Long exampleId) throws ServiceException, IOException {
        Example example = readExample(exampleId);
        String fileName = validatorFile.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(fileName!=null) {
            if (!FileConstants.JarFileExtension.equals(extension))
                throw new ServiceException("Error: Not a jar File!");
        }

        String validatorFilePath = getValidatorFilePath(example);
        clearDirectory(validatorFilePath);
        saveFile(validatorFilePath,validatorFile);
        example.setValidator(validatorFile.getOriginalFilename());
        example.setSubmitFile(Boolean.TRUE);
        exampleRepository.save(example);
    }

    protected void clearDirectory(String filePath) throws IOException
    {
        File fileToBeDeleted= new File(filePath);
        // if path does not exists create it
        if(fileToBeDeleted.exists()) {
            FileUtils.cleanDirectory(fileToBeDeleted);
        }
    }

    public Example getExampleValidator(Long exampleId) throws ServiceException, IOException {
        Example example = readExample(exampleId);
        String validatorFilePath = getValidatorFilePath(example);
        byte [] content = readFileFromDisk(validatorFilePath,example.getValidator());
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

    public byte [] getValidatorSkeleton() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        //simple file list, just for tests
        ArrayList<File> files = new ArrayList<>(2);
        files.add(new File(FileConstants.VALIDATOR_INTERFACE_PATH));
        files.add(new File(FileConstants.VIOLATION_PATH));

        for (File file : files) {
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, zipOutputStream);

            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        if (zipOutputStream != null) {
            zipOutputStream.finish();
            zipOutputStream.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void deleteExampleValidator(Long exampleId) throws ServiceException, IOException {
        Example example = readExample(exampleId);
        if(example.getValidator()==null || example.getValidator().isEmpty())
            return ;
        String validatorFilePath = getValidatorFilePath(example);
        boolean result = deleteFileFromDisk(validatorFilePath,example.getValidator());
        if(!result)
            throw new ServiceException("Error: Validator could not be deleted");


        example.setValidator(null);
        exampleRepository.save(example);
    }


    protected String getValidatorFilePath(Example example)
    {
        return FileConstants.validatorDir + createExampleAttachmentDir(example);
    }
}
