package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class SignUpRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    private String matriculationNumber;

    @NotBlank
    private String forename;
    @NotBlank
    private String surname;
    private Boolean isAdmin = Boolean.FALSE;

    @Pattern(regexp = "[^@ \\t\\r\\n]+@[^@ \\t\\r\\n]+\\.[^@ \\t\\r\\n]+")
    private String email;

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

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        if (isAdmin != null)
            this.isAdmin = isAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
