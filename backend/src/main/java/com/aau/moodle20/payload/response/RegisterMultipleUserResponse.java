package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterMultipleUserResponse {

    List<Integer> failedUsers  = new ArrayList<>();
    List<UserResponseObject> registeredUsers = new ArrayList<>();

    public List<Integer> getFailedUsers() {
        return failedUsers;
    }

    public void setFailedUsers(List<Integer> failedUsers) {
        this.failedUsers = failedUsers;
    }

    public List<UserResponseObject> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(List<UserResponseObject> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }
}
