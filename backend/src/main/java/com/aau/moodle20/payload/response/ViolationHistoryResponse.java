package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class ViolationHistoryResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;
    private List<ViolationResponse> violations;


    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<ViolationResponse> getViolations() {
        return violations;
    }

    public void setViolations(List<ViolationResponse> violations) {
        this.violations = violations;
    }
}
