package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotNull;

public class UpdateCourseRequest extends CreateCourseRequest {

    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
