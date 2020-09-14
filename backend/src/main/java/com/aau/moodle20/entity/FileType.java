package com.aau.moodle20.entity;

import com.aau.moodle20.payload.response.FileTypeResponseObject;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "file_type")
public class FileType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;
    private String name;

    @OneToMany(mappedBy = "fileType", fetch = FetchType.LAZY)
    Set<SupportFileType> assignedExamples;


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

    public FileTypeResponseObject createFileTypeResponseObject() {
        FileTypeResponseObject responseObject = new FileTypeResponseObject();
        responseObject.setId(getId());
        responseObject.setName(getName());
        responseObject.setValue(getValue());

        return responseObject;
    }

    public FileTypeResponseObject createFileTypeResponseObjectOnlyId() {
        FileTypeResponseObject responseObject = new FileTypeResponseObject();
        responseObject.setId(getId());
        return responseObject;
    }
}
