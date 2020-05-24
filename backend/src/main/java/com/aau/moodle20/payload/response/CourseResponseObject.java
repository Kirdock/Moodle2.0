package com.aau.moodle20.payload.response;

import java.util.List;

public class CourseResponseObject {

    private Long id;
    private String name;
    private String number;
    private Integer minKreuzel;
    private Integer minPoints;
    private List<ExerciseSheetResponseObject> exerciseSheets;
    private String descriptionTemplate;

    public CourseResponseObject()
    {
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
}
