package com.aau.moodle20.entity.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCourseKey implements Serializable {

    @Column(name = "matrikelNummer")
    String matrikelNummer;

    @Column(name = "course_id")
    Long courseId;

    public UserCourseKey()
    {
    }

    public UserCourseKey(String matrikelnummer, Long courseId)
    {
        this.courseId= courseId;
        this.matrikelNummer = matrikelnummer;
    }

    public String getMatrikelNummer() {
        return matrikelNummer;
    }

    public void setMatrikelNummer(String matrikelNummer) {
        this.matrikelNummer = matrikelNummer;
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
        return getMatrikelNummer().equals(that.getMatrikelNummer()) &&
                getCourseId().equals(that.getCourseId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatrikelNummer(), getCourseId());
    }
}
