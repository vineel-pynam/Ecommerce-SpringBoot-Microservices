package com.vineelpynam.OrderService.service;

import com.vineelpynam.OrderService.entity.Order;
import com.vineelpynam.OrderService.exception.CustomException;
import com.vineelpynam.OrderService.external.client.PaymentService;
import com.vineelpynam.OrderService.external.client.ProductService;
import com.vineelpynam.OrderService.external.request.PaymentRequest;
import com.vineelpynam.OrderService.external.response.ProductResponse;
import com.vineelpynam.OrderService.model.OrderRequest;
import com.vineelpynam.OrderService.model.OrderResponse;
import com.vineelpynam.OrderService.external.response.PaymentResponse;
import com.vineelpynam.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static com.vineelpynam.OrderService.model.PaymentMode.*;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Placing Order Request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order With Status CREATED");

        Order order = Order.builder()
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .productId(orderRequest.getProductId())
                .amount(orderRequest.getTotalAmount())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service To Complete Payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .paymentMode(CASH)
                .build();

        String orderStatus = "";
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully, Changing Order Status to PLACE");
            orderStatus = "PLACED";

        }catch (Exception e){
            log.info(e.getMessage());
            log.info("Error Occured In Payment, Changing Order Status To PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);

        orderRepository.save(order);

        log.info("Order Successfully placed with Order ID: {}", order.getOrderId());

        return order.getOrderId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {

        log.info("Getting Order For OrderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        ()-> new CustomException(
                                "Order Not Found",
                                "ORDER_NOT_FOUND",
                                404
                        )
                );

        log.info("Invoking Product Service For Getting Product For Id: {}", order.getProductId());

        ProductResponse productResponse = productService.getProductById(order.getProductId()).getBody();

        OrderResponse.ProductDetails productDetails
                = OrderResponse.ProductDetails.builder()
                    .productName(productResponse.getProductName())
                    .productId(productResponse.getProductId())
                    .quantity(productResponse.getQuantity())
                    .price(productResponse.getPrice())
                    .build();


        log.info("Invoking Payment Service For Getting Payment Details For Order Id: {} ", order.getOrderId());

//        PaymentResponse paymentResponse = restTemplate.getForObject(
//                paymentServiceUrl + "order/" + order.getOrderId(),
//                PaymentResponse.class
//        );

        PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(order.getOrderId()).getBody();

        OrderResponse.PaymentDetails paymentDetails =
                OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .status(paymentResponse.getStatus())
                        .amount(paymentResponse.getAmount())
                        .build();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
    }
}
