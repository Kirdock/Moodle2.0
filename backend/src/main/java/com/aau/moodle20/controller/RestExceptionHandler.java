package com.aau.moodle20.controller;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.exception.ApiError;
import com.aau.moodle20.exception.ServiceException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<Object> handleServiceValidationException(ServiceException ex) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;

        ApiError apiError =
                new ApiError(status, ex.getMessage(), ex.getErrors(), ex.getErrorResponseCode());
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(ClassNotFoundException.class)
    protected ResponseEntity<Object> handleClassNotFoundException(ClassNotFoundException ex) {
        List<String> errors = new ArrayList<>();
        errors.add("ClassNotFoundException");
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<Object> handleNullPointerException(NullPointerException ex) {
        List<String> errors = new ArrayList<>();
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, "NullPointerException occured", errors);
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN, ex.getLocalizedMessage(), "error occurred");
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors, ApiErrorResponseCodes.INVALID_METHOD_PARAMETER);
        return handleExceptionInternal(
                ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), new ArrayList<>(), ApiErrorResponseCodes.API_NOT_FOUND);
        return handleExceptionInternal(
                ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(
                " method is not supported for this request. Supported methods are ");
        if (ex.getSupportedHttpMethods() != null) {
            ex.getSupportedHttpMethods().forEach(t -> {
                if (t != null) {
                    builder.append(t).append(" ");
                }
            });
        }


        ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED,
                ex.getLocalizedMessage(), builder.toString(), ApiErrorResponseCodes.API_NOT_FOUND);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ApiError apiError = null;

        apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "error occurred");

        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }
}
