package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ServiceValidationException extends RuntimeException{

    private Integer errorResponseCode;
    private List<String> errors = new ArrayList<>();
    private HttpStatus httpStatus = null;
    public ServiceValidationException()
    {
    }

    public ServiceValidationException(String message)
    {
        super(message);
    }

    public ServiceValidationException(String message,Integer errorResponseCode)
    {
        super(message);
        this.errorResponseCode = errorResponseCode;
    }
    public ServiceValidationException(String message,Integer errorResponseCode,HttpStatus status)
    {
        super(message);
        this.errorResponseCode = errorResponseCode;
        this.httpStatus = status;
    }
    public Integer getErrorResponseCode() {
        return errorResponseCode;
    }

    public void setErrorResponseCode(Integer errorResponseCode) {
        this.errorResponseCode = errorResponseCode;
    }

    public List<String> getErrors() {
        return errors;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
