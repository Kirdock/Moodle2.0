package com.aau.moodle20.entities;

import com.aau.moodle20.entity.*;
import com.aau.moodle20.payload.response.ViolationResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(SpringRunner.class)
public class ViolationEntityUnitTests {

    @Test
    public void test_violationEntity_response()  {
        ViolationEntity violationEntity = new ViolationEntity();
        violationEntity.setResult("test");
        violationEntity.setId(200L);

        ViolationResponse violationResponse_expected = new ViolationResponse();
        violationResponse_expected.setResult("test");

        ViolationResponse response = violationEntity.createViolationResponse();
        assertEquals(violationResponse_expected.getResult(),response.getResult());
    }

}
