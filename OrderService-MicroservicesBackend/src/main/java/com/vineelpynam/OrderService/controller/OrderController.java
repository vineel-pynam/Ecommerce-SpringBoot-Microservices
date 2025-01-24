package com.vineelpynam.OrderService.controller;

import com.vineelpynam.OrderService.model.OrderRequest;
import com.vineelpynam.OrderService.model.OrderResponse;
import com.vineelpynam.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('Customer')")
    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        log.info("Invoking placeOrder...");
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Order Id: {}", orderId);
        return  new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable("orderId") long orderId){
        log.info("Invoking getOrderDetails...");
        return new ResponseEntity<>(
                orderService.getOrderDetails(orderId),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer')")
    @GetMapping("/test")
    public ResponseEntity<String> testMethod(){
        log.info("Invoking Test Method");
        return new ResponseEntity<String>("Order Service is Ok", HttpStatus.OK);
    }
}
