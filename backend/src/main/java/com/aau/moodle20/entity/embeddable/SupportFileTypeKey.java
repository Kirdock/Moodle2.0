package com.aau.moodle20.entity.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupportFileTypeKey implements Serializable {

    @Column(name = "example_id")
    Long exampleId;

    @Column(name = "file_type_id")
    Long fileTypeId;

    public SupportFileTypeKey()
    {
    }

    public SupportFileTypeKey(Long fileTypeId, Long exampleId)
    {
        this.exampleId = exampleId;
        this.fileTypeId = fileTypeId;
    }

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long courseId) {
        this.exampleId = courseId;
    }

    public Long getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(Long fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportFileTypeKey)) return false;
        SupportFileTypeKey that = (SupportFileTypeKey) o;
        return getExampleId().equals(that.getExampleId()) &&
                getFileTypeId().equals(that.getFileTypeId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExampleId(), getFileTypeId());
    }
}
