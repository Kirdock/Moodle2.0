package com.aau.moodle20.controller;

import com.aau.moodle20.exception.SemesterAlreadyCreatedException;
import com.aau.moodle20.payload.response.MessageResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {



    @ExceptionHandler(SemesterAlreadyCreatedException.class)
    protected ResponseEntity<Object> handleSemesterAlreadyCreated(SemesterAlreadyCreatedException ex) {
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse(ex.getMessage()));
    }

}
