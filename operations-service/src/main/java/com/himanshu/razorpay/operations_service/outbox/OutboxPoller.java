package com.himanshu.razorpay.operations_service.outbox;

import com.himanshu.razorpay.common_library.config.KafkaProperties;
import com.himanshu.razorpay.common_library.enums.OutboxStatus;
import com.himanshu.razorpay.operations_service.entity.OutboxEvent;
import com.himanshu.razorpay.operations_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedDelay = 5000)
    public void poll() {

        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for(OutboxEvent event : pendingEvents) {
            // everything per-event stays inside the try: resolving the topic or
            // reading the payload can throw too, and those failures need to be
            // recorded on the event rather than killing the whole poll
            try {
                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());

                Map<String, Object> envelope = Map.of(
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType().name(),
                        "aggregateId", event.getAggregateId().toString(),
                        "data", event.getPayload()
                );
                log.info("Publishing outbox event to kafka, eventID: {}, topic: {}, key: {}, envelope: {}", event.getId(), topic, key, envelope);

                kafkaTemplate.send(topic, key, envelope)
                        .get(5, TimeUnit.SECONDS);

                outboxResultHandler.handleEventPublished(event);
            }catch (Exception e){
                log.error("Outbox event failed, eventID: {}, attempts: {}", event.getId(), event.getAttempts(), e);
                outboxResultHandler.handleEventFailed(event, String.valueOf(e.getMessage()));
            }
        }
    }




    private String extractMerchantId(Map<String, Object> payload){
        Object value = payload.get("merchantId");
        return value != null ? value.toString() : "unknown";
    }
}
