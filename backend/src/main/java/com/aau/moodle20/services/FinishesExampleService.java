package com.aau.moodle20.services;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzeMultilRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.KreuzelResponse;
import com.aau.moodle20.validation.IValidator;
import com.aau.moodle20.validation.ValidatorLoader;
import com.aau.moodle20.validation.Violation;
import org.apache.maven.cli.MavenCli;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class FinishesExampleService extends AbstractService{

    @Transactional
    public void setKreuzelUser(List<UserKreuzelRequest> userKreuzelRequests) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        for (UserKreuzelRequest userKreuzelRequest : userKreuzelRequests) {
            updateOrCreateUserKreuzel(userKreuzelRequest.getExampleId(), userDetails.getMatriculationNumber(), userKreuzelRequest.getState(), userKreuzelRequest.getDescription(), false);
        }
    }

    @Transactional
    public void setKreuzelUserMulti(List<UserKreuzeMultilRequest> userKreuzeMultilRequests) throws ServiceValidationException
    {
        for(UserKreuzeMultilRequest userKreuzeMultilRequest: userKreuzeMultilRequests)
        {
            updateOrCreateUserKreuzel(userKreuzeMultilRequest.getExampleId(),userKreuzeMultilRequest.getMatriculationNumber(),userKreuzeMultilRequest.getState(),null, true);
        }
    }

    protected void updateOrCreateUserKreuzel(Long exampleId, String matriculationNumber, EFinishesExampleState state, String description, boolean kreuzelMulti) throws ServiceValidationException {
        FinishesExample finishesExample = null;
        Example example = readExample(exampleId);
        User user = readUser(matriculationNumber);
        if (!example.getSubExamples().isEmpty())
            throw new ServiceValidationException("Error: Example has sub-examples and can therefore not be kreuzelt");

        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository
                .findByExample_IdAndUser_MatriculationNumber(exampleId, matriculationNumber);
        //update
        if (optionalFinishesExample.isPresent()) {
            finishesExample = optionalFinishesExample.get();
            finishesExample.setState(state);
            if (!kreuzelMulti)
                finishesExample.setDescription(description);
        }
        //Create
        else {
            FinishesExampleKey finishesExampleKey = new FinishesExampleKey(matriculationNumber, exampleId);
            finishesExample = new FinishesExample();
            finishesExample.setId(finishesExampleKey);
            finishesExample.setExample(example);
            finishesExample.setUser(user);
            finishesExample.setDescription(description);
            finishesExample.setState(state);
            finishesExample.setRemainingUploadCount(example.getUploadCount());
        }

        finishesExampleRepository.save(finishesExample);
    }

    @Transactional
    public List<? extends Violation> setKreuzelUserAttachment(MultipartFile file, Long exampleId) throws ServiceValidationException, IOException, ClassNotFoundException {
        if (file.isEmpty())
            throw new ServiceValidationException("Error: given file is empty");

        List<? extends Violation> violations = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();
        Example example = readExample(exampleId);

        if(!finishesExampleRepository.existsByExample_IdAndUser_MatriculationNumber(exampleId,userDetails.getMatriculationNumber()))
        {
            List<UserKreuzelRequest> requests = new ArrayList<>();
            UserKreuzelRequest kreuzelRequest = new UserKreuzelRequest();
            kreuzelRequest.setDescription(null);
            kreuzelRequest.setExampleId(exampleId);
            kreuzelRequest.setState(EFinishesExampleState.NO);
            requests.add(kreuzelRequest);
            setKreuzelUser(requests);
            finishesExampleRepository.flush();
        }

        FinishesExample finishesExample = readFinishesExample(exampleId, userDetails.getMatriculationNumber());
        if (finishesExample.getRemainingUploadCount() == 0 && finishesExample.getExample().getUploadCount() != 0)
            throw new ServiceValidationException("Error: max upload counts reached!");

        saveFileToDisk(file, example);
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        String filePath = createUserExampleAttachmentDir(example) + "/" + file.getOriginalFilename();
        violations =  excectueValidator(filePath, example);

        finishesExample.setFileName(file.getOriginalFilename());
        if (finishesExample.getRemainingUploadCount() > 0)
            finishesExample.setRemainingUploadCount(finishesExample.getRemainingUploadCount() - 1);
        finishesExampleRepository.save(finishesExample);

        return violations;
    }

    protected List<? extends Violation> excectueValidator(String filePath, Example example) throws IOException, ClassNotFoundException {
        List<? extends Violation> violations = new ArrayList<>();
        String validatorDir = FileConstants.validatorDir + createExampleAttachmentDir(example);
        validatorDir = validatorDir + "/"+ example.getValidator();

        ValidatorLoader validationLoader = new ValidatorLoader();
        IValidator validator = validationLoader.loadValidator(validatorDir);
        if(validator!=null)
            violations = validator.validate(filePath);

        return violations; 

//        unzipMavenProject(filePath,new File(destDir));
//        installMavenProject(destDir);
//        executeValidator(destDir);
//        deleteMavenProject(destDir);
    }

    protected void installMavenProject(String destDir) throws ServiceValidationException
    {
        MavenCli cli = new MavenCli();
        int result = cli.doMain(new String[]{"clean", "install"}, destDir, System.out, System.out);
        if(result !=0)
            throw new ServiceValidationException("Error: maven project could not be build!");

//        InvocationRequest request = new DefaultInvocationRequest();
//        request.setPomFile( new File( "/path/to/pom.xml" ) );
//        request.setGoals( Collections.singletonList( "install" ) );
//
//        Invoker invoker = new DefaultInvoker();
//        invoker.execute( request );
    }

    protected void executeValidator(String destDir)
    {
        MavenCli cli = new MavenCli();
        int result = cli.doMain(new String[]{"test"}, destDir, System.out, System.out);
        if(result !=0)
            throw new ServiceValidationException("Error: maven project could not be tested!");

    }

    protected void deleteMavenProject(String destDir) throws IOException {
        File directory = new File(destDir);
        if(directory.exists())
            FileUtils.deleteDirectory(directory);
    }

    protected void unzipMavenProject(String zipFilePath, File destDir) throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            if (zipEntry.isDirectory()) {
                String subDir = destDir + "\\" + zipEntry.getName();
                File as = new File(subDir);
                as.mkdirs();
            } else {
                // Create new  file
                File newFile = new File(destDir, zipEntry.getName());
                String extractedDirectoryPath = destDir.getCanonicalPath();
                String extractedFilePath = newFile.getCanonicalPath();
                if (!extractedFilePath.startsWith(extractedDirectoryPath + File.separator))
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());

                BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                    while (inputStream.available() > 0) {
                        outputStream.write(inputStream.read());
                    }
                }
            }
        }
        zipFile.close();
    }

    protected void saveFileToDisk(MultipartFile file, Example example) throws IOException {
        String filePath = createUserExampleAttachmentDir(example);
        File directory = new File(filePath);
        if(directory.exists())
            FileUtils.cleanDirectory(directory);

        saveFile(filePath,file);
    }

    protected byte[] readFileFromDisk(FinishesExample  finishesExample) throws IOException {
        String filePath = createUserExampleAttachmentDir(finishesExample.getExample());
        Path path = Paths.get(filePath+"/"+finishesExample.getFileName());

        return Files.readAllBytes(path);
    }

    protected String createUserExampleAttachmentDir(Example example)
    {
        String userDir = "/" + getUserDetails().getMatriculationNumber();
        return FileConstants.attachmentsDir + createExampleAttachmentDir(example) + userDir;
    }

    public FinishesExample getKreuzelAttachment(Long exampleId) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        FinishesExample finishesExample = readFinishesExample(exampleId, userDetails.getMatriculationNumber());
        try {
            finishesExample.setAttachmentContent(readFileFromDisk(finishesExample));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceValidationException(e.getMessage());
        }

        return finishesExample;
    }

    public void setUserExamplePresented(UserExamplePresentedRequest userExamplePresented) throws ServiceValidationException {
        readExample(userExamplePresented.getExampleId()); // checks if example exits
        readUser(userExamplePresented.getMatriculationNumber()); // checks if user exists
        FinishesExample finishesExample = readFinishesExample(userExamplePresented.getExampleId(), userExamplePresented.getMatriculationNumber());
        finishesExample.setHasPresented(userExamplePresented.getHasPresented());
        finishesExampleRepository.save(finishesExample);
    }

    protected FinishesExample readFinishesExample(Long exampleId, String matriculationNumber) throws ServiceValidationException {
        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository
                .findByExample_IdAndUser_MatriculationNumber(exampleId, matriculationNumber);
        if (!optionalFinishesExample.isPresent())
            throw new ServiceValidationException("Error: user did not check this example");

        return optionalFinishesExample.get();
    }

    /**
     * returns all examples which the given user finihsed in given course
     * @param matriculationNumber
     * @param courseId
     * @return list of example id and name
     * @throws ServiceValidationException
     */
    public List<KreuzelResponse> getKreuzelUserCourse(String matriculationNumber, Long courseId) throws ServiceValidationException {
        List<KreuzelResponse> responseObjects = new ArrayList<>();

        User user = readUser(matriculationNumber);
        Course course = readCourse(courseId);

        // check permission
        if (!isAdmin() && !isOwner(course))
            throw new ServiceValidationException("Error: Not admin or Course Owner!", HttpStatus.UNAUTHORIZED);

        Comparator<ExerciseSheet> exerciseSheetComparator = Comparator.comparing(ExerciseSheet::getSubmissionDate).thenComparing(ExerciseSheet::getName);
        Comparator<Example> exampleComparator = Comparator.comparing(Example::getOrder);
        List<ExerciseSheet> sortedExerciseSheets = course.getExerciseSheets().stream().sorted(exerciseSheetComparator).collect(Collectors.toList());

        for (ExerciseSheet exerciseSheet : sortedExerciseSheets) {

            List<Example> notSubExamples = exerciseSheet.getExamples().stream()
                    .filter(example -> example.getParentExample() == null)
                    .sorted(exampleComparator).collect(Collectors.toList());

            KreuzelResponse kreuzelResponse = new KreuzelResponse();
            kreuzelResponse.setExerciseSheetName(exerciseSheet.getName());
            kreuzelResponse.setExamples(new ArrayList<>());

            // examples which are not sub examples
            for (Example example : notSubExamples) {

                if (!example.getSubExamples().isEmpty()) {
                    List<FinishesExampleResponse> finishesExamplesSubExamples = new ArrayList<>();
                    List<Example> sortedSubExamples = example.getSubExamples().stream().sorted(exampleComparator).collect(Collectors.toList());
                    for (Example subExample : sortedSubExamples) {
                        if (hasUserFinishedExample(subExample, user))
                            finishesExamplesSubExamples.add(createFinishedExampleResponse4UserKreuzel(subExample));
                    }
                    if (!finishesExamplesSubExamples.isEmpty()) {
                        kreuzelResponse.getExamples().addAll(finishesExamplesSubExamples);
                    }
                } else if (hasUserFinishedExample(example, user))
                    kreuzelResponse.getExamples().add(createFinishedExampleResponse4UserKreuzel(example));
            }
            if (!kreuzelResponse.getExamples().isEmpty())
                responseObjects.add(kreuzelResponse);
        }
        return responseObjects;
    }

    /**
     * checks if the given user has finished given example
     * @param example
     * @param user
     * @return True or False
     */
    protected Boolean hasUserFinishedExample(Example example, User user) {
        Optional<FinishesExample> optFinishesExample = example.getExamplesFinishedByUser().stream()
                .filter(finishesExample -> finishesExample.getUser().getMatriculationNumber().equals(user.getMatriculationNumber()))
                .findFirst();
        return optFinishesExample.isPresent() && !(EFinishesExampleState.NO.equals(optFinishesExample.get().getState()));
    }

    protected FinishesExampleResponse createFinishedExampleResponse4UserKreuzel(Example example) {
        FinishesExampleResponse responseObject = new FinishesExampleResponse();
        responseObject.setExampleName(example.getName());
        responseObject.setExampleId(example.getId());
        return responseObject;
    }
}
