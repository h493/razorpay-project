package com.himanshu.razorpay.operations_service.repository;

import com.himanshu.razorpay.common_library.enums.OutboxStatus;
import com.himanshu.razorpay.operations_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);
}
