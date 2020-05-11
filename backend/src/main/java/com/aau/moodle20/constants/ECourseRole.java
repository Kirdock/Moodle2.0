package com.aau.moodle20.constants;

import com.fasterxml.jackson.annotation.JsonValue;
import org.hibernate.cfg.Environment;

public enum ECourseRole {

    Lecturer("l"),
    Student("s"),
    Teacher("t"),
    None("n");

    private String role;

    ECourseRole(String role)
    {
        this.role = role;
    }

    @JsonValue
    public String getRole()
    {
        return this.role;
    }
}
