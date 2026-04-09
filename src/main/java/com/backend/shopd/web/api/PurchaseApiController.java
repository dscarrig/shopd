package com.backend.shopd.web.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.data.entity.UserItemPurchase;
import com.backend.shopd.service.PurchaseService;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class PurchaseApiController {

    private final PurchaseService purchaseService;

    public PurchaseApiController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * POST /api/purchases/{user_id}/{item_id}?quantity=1
     * Records a purchase and decrements item stock.
     */
    @PostMapping("/{user_id}/{item_id}")
    public ResponseEntity<UserItemPurchase> recordPurchase(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id,
            @RequestParam(defaultValue = "1") int quantity) {
        try {
            UserItemPurchase purchase = purchaseService.recordPurchase(user_id, item_id, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * GET /api/purchases/user/purchases/{user_id}
     * Returns full purchase history for a user, newest first.
     */
    @GetMapping("/user/{user_id}")
    public List<UserItemPurchase> getPurchasesByUser(@PathVariable UUID user_id) {
        System.out.println("Received request for purchase history of user: " + user_id);
        return purchaseService.getPurchasesByUser(user_id);
    }

    @GetMapping("/user/orders/{user_id}")
    public List<OrderEntity> getOrdersByUser(@PathVariable UUID user_id) {
        System.out.println("Received request for orders of user: " + user_id);
        return purchaseService.getOrdersByUser(user_id);
    }
    

    /**
     * GET /api/purchases/item/{item_id}
     * Returns all purchase records for a specific item.
     */
    @GetMapping("/item/{item_id}")
    public List<UserItemPurchase> getPurchasesByItem(@PathVariable UUID item_id) {
        return purchaseService.getPurchasesByItem(item_id);
    }

    /**
     * GET /api/purchases/{user_id}/{item_id}
     * Returns all purchase transactions a user made for a specific item.
     */
    @GetMapping("/{user_id}/{item_id}")
    public List<UserItemPurchase> getPurchasesForUserAndItem(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return purchaseService.getPurchasesForUserAndItem(user_id, item_id);
    }

    /**
     * GET /api/purchases/{user_id}/{item_id}/has-purchased
     * Returns true/false — has this user bought this item at least once?
     */
    @GetMapping("/{user_id}/{item_id}/has-purchased")
    public ResponseEntity<Boolean> hasUserPurchasedItem(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(purchaseService.hasUserPurchasedItem(user_id, item_id));
    }

    /**
     * GET /api/purchases/{user_id}/{item_id}/count
     * Returns the number of separate purchase transactions for this user+item.
     */
    @GetMapping("/{user_id}/{item_id}/count")
    public ResponseEntity<Long> purchaseCount(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(purchaseService.purchaseCountForUserAndItem(user_id, item_id));
    }

    /**
     * GET /api/purchases/{user_id}/{item_id}/total-units
     * Returns the total number of units this user has purchased of this item.
     */
    @GetMapping("/{user_id}/{item_id}/total-units")
    public ResponseEntity<Long> totalUnitsPurchased(
            @PathVariable UUID user_id,
            @PathVariable UUID item_id) {
        return ResponseEntity.ok(purchaseService.totalUnitsUserPurchasedItem(user_id, item_id));
    }
}
