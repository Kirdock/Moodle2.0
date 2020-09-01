package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.ESemesterType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CreateSemesterRequest {

    @NotNull
    @Min(value = 0)
    private Integer year;
    @NotNull
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
