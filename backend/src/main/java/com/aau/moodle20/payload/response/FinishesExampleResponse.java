package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.EFinishesExampleState;

public class FinishesExampleResponse {

    private Long exampleId;
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
