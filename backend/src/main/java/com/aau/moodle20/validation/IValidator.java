package com.aau.moodle20.validation;

import com.aau.moodle20.entity.Violation;

import java.util.List;

public interface IValidator {

    List<? extends Violation> validate(String fileDir);
}
