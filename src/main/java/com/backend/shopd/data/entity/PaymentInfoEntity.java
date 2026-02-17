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
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "payment_token", nullable = false, unique = true)
    private String paymentToken;
    
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;
    
    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;
    
    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;
    
    @Column(name = "card_type")
    private String cardType; // e.g., "Visa", "Mastercard"
    
    // NOTE: CVV is NEVER stored (PCI DSS requirement)
    // Full card number is NEVER stored (stored securely by payment processor)
}
