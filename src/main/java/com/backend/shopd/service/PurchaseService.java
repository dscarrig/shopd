package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.data.entity.UserItemPurchase;
import com.backend.shopd.data.repository.ShopdItemRepository;
import com.backend.shopd.data.repository.UserItemPurchaseRepository;

@Service
public class PurchaseService {

    private final UserItemPurchaseRepository purchaseRepository;
    private final ShopdItemRepository shopdItemRepository;

    public PurchaseService(UserItemPurchaseRepository purchaseRepository,
                           ShopdItemRepository shopdItemRepository) {
        this.purchaseRepository = purchaseRepository;
        this.shopdItemRepository = shopdItemRepository;
    }

    /**
     * Records a purchase and decrements the item's stock atomically.
     * Throws if the item does not exist or has insufficient stock.
     */
    @Transactional
    public UserItemPurchase recordPurchase(UUID userId, UUID itemId, int quantity) {
        ShopdItem item = shopdItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        if (!item.isAvailable()) {
            throw new IllegalStateException("Item is not available for purchase: " + itemId);
        }
        if (item.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Requested: " + quantity + ", available: " + item.getQuantity());
        }

        // Decrement stock; mark unavailable when stock reaches zero
        int newQuantity = item.getQuantity() - quantity;
        item.setQuantity(newQuantity);
        if (newQuantity == 0) {
            item.setAvailable(false);
        }
        shopdItemRepository.save(item);

        UserItemPurchase purchase = new UserItemPurchase(userId, itemId, quantity, item.getPrice());
        return purchaseRepository.save(purchase);
    }

    // Purchase history for a user, newest first
    public List<UserItemPurchase> getPurchasesByUser(UUID userId) {
        return purchaseRepository.findByUserIdOrderByPurchasedAtDesc(userId);
    }

    // All purchases recorded for an item
    public List<UserItemPurchase> getPurchasesByItem(UUID itemId) {
        return purchaseRepository.findByItemId(itemId);
    }

    // All purchases a specific user made for a specific item
    public List<UserItemPurchase> getPurchasesForUserAndItem(UUID userId, UUID itemId) {
        return purchaseRepository.findByUserIdAndItemId(userId, itemId);
    }

    // Has the user bought this item at least once?
    public boolean hasUserPurchasedItem(UUID userId, UUID itemId) {
        return purchaseRepository.existsByUserIdAndItemId(userId, itemId);
    }

    // Total number of purchase transactions for user+item
    public long purchaseCountForUserAndItem(UUID userId, UUID itemId) {
        return purchaseRepository.countByUserIdAndItemId(userId, itemId);
    }

    // Total units purchased by user for a specific item across all transactions
    public long totalUnitsUserPurchasedItem(UUID userId, UUID itemId) {
        return purchaseRepository.sumQuantityByUserIdAndItemId(userId, itemId);
    }
}
