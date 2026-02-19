package com.backend.shopd.data.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shopd_items")
@Data
public class ShopdItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", updatable = false, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "price", nullable = false)
    private double price;
    @Column(name = "image_url", nullable = true)
    private String imageUrl;
    @Column(name = "category", nullable = false)
    private String category;
    @Column(name = "available", nullable = false)
    private boolean available;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
