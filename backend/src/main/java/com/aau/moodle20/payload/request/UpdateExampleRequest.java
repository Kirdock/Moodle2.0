package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class UpdateExampleRequest  extends AbstractExampleRequest implements IExampleRequest<UpdateExampleRequest>{


    private Long id;
    private List<UpdateExampleRequest> subExamples;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<UpdateExampleRequest> getSubExamples() {
        return subExamples;
    }

    public void setSubExamples(List<UpdateExampleRequest> subExamples) {
        this.subExamples = subExamples;
    }
}
