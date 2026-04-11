package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.data.entity.OrderItemEntity;
import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.data.repository.OrderRepository;
import com.backend.shopd.data.repository.OrderItemRepository;
import com.backend.shopd.data.repository.ShopdItemRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopdItemRepository shopdItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ShopdItemRepository shopdItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shopdItemRepository = shopdItemRepository;
    }

    /**
     * Creates an order, validating and decrementing stock for each item atomically.
     * Throws if any item does not exist, is unavailable, or has insufficient stock.
     */
    @Transactional
    public OrderEntity createOrder(OrderEntity entity) {
        for (OrderItemEntity orderItem : entity.getItems()) {
            ShopdItem shopdItem = shopdItemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + orderItem.getItemId()));
            if (!shopdItem.isAvailable()) {
                throw new IllegalStateException("Item is not available: " + orderItem.getItemId());
            }
            if (shopdItem.getQuantity() < orderItem.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for item: " + orderItem.getItemId()
                        + ". Requested: " + orderItem.getQuantity()
                        + ", available: " + shopdItem.getQuantity());
            }
            int newQty = shopdItem.getQuantity() - orderItem.getQuantity();
            shopdItem.setQuantity(newQty);
            if (newQty == 0) {
                shopdItem.setAvailable(false);
            }
            shopdItemRepository.save(shopdItem);
        }
        entity.setOrderId(null);
        return orderRepository.save(entity);
    }

    /**
     * Creates a single-item order for the given user ("buy now").
     * Validates and decrements stock atomically.
     */
    @Transactional
    public OrderEntity buyNow(UUID userId, UUID itemId, int quantity) {
        ShopdItem shopdItem = shopdItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        if (!shopdItem.isAvailable()) {
            throw new IllegalStateException("Item is not available: " + itemId);
        }
        if (shopdItem.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Requested: " + quantity + ", available: " + shopdItem.getQuantity());
        }
        int newQty = shopdItem.getQuantity() - quantity;
        shopdItem.setQuantity(newQty);
        if (newQty == 0) {
            shopdItem.setAvailable(false);
        }
        shopdItemRepository.save(shopdItem);

        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setItemId(itemId);
        orderItem.setName(shopdItem.getName());
        orderItem.setPrice(shopdItem.getPrice());
        orderItem.setQuantity(quantity);

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setItems(List.of(orderItem));
        order.setTotalAmount(shopdItem.getPrice() * quantity);
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    public OrderEntity getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public List<OrderEntity> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<OrderEntity> getOrdersByItem(UUID itemId) {
        return orderRepository.findByItemId(itemId);
    }

    public List<OrderEntity> getOrdersForUserAndItem(UUID userId, UUID itemId) {
        return orderRepository.findByUserIdAndItemId(userId, itemId);
    }

    public boolean hasUserPurchasedItem(UUID userId, UUID itemId) {
        return orderRepository.existsByUserIdAndItemId(userId, itemId);
    }

    public long purchaseCountForUserAndItem(UUID userId, UUID itemId) {
        return orderRepository.countItemsByUserIdAndItemId(userId, itemId);
    }

    public long totalUnitsUserPurchasedItem(UUID userId, UUID itemId) {
        return orderRepository.sumItemQuantityByUserIdAndItemId(userId, itemId);
    }

    public List<OrderItemEntity> getOrderItemsByUser(UUID userId) {
        return orderRepository.findOrderItemsByShopdItemUserId(userId);
    }

    public ShopdItem getShopdItemByOrderItemId(UUID orderItemId) {
        return shopdItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("ShopdItem not found for OrderItem: " + orderItemId));
    }

    public void updateOrderStatus(UUID order_id, String status) {
        OrderEntity order = orderRepository.findById(order_id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order_id));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void updateOrderItemStatus(UUID orderItemId, String status) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found: " + orderItemId));
        orderItem.setStatus(status);
        orderItemRepository.save(orderItem);
    }

    public UUID getUserIdByOrderItemId(UUID order_item_id) {
        OrderEntity order = orderRepository.findByOrderItemId(order_item_id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for OrderItem: " + order_item_id));
        return order.getUserId();
    }
}
