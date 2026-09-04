package com.himanshu.razorpay.common_library.config;


import com.himanshu.razorpay.common_library.enums.EventAggregateType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Registered via {@code @EnableConfigurationProperties} on the application class.
 * Records bind through their constructor, which Spring Boot only does for beans
 * registered that way — annotating this {@code @Component} would silently yield
 * an empty map instead.
 */
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        Map<String, String> topics
) {

    public KafkaProperties {
        topics = topics == null ? Map.of() : topics;
    }

    public String topicFor(EventAggregateType aggregateType) {
        String topic = topics.get(aggregateType.name().toLowerCase());

        if(topic == null){
            throw new IllegalStateException("No kafka topic is configured for aggregateType: " + aggregateType);
        }

        return topic;
    }
}
