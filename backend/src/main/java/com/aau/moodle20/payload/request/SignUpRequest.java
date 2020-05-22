package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;

public class SignUpRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String matrikelnummer;

    @NotBlank
    private String forename;
    @NotBlank
    private String surname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMatrikelnummer() {
        return matrikelnummer;
    }

    public void setMatrikelnummer(String matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
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
}
