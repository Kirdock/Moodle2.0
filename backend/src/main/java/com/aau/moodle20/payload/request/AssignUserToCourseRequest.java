package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.ECourseRole;

import javax.validation.constraints.NotNull;

public class AssignUserToCourseRequest {

    @NotNull
    private Long courseId;
    @NotNull
    private String matriculationNumber;
    @NotNull
    private ECourseRole role;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public ECourseRole getRole() {
        return role;
    }

    public void setRole(ECourseRole role) {
        this.role = role;
    }
}
