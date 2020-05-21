package com.aau.moodle20.domain;

import com.aau.moodle20.constants.EUserRole;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name ="user",
uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "matrikelNummer")
})
public class User {

    @Id
    @Size(max = 20)
    @Column(name = "matrikelNummer", nullable = false)
    @Basic(optional = false)
    private String matrikelNummer;
    @Column(name = "role")
    private EUserRole role;

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

    public User(String matrikelNummer)
    {
        this.matrikelNummer = matrikelNummer;
    }

    public User(String username, String password, EUserRole role)
    {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public User(String username, String matrikelNummer, String forename, String surename, String password, EUserRole role)
    {
        this.username = username;
        this.matrikelNummer = matrikelNummer;
        this.forename = forename;
        this.surname = surename;
        this.password = password;
        this.role = role;
    }

    public String getMatrikelNumber() {
        return matrikelNummer;
    }

    public void setMatrikelNumber(String martikelNummer) {
        this.matrikelNummer = martikelNummer;
    }

    public EUserRole getRole() {
        return role;
    }

    public void setRole(EUserRole role) {
        this.role = role;
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

    public void setSurname(String surename) {
        this.surname = surename;
    }

    public Set<UserInCourse> getCourses() {
        return courses;
    }

    public void setCourses(Set<UserInCourse> courses) {
        this.courses = courses;
    }
}
