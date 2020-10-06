package com.aau.moodle20.entities;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.UserResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
public class UserEntityUnitTests {

    private final Long EXERCISE_SHEET_ID = 500L;
    private final Long EXAMPLE_ID = 340L;


    @Test
    public void creatUserResponseObject()  {
        User user = new User();
        user.setMatriculationNumber("12345678");
        user.setSurname("normal");
        user.setForename("normal");
        user.setAdmin(Boolean.FALSE);
        user.setCourses(new HashSet<>());
        user.setEmail("ppe@edu.aau.at");
        user.setPassword("password");

        UserResponseObject responseObject = user.createUserResponseObject();

        assertEquals(user.getUsername(),responseObject.getUsername());
        assertEquals(user.getSurname(),responseObject.getSurname());
        assertEquals(user.getForename(),responseObject.getForename());
        assertEquals(user.getMatriculationNumber(),responseObject.getMatriculationNumber());
        assertEquals(user.getEmail(),responseObject.getEmail());
        assertEquals(user.getAdmin(),responseObject.getIsAdmin());
    }

}
