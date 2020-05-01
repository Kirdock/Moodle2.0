package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SemesterException extends RuntimeException {

    public SemesterException()
    {
    }

    public SemesterException(String message)
    {
        super(message);
    }
}
