package com.aau.moodle20.entity;

import com.aau.moodle20.payload.response.ViolationResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name ="violation")
public class ViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String result;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "violation_history_id", referencedColumnName = "id")
    private ViolationHistory violationHistory;

    public ViolationEntity()
    {
    }

    public ViolationEntity(String result)
    {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ViolationHistory getViolationHistory() {
        return violationHistory;
    }

    public void setViolationHistory(ViolationHistory exerciseSheet) {
        this.violationHistory = exerciseSheet;
    }

    public ViolationResponse createViolationResponse()
    {
        ViolationResponse response = new ViolationResponse();
        response.setResult(getResult());

        return response;
    }
}
