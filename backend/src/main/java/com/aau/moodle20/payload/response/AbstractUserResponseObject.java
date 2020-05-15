package com.aau.moodle20.payload.response;

public class AbstractUserResponseObject {

    private String matrikelNummer;
    private String username;
    private String forename;
    private String surname;

    public AbstractUserResponseObject()
    {
    }

    public String getMatrikelNummer() {
        return matrikelNummer;
    }

    public void setMatrikelNummer(String matrikelNummer) {
        this.matrikelNummer = matrikelNummer;
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

}
