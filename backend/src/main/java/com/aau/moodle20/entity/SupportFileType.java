package com.aau.moodle20.entity;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;

import javax.persistence.*;

@Entity
@Table(name ="support_file_type")
public class SupportFileType {

    @EmbeddedId
    SupportFileTypeKey id;

    @ManyToOne
    @MapsId("example_id")
    @JoinColumn(name = "example_id")
     Example example;

    @ManyToOne
    @MapsId("file_type_id")
    @JoinColumn(name = "file_type_id")
    FileType fileType;

    public SupportFileType()
    {
    }

    public SupportFileTypeKey getId() {
        return id;
    }

    public void setId(SupportFileTypeKey id) {
        this.id = id;
    }

    public Example getExample() {
        return example;
    }

    public void setExample(Example example) {
        this.example = example;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
}
