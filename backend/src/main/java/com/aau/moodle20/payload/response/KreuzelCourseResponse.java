package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.EFinishesExampleState;

import java.util.List;

public class KreuzelCourseResponse {

    private String exerciseSheetName;
    private String matriculationName;
    private String surname;
    private String forename;
    private EFinishesExampleState state;
    private String exampleName;
    private Long exampleId;

    public String getExerciseSheetName() {
        return exerciseSheetName;
    }

    public void setExerciseSheetName(String exerciseSheetName) {
        this.exerciseSheetName = exerciseSheetName;
    }

    public String getMatriculationName() {
        return matriculationName;
    }

    public void setMatriculationName(String matriculationName) {
        this.matriculationName = matriculationName;
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
}
