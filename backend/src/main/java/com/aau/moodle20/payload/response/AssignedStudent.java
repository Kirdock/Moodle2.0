package com.aau.moodle20.payload.response;

import java.util.List;

public class AssignedStudent {

    private String matriculationNumber;
    private List<UserPresentedResponse> presentedExamples;


    public List<UserPresentedResponse> getPresentedExamples() {
        return presentedExamples;
    }

    public void setPresentedExamples(List<UserPresentedResponse> presentedExamples) {
        this.presentedExamples = presentedExamples;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }
}
