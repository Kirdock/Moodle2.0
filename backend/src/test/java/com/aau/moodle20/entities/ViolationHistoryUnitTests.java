package com.aau.moodle20.entities;

import com.aau.moodle20.entity.ViolationEntity;
import com.aau.moodle20.entity.ViolationHistory;
import com.aau.moodle20.payload.response.ViolationHistoryResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(SpringRunner.class)
public class ViolationHistoryUnitTests {

    @Test
    public void test_violationHistory_response()  {
        LocalDateTime date = LocalDateTime.now();
        ViolationHistory violationHistory = new ViolationHistory();
        violationHistory.setDate(date);

        ViolationEntity violationEntity1 = new ViolationEntity();
        violationEntity1.setResult("test1");
        violationEntity1.setId(200L);

        ViolationEntity violationEntity2 = new ViolationEntity();
        violationEntity2.setResult("test2");
        violationEntity2.setId(220L);

        violationHistory.setViolations(new HashSet<>());
        violationHistory.getViolations().add(violationEntity1);
        violationHistory.getViolations().add(violationEntity2);

        ViolationHistoryResponse response = violationHistory.createViolationHistoryResponse();

        assertEquals(date,response.getDate());
        assertEquals(violationEntity1.getResult(),response.getViolations().get(0).getResult());
        assertEquals(violationEntity2.getResult(),response.getViolations().get(1).getResult());
    }
}
