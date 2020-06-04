package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.EFinishesExampleState;

import javax.validation.constraints.NotNull;

public class UserKreuzelRequest {

    @NotNull
    private Long exampleId;
    @NotNull
    private EFinishesExampleState state;

    private String description;

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public EFinishesExampleState getState() {
        return state;
    }

    public void setState(EFinishesExampleState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
