package com.aau.moodle20.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name ="file_type")
public class FileType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;
    private String name;

    @OneToMany(mappedBy = "fileType", fetch = FetchType.LAZY)
    Set<SupportFileType> assignedExamples; // TODO find better name


    public FileType() {
    }

    public FileType(String name, String value) {
        this.name = name;
        this.value = value;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SupportFileType> getAssignedExamples() {
        return assignedExamples;
    }

    public void setAssignedExamples(Set<SupportFileType> assignedExamples) {
        this.assignedExamples = assignedExamples;
    }
}
