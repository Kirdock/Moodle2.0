package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.EFinishesExampleState;

import javax.validation.constraints.NotNull;

public class UserKreuzeMultilRequest {

    @NotNull
    private Long exampleId;
    @NotNull
    private EFinishesExampleState state;
    @NotNull
    private String matriculationNumber;

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

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }
}
