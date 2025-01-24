package com.vineelpynam.PaymentService.service;

import com.vineelpynam.PaymentService.entity.TransactionDetails;
import com.vineelpynam.PaymentService.model.PaymentMode;
import com.vineelpynam.PaymentService.model.PaymentRequest;
import com.vineelpynam.PaymentService.model.PaymentResponse;
import com.vineelpynam.PaymentService.repository.TransactionDetailsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public Long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details: {}", paymentRequest);

        TransactionDetails transactionDetails = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(String.valueOf(paymentRequest.getPaymentMode()))
                .amount(paymentRequest.getAmount())
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .paymentStatus("SUCCESS")
                .build();

        transactionDetails = transactionDetailsRepository.save(transactionDetails);
        log.info("Transaction Completed With Id: {}", transactionDetails.getTransactionId());

        return transactionDetails.getTransactionId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Getting payment details for Order Id: {} ", orderId);

        TransactionDetails transactionDetails =
                transactionDetailsRepository.findByOrderId(orderId);

        return PaymentResponse.builder()
                .paymentId(transactionDetails.getTransactionId())
                .status(transactionDetails.getPaymentStatus())
                .amount(transactionDetails.getAmount())
                .orderId(transactionDetails.getOrderId())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .paymentDate(transactionDetails.getPaymentDate())
                .build();

    }
}
