package com.aau.moodle20.constants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EUserRole {

    Lecturer("l"),
    Student("s"),
    Admin("a"),
    None("n");

    private String role;

    EUserRole(String role)
    {
        this.role = role;
    }

    @JsonValue
    public String getRole()
    {
        return this.role;
    }
}
