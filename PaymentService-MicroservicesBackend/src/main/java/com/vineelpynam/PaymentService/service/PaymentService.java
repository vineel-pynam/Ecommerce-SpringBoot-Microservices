package com.vineelpynam.PaymentService.service;

import com.vineelpynam.PaymentService.model.PaymentRequest;
import com.vineelpynam.PaymentService.model.PaymentResponse;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);
}
