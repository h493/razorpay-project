package com.himanshu.razorpay.payment_service.service.impl;


import com.himanshu.razorpay.common_library.enums.EventAggregateType;
import com.himanshu.razorpay.common_library.enums.OrderStatus;
import com.himanshu.razorpay.common_library.exception.BusinessRuleViolationException;
import com.himanshu.razorpay.common_library.exception.DuplicateResourceException;
import com.himanshu.razorpay.common_library.exception.ResourceNotFoundException;
import com.himanshu.razorpay.payment_service.dto.request.CreateOrderRequest;
import com.himanshu.razorpay.payment_service.dto.response.OrderResponse;
import com.himanshu.razorpay.payment_service.dto.response.PaymentResponse;
import com.himanshu.razorpay.payment_service.entity.OrderRecord;
import com.himanshu.razorpay.payment_service.entity.Payment;
import com.himanshu.razorpay.payment_service.mapper.OrderMapper;
import com.himanshu.razorpay.payment_service.mapper.PaymentMapper;
import com.himanshu.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.himanshu.razorpay.payment_service.repository.OrderRepository;
import com.himanshu.razorpay.payment_service.repository.PaymentRepository;
import com.himanshu.razorpay.payment_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final OutboxEventPublisher eventPublisher;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + request.receipt());
        }

        UUID customerId = null;
        if(request.customer() != null){
            customerId = customerService.findOrCreate(
                    merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()
            );
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        eventPublisher.publish(
                EventAggregateType.ORDER,
                order.getId(),
                "ORDER_CREATED",
                Map.of("orderId", order.getId().toString(),
                        "merchantId", order.getMerchantId().toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getAmountUnits()
        ));

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
         //merchantId ->  any merchant should not able to see other merchant order data
         OrderRecord orderRecord =  orderRepository.findByIdAndMerchantId(orderId, merchantId)
                 .orElseThrow(() -> new ResourceNotFoundException("order", orderId));
         return orderMapper.toResponse(orderRecord);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order =  orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order", orderId));

        if(OrderStatus.CANCELLED == order.getOrderStatus() || OrderStatus.PAID == order.getOrderStatus()){
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCLEL",
                    "Cannot cancel order with status: " + order.getOrderStatus().name());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        eventPublisher.publish(
                EventAggregateType.ORDER,
                order.getId(),
                "ORDER_CANCELLED",
                Map.of("orderId", order.getId().toString(),
                        "merchantId", order.getMerchantId().toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getAmountUnits()
                ));

        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order", orderId));

        List<Payment> paymentList = paymentRepository.findByOrder_Id(orderId);

        return paymentMapper.toResponseList(paymentList);
    }
}
