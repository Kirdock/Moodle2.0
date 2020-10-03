package com.aau.moodle20.payload.response;

import java.util.Objects;

public class ViolationResponse {

    String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationResponse)) return false;
        ViolationResponse response = (ViolationResponse) o;
        return Objects.equals(getResult(), response.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResult());
    }
}
