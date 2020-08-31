package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ServiceException extends RuntimeException{

    private Integer errorResponseCode;
    private List<String> errors = new ArrayList<>();
    private HttpStatus httpStatus = null;
    public ServiceException()
    {
    }

    public ServiceException(String message, Throwable throwable)
    {
        super(message,throwable);
    }

    public ServiceException(String message)
    {
        super(message);
    }

    public ServiceException(String message, Integer errorResponseCode)
    {
        super(message);
        this.errorResponseCode = errorResponseCode;
    }
    public ServiceException(String message, Throwable throwable,Integer errorResponseCode)
    {
        super(message,throwable);
        this.errorResponseCode = errorResponseCode;
    }
    public ServiceException(String message, HttpStatus status)
    {
        super(message);
        this.httpStatus = status;
    }
    public ServiceException(String message, Integer errorResponseCode, HttpStatus status)
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
