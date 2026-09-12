package com.himanshu.razorpay.payment_service.config;

import com.himanshu.razorpay.common_library.idempotency.IdempotencyFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final IdempotencyFilter idempotencyFilter;

    @Bean
    public FilterRegistrationBean<Filter> idempotencyFilterRegistration() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(idempotencyFilter);
        registrationBean.setOrder(IdempotencyFilter.ORDER);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
