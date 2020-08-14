package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterMultipleUserResponse {

    String message;
    List<Integer> failedUsers  = new ArrayList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Integer> getFailedUsers() {
        return failedUsers;
    }

    public void setFailedUsers(List<Integer> failedUsers) {
        this.failedUsers = failedUsers;
    }
}
