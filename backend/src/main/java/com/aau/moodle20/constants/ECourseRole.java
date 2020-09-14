package com.aau.moodle20.constants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ECourseRole {

    LECTURER("l"),
    STUDENT("s"),
    TEACHER("t"),
    NONE("n");

    private String role;

    ECourseRole(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return this.role;
    }
}
