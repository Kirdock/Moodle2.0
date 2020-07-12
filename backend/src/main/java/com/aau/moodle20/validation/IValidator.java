package com.aau.moodle20.validation;

import com.aau.moodle20.entity.Example;

import java.util.List;

public interface IValidator {

    List<Violation> validate(Example example, String fileDir);
}
