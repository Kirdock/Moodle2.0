package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SemesterAlreadyCreatedException extends RuntimeException {

    public SemesterAlreadyCreatedException()
    {
    }

    public SemesterAlreadyCreatedException(String message)
    {
        super(message);
    }
}
