package com.aau.moodle20.payload.request;

import com.aau.moodle20.domain.ESemesterType;

import javax.validation.constraints.Size;

public class CreateSemesterRequest {

    private Integer year;

    private ESemesterType type;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ESemesterType getType() {
        return type;
    }

    public void setType(ESemesterType type) {
        this.type = type;
    }
}
