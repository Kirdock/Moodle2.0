package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

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
    private Long semesterId;
    private List<FinishesExampleResponse> presented;
    private String description;
    private List<KreuzelCourseResponse> kreuzelList;
    private Integer uploadCount;


    public CourseResponseObject() {
    }

    public CourseResponseObject(Long id) {
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

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public List<FinishesExampleResponse> getPresented() {
        return presented;
    }

    public void setPresented(List<FinishesExampleResponse> presented) {
        this.presented = presented;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<KreuzelCourseResponse> getKreuzelList() {
        return kreuzelList;
    }

    public void setKreuzelList(List<KreuzelCourseResponse> kreuzelList) {
        this.kreuzelList = kreuzelList;
    }

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseResponseObject)) return false;
        CourseResponseObject that = (CourseResponseObject) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getNumber(), that.getNumber()) &&
                Objects.equals(getMinKreuzel(), that.getMinKreuzel()) &&
                Objects.equals(getMinPoints(), that.getMinPoints()) &&
                Objects.equals(getExerciseSheets(), that.getExerciseSheets()) &&
                Objects.equals(getDescriptionTemplate(), that.getDescriptionTemplate()) &&
                Objects.equals(getOwner(), that.getOwner()) &&
                Objects.equals(getIncludeThird(), that.getIncludeThird()) &&
                Objects.equals(getSemesterId(), that.getSemesterId()) &&
                Objects.equals(getPresented(), that.getPresented()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getKreuzelList(), that.getKreuzelList()) &&
                Objects.equals(getUploadCount(), that.getUploadCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getNumber(), getMinKreuzel(), getMinPoints(), getExerciseSheets(), getDescriptionTemplate(), getOwner(), getIncludeThird(), getSemesterId(), getPresented(), getDescription(), getKreuzelList(), getUploadCount());
    }
}
