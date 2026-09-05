package com.himanshu.razorpay.operations_service.outbox;

import com.himanshu.razorpay.common_library.enums.EventAggregateType;
import com.himanshu.razorpay.operations_service.entity.OutboxEvent;
import com.himanshu.razorpay.operations_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public void publish(EventAggregateType aggregateType,
                        UUID aggregateId,
                        String eventType,
                        Map<String, Object> payload){

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
