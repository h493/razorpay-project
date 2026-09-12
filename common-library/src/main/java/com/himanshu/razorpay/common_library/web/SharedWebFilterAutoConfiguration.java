package com.himanshu.razorpay.common_library.web;

import com.himanshu.razorpay.common_library.context.MerchantContext;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RequestContextFilter;

@AutoConfiguration
public class SharedWebFilterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.security.trust-inbound-headers", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<Filter> merchantContextRegistration(MerchantContext merchantContext){
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(new MerchantContextFilter(merchantContext));
        registrationBean.setOrder(MerchantContextFilter.ORDER);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> requestContextFilterRegisteration(){
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(new RequestContextFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
