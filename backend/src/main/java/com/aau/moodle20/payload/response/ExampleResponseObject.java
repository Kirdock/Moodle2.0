package com.aau.moodle20.payload.response;

import com.aau.moodle20.entity.Example;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExampleResponseObject {


    private Long id;
    private Long exerciseSheetId;
    private Long parentId;
    private String name;
    private String description;
    private Integer points;
    private Integer weighting;
    private Boolean mandatory;
    private Integer order;
    private String validator;
    List<FileTypeResponseObject> supportedFileTypes;
    private List<ExampleResponseObject> subExamples = new ArrayList<>();
    private Boolean submitFile;


    public ExampleResponseObject(){
    }

    public ExampleResponseObject(Long id){
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getWeighting() {
        return weighting;
    }

    public void setWeighting(Integer weighting) {
        this.weighting = weighting;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public List<FileTypeResponseObject> getSupportedFileTypes() {
        return supportedFileTypes;
    }

    public void setSupportedFileTypes(List<FileTypeResponseObject> supportedFileTypes) {
        this.supportedFileTypes = supportedFileTypes;
    }

    public List<ExampleResponseObject> getSubExamples() {
        return subExamples;
    }

    public void setSubExamples(List<ExampleResponseObject> subExamples) {
        this.subExamples = subExamples;
    }

    public Long getExerciseSheetId() {
        return exerciseSheetId;
    }

    public void setExerciseSheetId(Long exerciseSheetId) {
        this.exerciseSheetId = exerciseSheetId;
    }

    public Boolean getSubmitFile() {
        return submitFile;
    }

    public void setSubmitFile(Boolean submitFile) {
        this.submitFile = submitFile;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
