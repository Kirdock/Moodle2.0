package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponseObject {

    private Long id;
    private String name;
    private String number;
    private Integer minKreuzel;
    private Integer minPoints;
    private List<ExerciseSheetResponseObject> exerciseSheets;
    private String descriptionTemplate;
    private String owner;
    private Boolean includeThird;
    private List<AssignedStudent> assignedStudents;
    private Long semesterId;

    public CourseResponseObject()
    {
    }

    public CourseResponseObject(Long id)
    {
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    public Integer getMinKreuzel() {
        return minKreuzel;
    }

    public void setMinKreuzel(Integer minKreuzel) {
        this.minKreuzel = minKreuzel;
    }

    public Integer getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(Integer minPoints) {
        this.minPoints = minPoints;
    }

    public List<ExerciseSheetResponseObject> getExerciseSheets() {
        return exerciseSheets;
    }

    public void setExerciseSheets(List<ExerciseSheetResponseObject> exerciseSheets) {
        this.exerciseSheets = exerciseSheets;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Boolean getIncludeThird() {
        return includeThird;
    }

    public void setIncludeThird(Boolean includeThird) {
        this.includeThird = includeThird;
    }

    public List<AssignedStudent> getAssignedStudents() {
        return assignedStudents;
    }

    public void setAssignedStudents(List<AssignedStudent> assignedStudents) {
        this.assignedStudents = assignedStudents;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }
}
