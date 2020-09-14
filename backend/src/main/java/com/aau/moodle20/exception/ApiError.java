package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors;
    private Integer errorResponseCode;


    public ApiError(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiError(HttpStatus status, String message, List<String> errors, Integer errorResponseCode) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.errorResponseCode = errorResponseCode;
    }

    public ApiError(HttpStatus status, String message, String error, Integer errorResponseCode) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
        this.errorResponseCode = errorResponseCode;
    }

    public ApiError(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Integer getErrorResponseCode() {
        return errorResponseCode;
    }

    public void setErrorResponseCode(Integer errorResponseCode) {
        this.errorResponseCode = errorResponseCode;
    }
}
