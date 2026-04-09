package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

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
        // Ensure orderId is null so Hibernate treats this as a new entity
        entity.setOrderId(null);
        return orderRepository.save(entity);
    }

    public List<OrderEntity> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
}
