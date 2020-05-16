package com.aau.moodle20.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateExerciseSheetRequest {

    @NotNull
    private Long courseId;
    @NotNull
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime submissionDate;
    @NotNull
    private Integer minKreuzel;
    @NotNull
    private Integer minPoints;
    @NotNull
    private Integer order;


    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}