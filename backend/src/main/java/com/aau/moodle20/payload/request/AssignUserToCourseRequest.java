package com.aau.moodle20.payload.request;

import com.aau.moodle20.domain.ECourseRole;

public class AssignUserToCourseRequest {

    private Long courseId;
    private String matrikelNummer;
    private ECourseRole courseRole;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getMatrikelNummer() {
        return matrikelNummer;
    }

    public void setMatrikelNummer(String matrikelNummer) {
        this.matrikelNummer = matrikelNummer;
    }

    public ECourseRole getCourseRole() {
        return courseRole;
    }

    public void setCourseRole(ECourseRole courseRole) {
        this.courseRole = courseRole;
    }
}
