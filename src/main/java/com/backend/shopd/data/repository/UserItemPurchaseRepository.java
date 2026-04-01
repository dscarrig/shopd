package com.backend.shopd.data.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.shopd.data.entity.UserItemPurchase;

@Repository
public interface UserItemPurchaseRepository extends JpaRepository<UserItemPurchase, UUID> {

    // All purchases made by a user, newest first
    List<UserItemPurchase> findByUserIdOrderByPurchasedAtDesc(UUID userId);

    // All purchases for a specific item (who bought it)
    List<UserItemPurchase> findByItemId(UUID itemId);

    // All purchases for a specific user+item pair (repeat purchases)
    List<UserItemPurchase> findByUserIdAndItemId(UUID userId, UUID itemId);

    // Whether a user has purchased a specific item at least once
    boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

    // Total number of times a user purchased a specific item
    long countByUserIdAndItemId(UUID userId, UUID itemId);

    // Total units a user has purchased of a specific item
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM UserItemPurchase p WHERE p.userId = :userId AND p.itemId = :itemId")
    long sumQuantityByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);
}
