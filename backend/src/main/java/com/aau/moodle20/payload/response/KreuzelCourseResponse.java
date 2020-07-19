package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.EFinishesExampleState;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KreuzelCourseResponse {

    private String exerciseSheetName;
    private String matriculationNumber;
    private String surname;
    private String forename;
    private EFinishesExampleState state;
    private String exampleName;
    private Long exampleId;
    private List<KreuzelCourseState> states;

    public String getExerciseSheetName() {
        return exerciseSheetName;
    }

    public void setExerciseSheetName(String exerciseSheetName) {
        this.exerciseSheetName = exerciseSheetName;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public EFinishesExampleState getState() {
        return state;
    }

    public void setState(EFinishesExampleState state) {
        this.state = state;
    }

    public String getExampleName() {
        return exampleName;
    }

    public void setExampleName(String exampleName) {
        this.exampleName = exampleName;
    }

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public List<KreuzelCourseState> getStates() {
        return states;
    }

    public void setStates(List<KreuzelCourseState> states) {
        this.states = states;
    }
}
