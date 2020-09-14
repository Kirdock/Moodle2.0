package com.aau.moodle20.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserException extends RuntimeException {

    public UserException() {
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Exception ex) {
        super(message, ex);
    }
}
