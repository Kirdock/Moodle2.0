package com.aau.moodle20.services;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.FinishesExampleRepository;
import com.aau.moodle20.repository.UserRepository;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FinishesExampleService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ExampleRepository exampleRepository;

    @Autowired
    FinishesExampleRepository finishesExampleRepository;



    public void setKreuzelUser(List<UserKreuzelRequest> userKreuzelRequests) throws ServiceValidationException
    {
       UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
               finishesExampleList.add(finishesExample);
           }
       }
        finishesExampleRepository.saveAll(finishesExampleList);
    }

    public void setKreuzelUserAttachment(MultipartFile file, Long exampleId) throws ServiceValidationException, IOException {
        UserDetailsImpl userDetails = getUserDetails();
        if(file == null)
            throw new ServiceValidationException("Error: file is null");
        Optional<Example> optionalExample = exampleRepository.findById(exampleId);
        if(!optionalExample.isPresent())
            throw new ServiceValidationException("Error: example not found",HttpStatus.NOT_FOUND);
        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(exampleId,userDetails.getMatriculationNumber());
        if(!optionalFinishesExample.isPresent())
            throw new ServiceValidationException("Error: user did not kreuzel this example");

        FinishesExample finishesExample = optionalFinishesExample.get();
        finishesExample.setAttachment(file.getBytes());
        finishesExample.setFileName(file.getOriginalFilename());
        finishesExampleRepository.save(finishesExample);
    }

    public FinishesExample getKreuzelAttachment(Long exampleId) throws ServiceValidationException
    {
        UserDetailsImpl userDetails = getUserDetails();
        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository.findByExample_IdAndUser_MatriculationNumber(exampleId,userDetails.getMatriculationNumber());
        if(!optionalFinishesExample.isPresent())
            throw new ServiceValidationException("Error: user did not kreuzel this example");

       return optionalFinishesExample.get();
    }

    public void setUserExamplePresented(UserExamplePresentedRequest userExamplePresented) throws ServiceValidationException
    {
        if(!exampleRepository.existsById(userExamplePresented.getExampleId()))
            throw new ServiceValidationException("Error: Example does not exists!", HttpStatus.NOT_FOUND);
        if(!userRepository.existsByMatriculationNumber(userExamplePresented.getMatriculationNumber()))
            throw new ServiceValidationException("Error: User does not exists!", HttpStatus.NOT_FOUND);

        Optional<FinishesExample> optionalFinishesExample = finishesExampleRepository
                .findByExample_IdAndUser_MatriculationNumber(userExamplePresented.getExampleId(), userExamplePresented.getMatriculationNumber());
        if(!optionalFinishesExample.isPresent())
            throw new ServiceValidationException("Error: user did not check this example");

        FinishesExample finishesExample = optionalFinishesExample.get();
        finishesExample.setHasPresented(userExamplePresented.getHasPresented());
        finishesExampleRepository.save(finishesExample);
    }

    public UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected Example readExample(Long exampleId) throws ServiceValidationException {
        Optional<Example> optionalExample = exampleRepository.findById(exampleId);
        if (!optionalExample.isPresent())
            throw new ServiceValidationException("Error: Example not found!", HttpStatus.NOT_FOUND);
        return optionalExample.get();
    }

}
