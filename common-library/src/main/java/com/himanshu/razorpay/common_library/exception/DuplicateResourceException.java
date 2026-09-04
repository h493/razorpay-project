package com.himanshu.razorpay.common_library.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException{
    private final String errorCode;

    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
