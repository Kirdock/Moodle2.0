package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class AbstractExampleRequest{

    @NotNull
    private Long exerciseSheetId;
    @NotBlank
    private String name;
    private String description;
    private Integer points;
    private Integer weighting;
    @NotNull
    private Boolean mandatory;
    @NotNull
    private Integer order;
    @NotNull
    private String validator;
    @NotNull
    private List<Long> supportedFileTypes;

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

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public List<Long> getSupportedFileTypes() {
        return supportedFileTypes;
    }

    public void setSupportedFileTypes(List<Long> supportedFileTypes) {
        this.supportedFileTypes = supportedFileTypes;
    }
}
