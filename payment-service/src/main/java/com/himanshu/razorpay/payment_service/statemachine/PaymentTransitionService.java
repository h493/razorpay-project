package com.himanshu.razorpay.payment_service.statemachine;

import com.himanshu.razorpay.common_library.enums.PaymentActor;
import com.himanshu.razorpay.common_library.enums.PaymentEvent;
import com.himanshu.razorpay.common_library.enums.PaymentStatus;
import com.himanshu.razorpay.payment_service.entity.Payment;
import com.himanshu.razorpay.payment_service.entity.PaymentTransitionLog;
import com.himanshu.razorpay.payment_service.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event){
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), event);

        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(event)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO : Fetch merchant context to identify actor
                .occurredAt(LocalDateTime.now())
                .build();

        paymentTransitionLogRepository.save(log);
        payment.setStatus(next);
        return next;
    }
}
