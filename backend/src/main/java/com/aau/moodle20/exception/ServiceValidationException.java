package com.aau.moodle20.exception;

import java.util.ArrayList;
import java.util.List;

public class ServiceValidationException extends RuntimeException{

    private Integer errorResponseCode;
    private List<String> errors = new ArrayList<>();
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
    public Integer getErrorResponseCode() {
        return errorResponseCode;
    }

    public void setErrorResponseCode(Integer errorResponseCode) {
        this.errorResponseCode = errorResponseCode;
    }

    public List<String> getErrors() {
        return errors;
    }
}
