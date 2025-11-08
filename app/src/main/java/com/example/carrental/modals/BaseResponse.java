package com.example.carrental.modals;

public class BaseResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;

    public T getData() {
        return data;
    }
}
