package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPresentedResponse {

    private String exampleName;
    private Integer parentOrder;
    private Integer order;
    private Long exampleId;

    public String getExampleName() {
        return exampleName;
    }

    public void setExampleName(String exampleName) {
        this.exampleName = exampleName;
    }

    public Integer getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(Integer parentOrder) {
        this.parentOrder = parentOrder;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }
}
