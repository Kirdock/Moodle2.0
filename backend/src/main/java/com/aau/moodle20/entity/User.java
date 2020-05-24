package com.aau.moodle20.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name ="user",
uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "matriculation_Number")
})
public class User {

    @Id
    @Size(max = 20)
    @Column(name = "matriculation_Number", nullable = false)
    @Basic(optional = false)
    private String matriculationNumber;
    private Boolean isAdmin;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    private String username;

    private String forename;
    private String surname;

    @OneToMany(mappedBy = "user")
    private Set<UserInCourse> courses;

    public User()
    {

    }

    public User(String matriculationNumber)
    {
        this.matriculationNumber = matriculationNumber;
    }

    public User(String username, String password, Boolean isAdmin)
    {
        this.username = username;
        this.password = password;
        this.isAdmin  = isAdmin;
    }
    public User(String username, String matriculationNumber, String forename, String surename, String password, Boolean isAdmin)
    {
        this.username = username;
        this.matriculationNumber = matriculationNumber;
        this.forename = forename;
        this.surname = surename;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<UserInCourse> getCourses() {
        return courses;
    }

    public void setCourses(Set<UserInCourse> courses) {
        this.courses = courses;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}