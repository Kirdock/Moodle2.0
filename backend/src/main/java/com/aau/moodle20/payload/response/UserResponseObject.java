package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.EUserRole;

public class UserResponseObject  extends AbstractUserResponseObject{

    private EUserRole userRole;

    public UserResponseObject()
    {
    }

    public EUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(EUserRole userRole) {
        this.userRole = userRole;
    }
}
