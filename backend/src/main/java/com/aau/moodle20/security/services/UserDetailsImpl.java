package com.aau.moodle20.security.services;

import com.aau.moodle20.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String username;

    private Boolean isAdmin;
    private String matrikelNumber;
    private String forename;
    private String surename;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl( String username, String password, Boolean isAdmin, String matrikelNumber, String forename, String surename) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.matrikelNumber = matrikelNumber;
        this.forename = forename;
        this.surename = surename;
    }

    public static UserDetailsImpl build(User user) {

        List<GrantedAuthority> authorities = new ArrayList<>();
        if(user.getAdmin())
        {
            authorities.add(new SimpleGrantedAuthority("Admin")); // TODO define a constant somewhere
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                user.getAdmin(),
                user.getMartikelNumber(),
                user.getForename(),
                user.getSurname());

                userDetails.setAuthorities(authorities);

        return userDetails ;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getMatrikelNumber() {
        return matrikelNumber;
    }

    public void setMatrikelNumber(String matrikelNumber) {
        this.matrikelNumber = matrikelNumber;
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

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return
                Objects.equals(matrikelNumber, user.matrikelNumber);
    }
}
