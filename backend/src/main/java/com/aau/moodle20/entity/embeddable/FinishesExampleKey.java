package com.aau.moodle20.entity.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FinishesExampleKey implements Serializable {

    @Column(name = "matrikelNummer")
    String matrikelNummer;

    @Column(name = "example_id")
    Long exampleId;

    public FinishesExampleKey()
    {
    }

    public FinishesExampleKey(String matrikelnummer, Long exampleId)
    {
        this.exampleId = exampleId;
        this.matrikelNummer = matrikelnummer;
    }

    public String getMatrikelNummer() {
        return matrikelNummer;
    }

    public void setMatrikelNummer(String matrikelNummer) {
        this.matrikelNummer = matrikelNummer;
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
        return getMatrikelNummer().equals(that.getMatrikelNummer()) &&
                getExampleId().equals(that.getExampleId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatrikelNummer(), getExampleId());
    }
}
