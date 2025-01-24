package com.vineelpynam.OrderService.service;

import com.vineelpynam.OrderService.entity.Order;
import com.vineelpynam.OrderService.exception.CustomException;
import com.vineelpynam.OrderService.external.client.PaymentService;
import com.vineelpynam.OrderService.external.client.ProductService;
import com.vineelpynam.OrderService.external.request.PaymentRequest;
import com.vineelpynam.OrderService.external.response.PaymentResponse;
import com.vineelpynam.OrderService.external.response.ProductResponse;
import com.vineelpynam.OrderService.model.OrderRequest;
import com.vineelpynam.OrderService.model.OrderResponse;
import com.vineelpynam.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static com.vineelpynam.OrderService.model.PaymentMode.CASH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();


    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        // Mocking
        Order order = getMockOrder();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        when(productService.getProductById(anyLong()))
                .thenReturn(new ResponseEntity<>(getMockProductResponse(), HttpStatus.OK));
        when(paymentService.getPaymentDetailsByOrderId(anyLong()))
                .thenReturn(new ResponseEntity<>(getMockPaymentResponse(), HttpStatus.OK));

        // Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        // Verification
        verify(orderRepository, times(1)).findById(anyLong());
        verify(productService, times(1)).getProductById(anyLong());
        verify(paymentService, times(1)).getPaymentDetailsByOrderId(anyLong());

        // Assertion
        assertNotNull(orderResponse);
        assertEquals(order.getOrderId(), orderResponse.getOrderId());

    }

    @DisplayName("Get Order Details - Failure Scenario")
    @Test
    void test_When_Get_Order_NOT_FOUNF_Then_NOT_FOUND(){

        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> orderService.getOrderDetails(anyLong()));

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        verify(orderRepository, times(1)).findById(anyLong());
    }

    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        Order order = getMockOrder();

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(1L);

        long orderId = orderService.placeOrder(getMockOrderRequest());

        verify(orderRepository, times(2)).save(any());
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        assertEquals(order.getOrderId(), orderId);
    }

    @DisplayName("Place Order - Payment Fail Scenario")
    @Test
    void test_When_Place_Order_Payment_Fails_then_Order_Placed(){
        Order order = getMockOrder();

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(getMockOrderRequest());

        verify(orderRepository, times(2)).save(any());
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        assertEquals(order.getOrderId(), orderId);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .totalAmount(100)
                .paymentMode(CASH)
                .productId(1)
                .quantity(2)
                .build();
    }


    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(CASH)
                .status("SUCCESS")
                .orderId(1)
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productName("Iphone")
                .price(100)
                .quantity(2)
                .productId(1)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderId(1)
                .amount(100)
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .productId(1)
                .quantity(2)
                .build();
    }
}