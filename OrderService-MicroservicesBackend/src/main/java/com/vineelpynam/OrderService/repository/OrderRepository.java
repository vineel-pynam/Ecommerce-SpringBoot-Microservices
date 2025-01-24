package com.vineelpynam.OrderService.repository;

import com.vineelpynam.OrderService.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
