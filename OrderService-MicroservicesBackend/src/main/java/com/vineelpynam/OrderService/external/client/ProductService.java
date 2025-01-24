package com.vineelpynam.OrderService.external.client;

import com.vineelpynam.OrderService.exception.CustomException;
import com.vineelpynam.OrderService.external.response.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "product", url = "${microservices.product}")
public interface ProductService {
    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(
            @PathVariable("id") long productId,
            @RequestParam long quantity
    );

    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId );

    default ResponseEntity<Void> fallback(Exception e){
        throw new CustomException("Product Service is not availble", "UNAVAILABLE", 500);
    }
}
