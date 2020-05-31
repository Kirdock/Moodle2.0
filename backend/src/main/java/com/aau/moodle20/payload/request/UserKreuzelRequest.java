package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.EFinishesExampleState;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UserKreuzelRequest {

    @NotNull
    private Long exampleId;
    @NotNull
    private EFinishesExampleState type;
    
    private String description;

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public EFinishesExampleState getType() {
        return type;
    }

    public void setType(EFinishesExampleState type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
