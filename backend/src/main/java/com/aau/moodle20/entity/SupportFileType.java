package com.aau.moodle20.entity;

import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "support_file_type")
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

    public SupportFileType copy() {
        SupportFileType supportFileType = new SupportFileType();
        supportFileType.setFileType(getFileType());
        supportFileType.setExample(getExample());
        return supportFileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportFileType)) return false;
        SupportFileType that = (SupportFileType) o;
        return Objects.equals(getExample(), that.getExample()) &&
                Objects.equals(getFileType(), that.getFileType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExample(), getFileType());
    }
}
