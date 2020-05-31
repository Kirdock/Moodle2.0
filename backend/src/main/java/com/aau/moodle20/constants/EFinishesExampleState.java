package com.aau.moodle20.constants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EFinishesExampleState {

    YES("y"),
    NO("n"),
    MAYBE("m");

    private String role;

    EFinishesExampleState(String role)
    {
        this.role = role;
    }

    @JsonValue
    public String getRole()
    {
        return this.role;
    }
}
