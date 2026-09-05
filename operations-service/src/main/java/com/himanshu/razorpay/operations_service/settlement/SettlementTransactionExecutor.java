package com.himanshu.razorpay.operations_service.settlement;


import com.himanshu.razorpay.common_library.dto.PaymentSettlementView;
import com.himanshu.razorpay.common_library.dto.SettlementBankDetails;
import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.common_library.enums.EventAggregateType;
import com.himanshu.razorpay.common_library.enums.SettlementStatus;
import com.himanshu.razorpay.common_library.exception.ResourceNotFoundException;
import com.himanshu.razorpay.operations_service.client.MerchantServiceClient;
import com.himanshu.razorpay.operations_service.client.PaymentServiceClient;
import com.himanshu.razorpay.operations_service.entity.Settlement;
import com.himanshu.razorpay.operations_service.entity.SettlementPayment;
import com.himanshu.razorpay.operations_service.entity.SettlementPaymentId;
import com.himanshu.razorpay.operations_service.outbox.OutboxEventPublisher;
import com.himanshu.razorpay.operations_service.repository.SettlementPaymentRepository;
import com.himanshu.razorpay.operations_service.repository.SettlementRepository;
import com.himanshu.razorpay.operations_service.settlement.dto.BankTransferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementTransactionExecutor {

    private static final double FEE_RATE = 0.02;
    private static final double GST_RATE = 0.18;


    private final PaymentServiceClient paymentServiceClient;
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final MerchantServiceClient merchantServiceClient;
    private final BankTransferProcessor bankTransferProcessor;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void processForMerchant(UUID merchantId, LocalDate settlementDate) {
        List<PaymentSettlementView> unsettledPayments = paymentServiceClient.findUnsettledCaptured(merchantId);
        if (unsettledPayments.isEmpty()) return;

        log.info("Processing {} unsettled payments for merchantId:{} on {} date",
                unsettledPayments.size(), merchantId, settlementDate);
        Long grossAmount = unsettledPayments.stream()
                .map(PaymentSettlementView::amountUnits)
                .reduce(Long::sum)
                .orElse(0L);

        Money gross = Money.of(grossAmount, unsettledPayments.getFirst().currency());

        long fee = Math.round(gross.getAmountUnits() * FEE_RATE);
        long gst = Math.round(fee * GST_RATE);
        Money feeAmount = Money.of(fee, gross.getCurrency());
        Money gstAmount = Money.of(gst, gross.getCurrency());
        Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

        Settlement settlement = Settlement.builder()
                .merchantId(merchantId)
                .grossAmount(gross)
                .feeAmount(feeAmount)
                .gstAmount(gstAmount)
                .netAmount(netAmount)
                .status(SettlementStatus.INITIATED)
                .build();

        settlementRepository.save(settlement);
        try {
            List<SettlementPayment> links = new ArrayList<>();
            for (PaymentSettlementView p : unsettledPayments) {
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(), p.paymentId()))
                        .settlement(settlement)
                        .build());
            }

            settlementPaymentRepository.saveAll(links);


            SettlementBankDetails settlementBankDetails = merchantServiceClient.getSettlementBankDetails(merchantId);
            // call the bankTransferService to transfer netamount to merchant settlement bank details
            BankTransferResult bankTransferResult = bankTransferProcessor.initiate(settlement.getId(), merchantId,
                    netAmount, settlementBankDetails.accountNumber(), settlementBankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTransferResult.registrationRef());

            settlementRepository.save(settlement);
        } catch (Exception e) {
            log.error("Settlement failed for settlementId: {} on date: {}", settlement.getId(), settlementDate);
            settlement.setStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }
    }

    @Transactional
    public void resolveTransfer(UUID settlementId,
                                String errorCode, String errorDescription){
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if(settlement.getStatus() != SettlementStatus.TRANSFER_PENDING){
            log.info("Settlement resolved , skipping for id : {}",  settlement.getId());
            return;
        }

        if(errorCode == null){ //success
            settlement.setStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);

            List<SettlementPayment> settlementPaymentList = settlementPaymentRepository.findBySettlement(settlement);
            List<UUID> paymentIds = settlementPaymentList.stream()
                    .map(sp -> sp.getId().getPaymentId())
                    .toList();
            paymentServiceClient.markSettled(paymentIds);

            log.info("Settlement processed sucessfully, settlementId: {}", settlementId);
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT, settlementId,
                    "SETTLEMENT_PROCESSED", Map.of(
                            "settlementId", settlementId,
                            "merchantId", settlement.getMerchantId(),
                            "status", settlement.getStatus().name(),
                            "settlementAmount", settlement.getNetAmount().getAmountUnits(),
                            "settlementAmountCurrency", settlement.getNetAmount().getCurrency()
                    ));
        }else {  // failed
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode + " : " + errorDescription);
            settlementRepository.save(settlement);
            log.info("Settlement failed for, settlementId: {}", settlementId);
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT, settlementId,
                    "SETTLEMENT_FAILED", Map.of(
                            "settlementId", settlementId,
                            "merchantId", settlement.getMerchantId(),
                            "status", settlement.getStatus().name(),
                            "settlementAmount", settlement.getNetAmount().getAmountUnits(),
                            "settlementAmountCurrency", settlement.getNetAmount().getCurrency()
                    ));
        }


    }
}
