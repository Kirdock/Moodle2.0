package com.aau.moodle20.services;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class FinishesExampleService extends AbstractService{

    public void setKreuzelUser(List<UserKreuzelRequest> userKreuzelRequests) throws ServiceValidationException
    {
       UserDetailsImpl userDetails = getUserDetails();
       List<FinishesExample> finishesExampleList = new ArrayList<>();
       for(UserKreuzelRequest userKreuzelRequest: userKreuzelRequests)
       {
           Example example = readExample(userKreuzelRequest.getExampleId());
           if(!example.getSubExamples().isEmpty())
               throw new ServiceValidationException("Error: Example has sub-examples and can therefore not be kreuzelt"); 

           Optional<FinishesExample> optionalFinishesExample =  finishesExampleRepository
                   .findByExample_IdAndUser_MatriculationNumber(userKreuzelRequest.getExampleId(),
                           userDetails.getMatriculationNumber());
           //update
           if(optionalFinishesExample.isPresent())
           {
               FinishesExample finishesExample = optionalFinishesExample.get();
               finishesExample.setState(userKreuzelRequest.getState());
               finishesExample.setDescription(userKreuzelRequest.getDescription());
               finishesExampleList.add(finishesExample);
           }
           //Create
           else
           {
               FinishesExampleKey finishesExampleKey = new FinishesExampleKey(userDetails.getMatriculationNumber(),userKreuzelRequest.getExampleId());
               FinishesExample finishesExample = new FinishesExample();
               finishesExample.setId(finishesExampleKey);
               finishesExample.setExample(new Example(userKreuzelRequest.getExampleId()));
               finishesExample.setUser(new User(userDetails.getMatriculationNumber()));
               finishesExample.setDescription(userKreuzelRequest.getDescription());
               finishesExample.setState(userKreuzelRequest.getState());
               finishesExample.setRemainingUploadCount(example.getExerciseSheet().getUploadCount());
               finishesExampleList.add(finishesExample);
           }
       }
        finishesExampleRepository.saveAll(finishesExampleList);
    }

    public void setKreuzelUserAttachment(MultipartFile file, Long exampleId) throws ServiceValidationException, IOException {
        UserDetailsImpl userDetails = getUserDetails();
        readExample(exampleId);
        FinishesExample finishesExample = readFinishesExample(exampleId, userDetails.getMatriculationNumber());

        finishesExample.setAttachment(file.getBytes());
        finishesExample.setFileName(file.getOriginalFilename());
        if (finishesExample.getRemainingUploadCount() > 0)
            finishesExample.setRemainingUploadCount(finishesExample.getRemainingUploadCount() - 1);
        finishesExampleRepository.save(finishesExample);
    }

    public FinishesExample getKreuzelAttachment(Long exampleId) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        return readFinishesExample(exampleId, userDetails.getMatriculationNumber());
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
    public List<ExampleResponseObject> getFinishedExamplesUserCourse(String matriculationNumber, Long courseId) throws ServiceValidationException {
        List<ExampleResponseObject> responseObjects = new ArrayList<>();

        User user = readUser(matriculationNumber);
        Course course = readCourse(courseId);

        if (!isAdmin() && !isOwner(course))
            throw new ServiceValidationException("Error: Not admin or Course Owner!", HttpStatus.UNAUTHORIZED);

        for (ExerciseSheet exerciseSheet : course.getExerciseSheets()) {
            for (Example example : exerciseSheet.getExamples()) {
                Optional<FinishesExample> optFinishesExample = example.getExamplesFinishedByUser().stream()
                        .filter(finishesExample -> finishesExample.getUser().getMatriculationNumber().equals(user.getMatriculationNumber()))
                        .findFirst();
                if (optFinishesExample.isPresent() &&
                        (EFinishesExampleState.MAYBE.equals(optFinishesExample.get().getState()) || (EFinishesExampleState.YES.equals(optFinishesExample.get().getState())))) {
                    ExampleResponseObject responseObject = new ExampleResponseObject();
                    responseObject.setId(optFinishesExample.get().getExample().getId());
                    responseObject.setName(optFinishesExample.get().getExample().getName());
                    responseObject.setSubExamples(null);

                    responseObjects.add(responseObject);
                }
            }
        }
        responseObjects.sort(Comparator.comparing(ExampleResponseObject::getName));

        return responseObjects;
    }
}
