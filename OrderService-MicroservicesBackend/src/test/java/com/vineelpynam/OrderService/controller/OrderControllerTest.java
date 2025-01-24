package com.vineelpynam.OrderService.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.vineelpynam.OrderService.OrderServiceConfig;
import com.vineelpynam.OrderService.entity.Order;
import com.vineelpynam.OrderService.model.OrderRequest;
import com.vineelpynam.OrderService.model.OrderResponse;
import com.vineelpynam.OrderService.repository.OrderRepository;
import com.vineelpynam.OrderService.service.OrderService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.vineelpynam.OrderService.model.PaymentMode.CASH;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.util.StreamUtils.copyToString;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
public class OrderControllerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;


    @RegisterExtension
    static WireMockExtension wireMockServer =
            WireMockExtension.newInstance()
                    .options(WireMockConfiguration
                            .wireMockConfig()
                            .port(8999))
                    .build();

    private ObjectMapper objectMapper =
            new ObjectMapper()
                    .findAndRegisterModules()
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setUp() throws IOException {
        getProductDetailsResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() {
//        circuitBreakerRegistry.circuitBreaker("external").reset();
        wireMockServer.stubFor(put(urlMatching("/product/reduceQuantity/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        circuitBreakerRegistry.circuitBreaker("external").reset();
        wireMockServer.stubFor(get(urlMatching("/payment/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("mock/GetPayment.json"),
                                Charset.defaultCharset()
                        ))));
    }

    private void doPayment() {
        circuitBreakerRegistry.circuitBreaker("external").reset();
        wireMockServer.stubFor(post(urlEqualTo("/payment"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getProductDetailsResponse() throws IOException {
        // GET - /product/1
        wireMockServer.stubFor(get("/product/1")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("mock/GetProduct.json"),
                                Charset.defaultCharset()
                        ))));
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(CASH)
                .totalAmount(2000)
                .build();
    }

//    @Test
//    public void test_WhenPlaceOrder_DoPaymentSuccess() throws Exception {
//        OrderRequest orderRequest = getMockOrderRequest();
//        MvcResult mvcResult = mockMvc
//                .perform(MockMvcRequestBuilders.post("/order/placeOrder")
//                .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(objectMapper.writeValueAsString(orderRequest)))
//                        .andExpect(MockMvcResultMatchers.status().isOk())
//                        .andReturn();
//        String orderId = mvcResult.getResponse().getContentAsString();
//        Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
//
//        assertTrue(order.isPresent());
//        assertEquals(Long.valueOf(orderId), order.get().getOrderId());
//        assertEquals("PLACED", order.get().getOrderStatus());
//
//    }

    @Test
    public void test_WhenPlaceOrderWithWrongAccess_thenThrows403() throws Exception {
        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/order/placeOrder")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }


//    @DisplayName("Get Order - Success Scenario")
//    @Test
//    public void test_WhenGetOrderSuccess() throws Exception {
//
//        Order order = Order.builder()
//                .quantity(2)
//                .productId(1)
//                .orderDate(Instant.now())
//                .orderStatus("CREATED")
//                .amount(200)
//                .build();
//        orderRepository.save(order);
//
//        MvcResult mvcResult =
//                mockMvc.perform(MockMvcRequestBuilders.get("/order/1")
//                                .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
//                                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andReturn();
//
//        String actualResponse = mvcResult.getResponse().getContentAsString();
//        Order orderFromDb = orderRepository.findById(1L).get();
//
//        String expectedResponse = getOrderResponse(orderFromDb);
//
//        assertEquals(actualResponse, expectedResponse);
//
//    }

    private String getOrderResponse(Order order) throws IOException {
        OrderResponse.ProductDetails productDetails =
                objectMapper.readValue(
                        copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("/mock/GetProduct.json"),
                                Charset.defaultCharset()
                        ), OrderResponse.ProductDetails.class);

        OrderResponse.PaymentDetails paymentDetails =
                objectMapper.readValue(
                        copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("/mock/GetPayment.json"),
                                Charset.defaultCharset()
                        ), OrderResponse.PaymentDetails.class);

        OrderResponse orderResponse = OrderResponse.builder()
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return objectMapper.writeValueAsString(orderResponse);
    }


}