package com.himanshu.razorpay.common_library.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Registered only when resilience4j is on the classpath (see SharedExceptionAutoConfiguration).
// Ordered ahead of GlobalExceptionHandler so its catch-all doesn't swallow these as 500s.
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Slf4j
public class CircuitBreakerExceptionHandler {

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCallNotPermittedException(CallNotPermittedException ex) {
        log.warn("Circuit breaker '{}' is open, rejecting call", ex.getCausingCircuitBreakerName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("SERVICE_UNAVAILABLE", "A downstream service is temporarily unavailable, please retry later"));
    }
}
