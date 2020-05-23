package com.aau.moodle20.payload.request;

import java.util.List;

public class CreateExampleRequest  extends AbstractExampleRequest  implements IExampleRequest<CreateExampleRequest>{


    private List<CreateExampleRequest> subExamples;


    public List<CreateExampleRequest> getSubExamples() {
        return subExamples;
    }

    public void setSubExamples(List<CreateExampleRequest> subExamples) {
        this.subExamples = subExamples;
    }
}
