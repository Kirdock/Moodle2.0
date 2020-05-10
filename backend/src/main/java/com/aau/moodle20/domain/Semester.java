package com.aau.moodle20.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name ="semester")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;
    @Enumerated(EnumType.STRING)
    private ESemesterType type;

    @OneToMany(
            mappedBy = "semester",
            cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<Course> courses;

    public Semester(Long id)
    {
        this.id = id;
    }

    public Semester()
    {
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ESemesterType getType() {
        return type;
    }

    public void setType(ESemesterType type) {
        this.type = type;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }
}
