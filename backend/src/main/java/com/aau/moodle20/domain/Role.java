package com.aau.moodle20.domain;

import javax.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ECourseRole name;

    public Role() {

    }

    public Role(ECourseRole name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ECourseRole getName() {
        return name;
    }

    public void setName(ECourseRole name) {
        this.name = name;
    }
}
