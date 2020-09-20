package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinishesExampleResponse {

    private Long exampleId;
    private Integer parentOrder;
    private Integer order;
    private String exampleName;
    private String exerciseSheetName;

    private String matriculationNumber;
    private String surname;
    private String forename;
    private Long exerciseSheetId;

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public Integer getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(Integer parentOrder) {
        this.parentOrder = parentOrder;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getExampleName() {
        return exampleName;
    }

    public void setExampleName(String exampleName) {
        this.exampleName = exampleName;
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

    public String getExerciseSheetName() {
        return exerciseSheetName;
    }

    public void setExerciseSheetName(String exerciseSheetName) {
        this.exerciseSheetName = exerciseSheetName;
    }

    public Long getExerciseSheetId() {
        return exerciseSheetId;
    }

    public void setExerciseSheetId(Long exerciseSheetId) {
        this.exerciseSheetId = exerciseSheetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FinishesExampleResponse)) return false;
        FinishesExampleResponse that = (FinishesExampleResponse) o;
        return Objects.equals(getExampleId(), that.getExampleId()) &&
                Objects.equals(getParentOrder(), that.getParentOrder()) &&
                Objects.equals(getOrder(), that.getOrder()) &&
                Objects.equals(getExampleName(), that.getExampleName()) &&
                Objects.equals(getExerciseSheetName(), that.getExerciseSheetName()) &&
                Objects.equals(getMatriculationNumber(), that.getMatriculationNumber()) &&
                Objects.equals(getSurname(), that.getSurname()) &&
                Objects.equals(getForename(), that.getForename()) &&
                Objects.equals(getExerciseSheetId(), that.getExerciseSheetId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExampleId(), getParentOrder(), getOrder(), getExampleName(), getExerciseSheetName(), getMatriculationNumber(), getSurname(), getForename(), getExerciseSheetId());
    }
}
