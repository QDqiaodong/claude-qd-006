package com.kitchen.central.dto;

public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
