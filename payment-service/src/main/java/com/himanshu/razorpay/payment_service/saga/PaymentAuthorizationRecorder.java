package com.himanshu.razorpay.payment_service.saga;

import com.himanshu.razorpay.common_library.enums.EventAggregateType;
import com.himanshu.razorpay.common_library.enums.OrderStatus;
import com.himanshu.razorpay.common_library.enums.PaymentEvent;
import com.himanshu.razorpay.common_library.enums.PaymentStatus;
import com.himanshu.razorpay.common_library.exception.BusinessRuleViolationException;
import com.himanshu.razorpay.common_library.exception.ResourceNotFoundException;
import com.himanshu.razorpay.payment_service.dto.request.PaymentInitRequest;
import com.himanshu.razorpay.payment_service.dto.response.PaymentResponse;
import com.himanshu.razorpay.payment_service.entity.OrderRecord;
import com.himanshu.razorpay.payment_service.entity.Payment;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentResult;
import com.himanshu.razorpay.payment_service.mapper.PaymentMapper;
import com.himanshu.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.himanshu.razorpay.payment_service.repository.OrderRepository;
import com.himanshu.razorpay.payment_service.repository.PaymentRepository;
import com.himanshu.razorpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAuthorizationRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitRequest request){
        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER", request.orderId()));

        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE",
                    "Order cannot accept payment in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .idempotencyKey(UUID.randomUUID().toString()) //TODO
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    public PaymentResponse compensateAuthorizationFailure(UUID paymentId,
                                                          String errorCode, String errorDescription){
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAILURE);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment = paymentRepository.save(payment);

        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse applyGatewayResult(UUID paymentId, PaymentResult result){
        log.info("Applying gateway result for paymentId : {}", paymentId);
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", paymentId));

        switch (result){
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationReference());
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAILURE);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> {
                log.warn("Invalid state");
                return null;
            }
        }
        publishStatusEvent(payment, "PAYMENT_CREATED");
        log.info("Successfully applied Gateway result for paymentId : {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    private void publishStatusEvent(Payment payment, String eventType){
        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("paymentId", payment.getId().toString(),
                        "orderId", payment.getOrder().getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod().name()));
    }


}
