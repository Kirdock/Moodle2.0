package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.ECourseRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseObject {

    private String matriculationNumber;
    private String username;
    private String forename;
    private String surname;
    private String email;
    private Boolean isAdmin;
    private ECourseRole courseRole;
    private List<FinishesExampleResponse> presentedExamples = new ArrayList<>();

    public UserResponseObject()
    {
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ECourseRole getCourseRole() {
        return courseRole;
    }

    public void setCourseRole(ECourseRole courseRole) {
        this.courseRole = courseRole;
    }

    public List<FinishesExampleResponse> getPresentedExamples() {
        return presentedExamples;
    }

    public void setPresentedExamples(List<FinishesExampleResponse> presentedExamples) {
        this.presentedExamples = presentedExamples;
    }
}
