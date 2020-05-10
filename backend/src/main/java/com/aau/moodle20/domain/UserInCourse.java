package com.aau.moodle20.domain;

import javax.persistence.*;

@Entity
@Table(name ="user_in_course")
public class UserInCourse {

    @EmbeddedId
    UserCourseKey id;

    @ManyToOne
    @MapsId("matrikelNummer")
    @JoinColumn(name = "matrikelNummer")
     User user;

    @ManyToOne
    @MapsId("course_id")
    @JoinColumn(name = "course_id")
    Course course;

    ECourseRole role;

    public UserInCourse()
    {
    }

    public UserCourseKey getId() {
        return id;
    }

    public void setId(UserCourseKey id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public ECourseRole getRole() {
        return role;
    }

    public void setRole(ECourseRole role) {
        this.role = role;
    }
}
