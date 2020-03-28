package com.seamCarving;

import java.util.List;

public class SeamCarvingResponse<T> {

    private boolean isSuccess;
    private String errorMessage;
    private T body;

    public SeamCarvingResponse(boolean isSuccess, String errorMessage, T body) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.body = body;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
