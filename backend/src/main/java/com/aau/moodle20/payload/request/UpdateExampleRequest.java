package com.aau.moodle20.payload.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class UpdateExampleRequest {

    @NotNull
    private Long id;
    private Long parentId;
    @NotNull
    private Long exerciseSheetId;
    @NotBlank
    private String name;
    private String description;
    private Integer points;
    private Integer weighting;
    private Boolean mandatory;
    @NotNull
    @Min(value = 0)
    private Integer order;
    private List<Long> supportedFileTypes;
    private Boolean submitFile;
    private List<String> customFileTypes;
    @Min(value = 0)
    private Integer uploadCount;

    public Long getExerciseSheetId() {
        return exerciseSheetId;
    }

    public void setExerciseSheetId(Long exerciseSheetId) {
        this.exerciseSheetId = exerciseSheetId;
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

    public List<Long> getSupportedFileTypes() {
        return supportedFileTypes;
    }

    public void setSupportedFileTypes(List<Long> supportedFileTypes) {
        this.supportedFileTypes = supportedFileTypes;
    }

    public Boolean getSubmitFile() {
        return submitFile;
    }

    public void setSubmitFile(Boolean submitFile) {
        this.submitFile = submitFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<String> getCustomFileTypes() {
        return customFileTypes;
    }

    public void setCustomFileTypes(List<String> customFileTypes) {
        this.customFileTypes = customFileTypes;
    }

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }
}
