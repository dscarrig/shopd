package com.backend.shopd.web.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.data.entity.OrderItemEntity;
import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.service.OrderService;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders/create
     * Creates an order with one or more items, validating and decrementing stock.
     */
    @PostMapping("/create")
    public ResponseEntity<OrderEntity> createOrder(@RequestBody OrderEntity entity) {
        System.out.println("Received order creation request: " + entity);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(entity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/status/{order_id}")
    public ResponseEntity<String> updateOrderStatus(@PathVariable UUID order_id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body("Missing 'status' field.");
            }
            orderService.updateOrderStatus(order_id, status);
            return ResponseEntity.ok("Order status updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/item/status/{order_id}")
    public ResponseEntity<Void> updateOrderItemStatus(@PathVariable UUID order_id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            orderService.updateOrderItemStatus(order_id, status);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * POST /api/orders/buy/{user_id}/{item_id}?quantity=1
     * Creates a single-item order for a user ("buy now"), decrementing stock.
     */
    @PostMapping("/buy/{user_id}/{item_id}")
    public ResponseEntity<OrderEntity> buyNow(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id,
            @RequestParam(defaultValue = "1") int quantity) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.buyNow(user_id, item_id, quantity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * GET /api/orders/{order_id}
     */
    @GetMapping("/{order_id}")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable UUID order_id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(order_id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/orders/user/{user_id}
     * Returns all orders for a user, newest first.
     */
    @GetMapping("/user/{user_id}")
    public List<OrderEntity> getOrdersByUser(@PathVariable UUID user_id) {
        return orderService.getOrdersByUser(user_id);
    }

    /**
     * GET /api/orders/item/{item_id}
     * Returns all orders containing a specific item.
     */
    @GetMapping("/item/{item_id}")
    public List<OrderEntity> getOrdersByItem(@PathVariable UUID item_id) {
        return orderService.getOrdersByItem(item_id);
    }

    /**
     * GET /api/orders/user/{user_id}/item/{item_id}
     * Returns all orders a user placed that contain a specific item.
     */
    @GetMapping("/user/{user_id}/item/{item_id}")
    public List<OrderEntity> getOrdersForUserAndItem(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return orderService.getOrdersForUserAndItem(user_id, item_id);
    }

    /**
     * GET /api/orders/user/{user_id}/item/{item_id}/has-purchased
     * Returns true if the user has ever ordered this item.
     */
    @GetMapping("/user/{user_id}/item/{item_id}/has-purchased")
    public ResponseEntity<Boolean> hasUserPurchasedItem(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(orderService.hasUserPurchasedItem(user_id, item_id));
    }

    /**
     * GET /api/orders/user/{user_id}/item/{item_id}/count
     * Returns the number of times this user has ordered this item across all orders.
     */
    @GetMapping("/user/{user_id}/item/{item_id}/count")
    public ResponseEntity<Long> purchaseCount(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(orderService.purchaseCountForUserAndItem(user_id, item_id));
    }

    /**
     * GET /api/orders/user/{user_id}/item/{item_id}/total-units
     * Returns the total units of this item the user has ordered.
     */
    @GetMapping("/user/{user_id}/item/{item_id}/total-units")
    public ResponseEntity<Long> totalUnitsPurchased(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(orderService.totalUnitsUserPurchasedItem(user_id, item_id));
    }

    /**
     * GET /api/orders/user/order-listings/{user_id}
     * Returns all OrderItemEntities whose ShopdItem is owned by the given user.
     */
    @GetMapping("/user/order-listings/{user_id}")
    public List<OrderItemEntity> getOrderItemsByUser(@PathVariable UUID user_id) {
        return orderService.getOrderItemsByUser(user_id);
    }

    @GetMapping("/order-item/shopd-item/{order_item_id}")
    public ShopdItem getShopdItemByOrderItemId(@PathVariable UUID order_item_id) {
        return orderService.getShopdItemByOrderItemId(order_item_id);
    }

    public ShopdItem[] getShopdItemsForOrderItems(List<OrderItemEntity> orderItems) {
        return orderItems.stream()
                .map(item -> orderService.getShopdItemByOrderItemId(item.getItemId()))
                .toArray(ShopdItem[]::new);
    }
}
