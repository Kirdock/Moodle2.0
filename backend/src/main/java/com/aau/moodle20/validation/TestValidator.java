package com.aau.moodle20.validation;

import com.aau.moodle20.entity.Example;

import java.util.ArrayList;
import java.util.List;

public class TestValidator implements IValidator {


    public TestValidator()
    {
    }

    @Override
    public List<Violation> validate(Example example, String fileDir) {

        List<Violation> violations = new ArrayList<>();
        violations.add(new Violation("hallo"));

        return violations;
    }
}
