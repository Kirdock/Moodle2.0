package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzeMultilRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.KreuzelResponse;
import com.aau.moodle20.payload.response.ViolationHistoryResponse;
import com.aau.moodle20.validation.ValidatorHandler;
import org.apache.maven.cli.MavenCli;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import validation.IValidator;
import validation.Violation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinishesExampleService extends AbstractService {

    private ValidatorHandler validatorHandler;

    public FinishesExampleService(ValidatorHandler validatorHandler)
    {
        this.validatorHandler = validatorHandler;
    }

    @Transactional
    public void setKreuzelUser(List<UserKreuzelRequest> userKreuzelRequests) {
        UserDetailsImpl userDetails = getUserDetails();
        for (UserKreuzelRequest userKreuzelRequest : userKreuzelRequests) {
            if (!isAssignedToCourse(userKreuzelRequest.getExampleId(), userDetails.getMatriculationNumber()))
                throw new ServiceException("Error: Access denied", HttpStatus.FORBIDDEN);

            updateOrCreateUserKreuzel(userKreuzelRequest.getExampleId(), userDetails.getMatriculationNumber(), userKreuzelRequest.getState(), userKreuzelRequest.getDescription(), false);
        }
    }

    @Transactional
    public void setKreuzelUserMulti(List<UserKreuzeMultilRequest> userKreuzeMultilRequests) {
        UserDetailsImpl userDetails = getUserDetails();
        for (UserKreuzeMultilRequest userKreuzeMultilRequest : userKreuzeMultilRequests) {
            Course course = readExample(userKreuzeMultilRequest.getExampleId()).getExerciseSheet().getCourse();
            if (!userDetails.getAdmin() && !isOwner(course))
                throw new ServiceException("Error: Access denied", HttpStatus.FORBIDDEN);

            updateOrCreateUserKreuzel(userKreuzeMultilRequest.getExampleId(), userKreuzeMultilRequest.getMatriculationNumber(), userKreuzeMultilRequest.getState(), null, true);
        }
    }


    protected void updateOrCreateUserKreuzel(Long exampleId, String matriculationNumber, EFinishesExampleState state, String description, boolean kreuzelMulti) {
        FinishesExample finishesExample = null;
        Example example = readExample(exampleId);
        User user = readUser(matriculationNumber);
        if (!example.getSubExamples().isEmpty())
            throw new ServiceException("Error: Example has sub-examples and can therefore not be kreuzelt");

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
    public ViolationHistoryResponse setKreuzelUserAttachment(MultipartFile file, Long exampleId) throws IOException, ClassNotFoundException {
        List<? extends Violation> violations;
        UserDetailsImpl userDetails = getUserDetails();
        Example example = readExample(exampleId);

        if (Boolean.FALSE.equals(example.getSubmitFile()))
            throw new ServiceException("Error: not submit file for this example");
        if (file.isEmpty())
            throw new ServiceException("Error: given file is empty");
        if (!isAssignedToCourse(exampleId, userDetails.getMatriculationNumber()))
            throw new ServiceException("Error: Not assigned to this course", HttpStatus.FORBIDDEN);


        if (!finishesExampleRepository.existsByExample_IdAndUser_MatriculationNumber(exampleId, userDetails.getMatriculationNumber())) {
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
            throw new ServiceException("Error: max upload counts reached!");

        saveFileToDisk(file, example);
        String filePath = createUserExampleAttachmentDir(example) + "/" + file.getOriginalFilename();
        violations = executeValidator(filePath, example);

        finishesExample.setFileName(file.getOriginalFilename());
        if (finishesExample.getRemainingUploadCount() > 0)
            finishesExample.setRemainingUploadCount(finishesExample.getRemainingUploadCount() - 1);
        finishesExampleRepository.saveAndFlush(finishesExample);

        ViolationHistory violationHistory = new ViolationHistory();
        violationHistory.setViolations(createViolationEntities(violations));
        violationHistory.setDate(LocalDateTime.now());
        violationHistoryRepository.saveAndFlush(violationHistory);

        violationHistory.setFinishesExample(finishesExample);
        violationHistory.getViolations().forEach(violation -> violation.setViolationHistory(violationHistory));

        violationHistoryRepository.save(violationHistory);

        return violationHistory.createViolationHistoryResponse();
    }


    protected Set<ViolationEntity> createViolationEntities(List<? extends Violation> violations) {
        Set<ViolationEntity> violationEntities = new HashSet<>();
        for (Violation violation : violations) {
            ViolationEntity entity = new ViolationEntity();
            entity.setResult(violation.getResult());
            violationEntities.add(entity);
        }
        return violationEntities;
    }

    protected List<? extends Violation> executeValidator(String filePath, Example example) throws IOException, ClassNotFoundException {
        List<? extends Violation> violations = new ArrayList<>();
        String validatorDir = FileConstants.VALIDATOR_DIR + createExampleAttachmentDir(example);
        validatorDir = validatorDir + "/" + example.getValidator();

        IValidator validator = validatorHandler.loadValidator(validatorDir);
        if (validator != null)
            violations = validator.validate(filePath);

        validator = null;
        return violations;
    }
    
    protected void saveFileToDisk(MultipartFile file, Example example) throws IOException {
        String filePath = createUserExampleAttachmentDir(example);
        File directory = new File(filePath);
        if (directory.exists())
            FileUtils.cleanDirectory(directory);

        saveFile(filePath, file);
    }

    protected byte[] readFileFromDisk(FinishesExample finishesExample) throws IOException {
        String filePath = createUserExampleAttachmentDir(finishesExample.getExample());
        Path path = Paths.get(filePath + "/" + finishesExample.getFileName());

        return Files.readAllBytes(path);
    }

    public void deleteFinishExampleData(FinishesExample finishesExample) throws IOException {
        if (finishesExample.getFileName() == null)
            return;

        String userDir = "/" + finishesExample.getId().getMatriculationNumber();
        String filePath = FileConstants.ATTACHMENTS_DIR + createExampleAttachmentDir(finishesExample.getExample()) + userDir;
        Path path = Paths.get(filePath + "/" + finishesExample.getFileName());

        Files.deleteIfExists(path);
    }

    protected String createUserExampleAttachmentDir(Example example) {
        String userDir = "/" + getUserDetails().getMatriculationNumber();
        return FileConstants.ATTACHMENTS_DIR + createExampleAttachmentDir(example) + userDir;
    }

    public FinishesExample getKreuzelAttachment(Long exampleId) {
        UserDetailsImpl userDetails = getUserDetails();
        FinishesExample finishesExample = readFinishesExample(exampleId, userDetails.getMatriculationNumber());
        try {
            finishesExample.setAttachmentContent(readFileFromDisk(finishesExample));
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }

        return finishesExample;
    }

    public void setUserExamplePresented(UserExamplePresentedRequest userExamplePresented) {
        readExample(userExamplePresented.getExampleId()); // checks if example exits
        readUser(userExamplePresented.getMatriculationNumber()); // checks if user exists
        FinishesExample finishesExample = readFinishesExample(userExamplePresented.getExampleId(), userExamplePresented.getMatriculationNumber());
        finishesExample.setHasPresented(userExamplePresented.getHasPresented());
        finishesExampleRepository.save(finishesExample);
    }

    protected FinishesExample readFinishesExample(Long exampleId, String matriculationNumber) {
        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository
                .findByExample_IdAndUser_MatriculationNumber(exampleId, matriculationNumber);
        if (!optionalFinishesExample.isPresent())
            throw new ServiceException("Error: user did not check this example");

        return optionalFinishesExample.get();
    }

    /**
     * returns all examples which the given user finihsed in given course
     *
     * @param matriculationNumber
     * @param courseId
     * @return list of example id and name
     */
    public List<KreuzelResponse> getKreuzelUserCourse(String matriculationNumber, Long courseId) {
        List<KreuzelResponse> responseObjects = new ArrayList<>();

        User user = readUser(matriculationNumber);
        Course course = readCourse(courseId);

        // check permission
        if (!isAdmin() && !isOwner(course))
            throw new ServiceException("Error: Not admin or Course Owner!", HttpStatus.FORBIDDEN);

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
     *
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

    protected boolean isAssignedToCourse(Long exampleId, String matriculationNumber) {
        boolean hasPermission = false;
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        Course course = readExample(exampleId).getExerciseSheet().getCourse();
        Long courseId = course.getId();
        if (optionalUser.isPresent()) {
            hasPermission = optionalUser.get().getCourses().stream()
                    .filter(userInCourse -> !ECourseRole.NONE.equals(userInCourse.getRole()))
                    .anyMatch(userInCourse -> userInCourse.getId().getCourseId().equals(courseId));
        }
        return hasPermission;
    }
}
