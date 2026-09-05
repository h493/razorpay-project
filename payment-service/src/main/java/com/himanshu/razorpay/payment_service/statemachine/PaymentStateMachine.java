package com.himanshu.razorpay.payment_service.statemachine;


import com.himanshu.razorpay.common_library.enums.PaymentEvent;
import com.himanshu.razorpay.common_library.enums.PaymentStatus;
import com.himanshu.razorpay.common_library.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.himanshu.razorpay.common_library.enums.PaymentEvent.*;
import static com.himanshu.razorpay.common_library.enums.PaymentStatus.*;


@Component
public class PaymentStateMachine {

    private record Transition(PaymentStatus from, PaymentEvent event){}

    private static final Map<Transition, PaymentStatus> TRANSITION = Map.ofEntries(
            Map.entry(new Transition(CREATED, AUTHORIZE_ATTEMPT), AUTHORIZING),
            Map.entry(new Transition(AUTHORIZING, AUTHORIZE_SUCCESS), AUTHORIZED),
            Map.entry(new Transition(AUTHORIZING, AUTHORIZE_FAILURE), FAILED),
            Map.entry(new Transition(AUTHORIZED, CAPTURE_ATTEMPT), CAPTURING),
            Map.entry(new Transition(CAPTURING, CAPTURE_SUCCESS), CAPTURED),
            Map.entry(new Transition(CAPTURING, CAPTURE_FAILURE), AUTHORIZED),
            Map.entry(new Transition(CAPTURED, REFUND_INIT), PARTIALLY_REFUNDED),
            Map.entry(new Transition(PARTIALLY_REFUNDED, REFUND_COMPLETE), REFUNDED),
            Map.entry(new Transition(SETTLED, REFUND_INIT), PARTIALLY_REFUNDED),
            Map.entry(new Transition(SETTLED, REFUND_COMPLETE), REFUNDED),
            Map.entry(new Transition(CAPTURED, REFUND_COMPLETE), REFUNDED),
            Map.entry(new Transition(CAPTURED, SETTLE), SETTLED),
            Map.entry(new Transition(CREATED, CANCEL), CANCELLED),
            Map.entry(new Transition(AUTHORIZING, CANCEL), CANCELLED),
            Map.entry(new Transition(AUTHORIZED, CAPTURE_TIMEOUT), AUTH_EXPIRED)
    );

    public PaymentStatus transition(PaymentStatus current, PaymentEvent event){
        PaymentStatus next = TRANSITION.get(new Transition(current, event));
        if(next == null){
            throw new InvalidStateTransitionException(current.name(), event.name());
        }
        return next;
    }
}
