package com.himanshu.razorpay.common_library.enums;

public enum PaymentEvent {
    AUTHORIZE_ATTEMPT,
    AUTHORIZE_SUCCESS,
    AUTHORIZE_FAILURE,
    CAPTURE_ATTEMPT,
    CAPTURE_SUCCESS,
    CAPTURE_FAILURE,
    REFUND_INIT,
    REFUND_COMPLETE,
    REFUND_FAILURE,
    CANCEL,
    SETTLE,
    CAPTURE_TIMEOUT
}
