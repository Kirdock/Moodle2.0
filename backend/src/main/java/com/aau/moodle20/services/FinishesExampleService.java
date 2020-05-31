package com.aau.moodle20.services;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.FinishesExampleRepository;
import com.aau.moodle20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FinishesExampleService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ExampleRepository exampleRepository;

    @Autowired
    FinishesExampleRepository finishesExampleRepository;



    public void setKreuzelUser(UserKreuzelRequest userKreuzelRequest) throws ServiceValidationException
    {
        if(!exampleRepository.existsById(userKreuzelRequest.getExampleId()))
            throw new ServiceValidationException("Error: Example does not exists!", HttpStatus.NOT_FOUND);
       UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        FinishesExampleKey finishesExampleKey = new FinishesExampleKey(userDetails.getMatriculationNumber(),userKreuzelRequest.getExampleId());
        FinishesExample finishesExample = new FinishesExample();
        finishesExample.setId(finishesExampleKey);
        finishesExample.setExample(new Example(userKreuzelRequest.getExampleId()));
        finishesExample.setUser(new User(userDetails.getMatriculationNumber()));
        finishesExample.setDescription(userKreuzelRequest.getDescription());
        finishesExample.setState(userKreuzelRequest.getType());

        finishesExampleRepository.save(finishesExample);
    }

}
