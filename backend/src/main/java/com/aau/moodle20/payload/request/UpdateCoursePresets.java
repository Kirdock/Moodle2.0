package com.aau.moodle20.payload.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateCoursePresets {

    @NotNull
    private Long id;
    private String description;
    @NotNull
    @Min(value = 0, message = "Upload count must greater or equal 0")
    private Integer uploadCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }
}
