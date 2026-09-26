package com.himanshu.razorpay.common_library.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Registered only when OpenFeign is on the classpath (see SharedExceptionAutoConfiguration).
// Ordered ahead of GlobalExceptionHandler so its catch-all doesn't swallow these as 500s.
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Slf4j
public class FeignExceptionHandler {

    // downstream error details stay in the logs, the client only sees a generic 502
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        log.error("Downstream call failed, status={}, message={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of("DOWNSTREAM_SERVICE_ERROR", "A downstream service call failed"));
    }
}
