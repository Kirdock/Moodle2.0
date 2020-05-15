package com.aau.moodle20.payload.response;

import com.aau.moodle20.constants.ECourseRole;

public class UserCourseResponseObject  extends AbstractUserResponseObject {

    private ECourseRole courseRole;


    public UserCourseResponseObject()
    {
    }

    public ECourseRole getCourseRole() {
        return courseRole;
    }

    public void setCourseRole(ECourseRole courseRole) {
        this.courseRole = courseRole;
    }
}
