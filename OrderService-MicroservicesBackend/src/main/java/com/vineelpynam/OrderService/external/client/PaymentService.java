package com.vineelpynam.OrderService.external.client;

import com.vineelpynam.OrderService.exception.CustomException;
import com.vineelpynam.OrderService.external.request.PaymentRequest;
import com.vineelpynam.OrderService.external.response.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "payment", url = "${microservices.payment}")
public interface PaymentService {
    @PostMapping
    Long doPayment(PaymentRequest paymentRequest);

    @GetMapping("/order/{orderId}")
    ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable("orderId") long orderId);

    default Long fallback(Exception e){
        throw new CustomException("Payment Service is not availble", "UNAVAILABLE", 500);
    }
}
