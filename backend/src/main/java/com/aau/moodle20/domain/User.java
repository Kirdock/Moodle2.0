package com.aau.moodle20.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name ="user",
uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "martikelNummer")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20)
    private String martikelNummer;
    private Boolean isAdmin;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    private String username;

    private String forename;
    private String surename;


    public User()
    {

    }

    public User(String username, String password, Boolean isAdmin)
    {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    public User(String username, String martikelNummer, String forename, String surename, String password)
    {
        this.username = username;
        this.martikelNummer = martikelNummer;
        this.forename = forename;
        this.surename = surename;
        this.password = password;
        this.isAdmin = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMartikelNummer() {
        return martikelNummer;
    }

    public void setMartikelNummer(String martikelNummer) {
        this.martikelNummer = martikelNummer;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getSurename() {
        return surename;
    }

    public void setSurename(String surename) {
        this.surename = surename;
    }
}
