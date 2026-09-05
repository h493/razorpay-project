package com.himanshu.razorpay.payment_service.simulator;


import com.himanshu.razorpay.common_library.enums.ChaosMode;
import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {

    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String, MethodSimulatorConfig> methods = new HashMap<>();


    @Getter
    @Setter
    public static class MethodSimulatorConfig{
        private Integer minDelaySeconds = 1;
        private Integer maxDelaySeconds = 5;
        private Integer sucessRate = 80;
    }

    public MethodSimulatorConfig configFor(PaymentMethod method){
            return methods.getOrDefault(method.name(), new MethodSimulatorConfig());
    }
}
