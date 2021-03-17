package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ServiceException extends RuntimeException {

    private final Integer errorResponseCode;
    private final List<String> errors;
    private final HttpStatus httpStatus;

    public ServiceException(String message, Throwable throwable, Integer errorResponseCode, List<String> errors, HttpStatus status) {
        super(message, throwable);
        this.errorResponseCode = errorResponseCode;
        this.httpStatus = status;
        this.errors = errors;
    }

    public Integer getErrorResponseCode() {
        return errorResponseCode;
    }


    public List<String> getErrors() {
        return errors;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
