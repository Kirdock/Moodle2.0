package com.aau.moodle20.payload.request;

import java.util.List;

public interface IExampleRequest<T> {

    List<T> getSubExamples();

    void setSubExamples(List<T> subExamples);
}
