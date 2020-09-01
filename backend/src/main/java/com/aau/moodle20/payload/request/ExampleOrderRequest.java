package com.aau.moodle20.payload.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ExampleOrderRequest {

    @NotNull
    private Long id;
    @NotNull
    @Min(value = 0)
    private Integer order;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
