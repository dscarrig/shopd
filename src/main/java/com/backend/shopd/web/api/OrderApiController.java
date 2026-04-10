package com.backend.shopd.web.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/orders")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @PostMapping("create")
    public OrderEntity createOrder(@RequestBody OrderEntity entity) {
        System.out.println("Received order creation request: " + entity);
        OrderEntity order = orderService.createOrder(entity);
        
        return order;
    }

    @GetMapping("/{order_id}")
    public OrderEntity getOrderById(@PathVariable("order_id") UUID orderId) {
        System.out.println("Received request for order: " + orderId);
        return orderService.getOrderById(orderId);
    }
}
