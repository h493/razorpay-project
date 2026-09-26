package com.himanshu.razorpay.common_library.exception;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class SharedExceptionAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(){
        return new GlobalExceptionHandler();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "io.github.resilience4j.circuitbreaker.CallNotPermittedException")
    static class CircuitBreakerExceptionConfiguration {

        @Bean
        public CircuitBreakerExceptionHandler circuitBreakerExceptionHandler(){
            return new CircuitBreakerExceptionHandler();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.FeignException")
    static class FeignExceptionConfiguration {

        @Bean
        public FeignExceptionHandler feignExceptionHandler(){
            return new FeignExceptionHandler();
        }
    }
}
