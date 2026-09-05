package com.himanshu.razorpay.payment_service.repository;

import com.himanshu.razorpay.common_library.enums.OutboxStatus;
import com.himanshu.razorpay.payment_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
