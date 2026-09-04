package com.himanshu.razorpay.common_library.exception;

import lombok.Getter;

@Getter
public class BusinessRuleViolationException extends RuntimeException{

    private final String errorCode;

    public BusinessRuleViolationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
