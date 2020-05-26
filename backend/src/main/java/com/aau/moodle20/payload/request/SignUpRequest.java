package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;

public class SignUpRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String matriculationNumber;

    @NotBlank
    private String forename;
    @NotBlank
    private String surname;
    private String admin;
    private Boolean isAdmin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
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

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
