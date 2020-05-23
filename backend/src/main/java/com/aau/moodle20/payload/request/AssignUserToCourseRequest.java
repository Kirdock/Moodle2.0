package com.aau.moodle20.payload.request;

import com.aau.moodle20.constants.ECourseRole;

public class AssignUserToCourseRequest {

    private Long courseId;
    private String matriculationNumber;
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
