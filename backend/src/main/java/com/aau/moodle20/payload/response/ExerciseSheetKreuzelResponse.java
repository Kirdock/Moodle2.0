package com.aau.moodle20.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExerciseSheetKreuzelResponse {

    private boolean includeThird;
    private List<ExampleResponseObject> examples = new ArrayList<>();
    private List<KreuzelCourseResponse> kreuzel = new ArrayList<>();

    public boolean isIncludeThird() {
        return includeThird;
    }

    public void setIncludeThird(boolean includeThird) {
        this.includeThird = includeThird;
    }

    public List<ExampleResponseObject> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleResponseObject> examples) {
        this.examples = examples;
    }

    public List<KreuzelCourseResponse> getKreuzel() {
        return kreuzel;
    }

    public void setKreuzel(List<KreuzelCourseResponse> kreuzel) {
        this.kreuzel = kreuzel;
    }
}
