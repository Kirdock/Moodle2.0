package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KreuzelCourseState {

    private String type;
    private String description;
    private List<ViolationHistoryResponse> result;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ViolationHistoryResponse> getResult() {
        return result;
    }

    public void setResult(List<ViolationHistoryResponse> result) {
        this.result = result;
    }
}
