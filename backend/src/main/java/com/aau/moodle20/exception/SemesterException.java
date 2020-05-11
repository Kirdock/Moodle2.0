package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class SemesterException extends RuntimeException {

    public SemesterException()
    {
    }

    public SemesterException(String message)
    {
        super(message);
    }
}
