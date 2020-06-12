package com.aau.moodle20.payload.response;

import java.util.List;

public class KreuzelResponse {

    private String exerciseSheetName;
    private List<FinishesExampleResponse> examples;

    public String getExerciseSheetName() {
        return exerciseSheetName;
    }

    public void setExerciseSheetName(String exerciseSheetName) {
        this.exerciseSheetName = exerciseSheetName;
    }

    public List<FinishesExampleResponse> getExamples() {
        return examples;
    }

    public void setExamples(List<FinishesExampleResponse> examples) {
        this.examples = examples;
    }
}
