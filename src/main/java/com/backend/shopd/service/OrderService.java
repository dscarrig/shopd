package com.backend.shopd.service;

import org.springframework.stereotype.Service;
import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.data.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderEntity createOrder(OrderEntity entity) {
        return orderRepository.save(entity);
    }
    
}
