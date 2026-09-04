package com.himanshu.razorpay.common_library.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(KafkaProperties.class)
public class SharedKafkaAutoConfiguration {

}
