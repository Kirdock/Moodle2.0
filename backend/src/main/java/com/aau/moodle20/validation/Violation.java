package com.aau.moodle20.validation;

public class Violation {

    private String result;

    public Violation()
    {
    }

    public Violation(String result)
    {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
