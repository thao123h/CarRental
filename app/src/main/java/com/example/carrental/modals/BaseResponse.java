package com.example.carrental.modals;

public class BaseResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public T getData() {
        return data;
    }
}
