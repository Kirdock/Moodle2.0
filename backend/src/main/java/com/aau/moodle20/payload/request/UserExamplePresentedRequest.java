package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UserExamplePresentedRequest {

    @NotNull
    private Long exampleId;
    @NotBlank
    private String matriculationNumber;
    @NotNull
    private Boolean hasPresented;

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public Boolean getHasPresented() {
        return hasPresented;
    }

    public void setHasPresented(Boolean hasPresented) {
        this.hasPresented = hasPresented;
    }
}
