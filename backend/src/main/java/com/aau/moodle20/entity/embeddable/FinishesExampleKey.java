package com.aau.moodle20.entity.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FinishesExampleKey implements Serializable {

    @Column(name = "matriculation_Number")
    String matriculationNumber;

    @Column(name = "example_id")
    Long exampleId;

    public FinishesExampleKey()
    {
    }

    public FinishesExampleKey(String matriculationNumber, Long exampleId)
    {
        this.exampleId = exampleId;
        this.matriculationNumber = matriculationNumber;
    }

    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long courseId) {
        this.exampleId = courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FinishesExampleKey)) return false;
        FinishesExampleKey that = (FinishesExampleKey) o;
        return getMatriculationNumber().equals(that.getMatriculationNumber()) &&
                getExampleId().equals(that.getExampleId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatriculationNumber(), getExampleId());
    }
}
