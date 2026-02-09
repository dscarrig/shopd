package com.backend.shopd.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payment_info")
@Data
public class PaymentInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", updatable = false, nullable = false)
    private String id;
    @Column(name = "card_number", nullable = false)
    private String cardNumber;
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;
    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;
    @Column(name = "cvv", nullable = false)
    private String cvv;
}
