package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationHistoryResponse)) return false;
        ViolationHistoryResponse response = (ViolationHistoryResponse) o;
        return Objects.equals(getDate(), response.getDate()) &&
                Objects.equals(getViolations(), response.getViolations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getViolations());
    }
}
