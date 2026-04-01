package com.backend.shopd.data.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_item_purchases")
@Data
@NoArgsConstructor
public class UserItemPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_id", updatable = false, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID purchaseId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_at_purchase", nullable = false)
    private double unitPriceAtPurchase;

    @CreationTimestamp
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    public UserItemPurchase(UUID userId, UUID itemId, int quantity, double unitPriceAtPurchase) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }
}
