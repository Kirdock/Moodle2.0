package com.aau.moodle20.entity.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCourseKey implements Serializable {

    @Column(name = "matriculation_Number")
    String matriculationNumber;

    @Column(name = "course_id")
    Long courseId;

    public UserCourseKey()
    {
    }

    public UserCourseKey(String matriculationNumber, Long courseId)
    {
        this.courseId= courseId;
        this.matriculationNumber = matriculationNumber;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCourseKey)) return false;
        UserCourseKey that = (UserCourseKey) o;
        return getMatriculationNumber().equals(that.getMatriculationNumber()) &&
                getCourseId().equals(that.getCourseId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatriculationNumber(), getCourseId());
    }
}
