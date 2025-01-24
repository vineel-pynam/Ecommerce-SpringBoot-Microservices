package com.vineelpynam.OrderService.service;

import com.vineelpynam.OrderService.model.OrderRequest;
import com.vineelpynam.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
