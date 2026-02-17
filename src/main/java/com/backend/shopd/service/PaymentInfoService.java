package com.backend.shopd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopd.data.entity.PaymentInfoEntity;
import com.backend.shopd.data.repository.PaymentInfoRepository;

/**
 * Service for managing payment information using tokenization.
 * This approach complies with PCI DSS by never storing sensitive card data.
 */
@Service
public class PaymentInfoService {
    private final PaymentInfoRepository paymentInfoRepository;
    private final PaymentTokenizationService tokenizationService;

    public PaymentInfoService(
            PaymentInfoRepository paymentInfoRepository,
            PaymentTokenizationService tokenizationService) {
        this.paymentInfoRepository = paymentInfoRepository;
        this.tokenizationService = tokenizationService;
    }

    /**
     * Saves payment information securely using tokenization.
     * 
     * Security features:
     * - Card number is tokenized and never stored in our database
     * - CVV is used only for tokenization and NEVER stored (PCI DSS requirement)
     * - Only safe data (token, last 4 digits, expiry) is persisted
     * 
     * @param userId The user ID
     * @param cardNumber The full card number (only used for tokenization)
     * @param cardHolderName The name on the card
     * @param expiryDate The card expiry date (MM/YY format)
     * @param cvv The CVV (used for tokenization only, never stored)
     * @return The saved PaymentInfoEntity with token
     */
    @Transactional
    public PaymentInfoEntity savePaymentInfo(
            String userId,
            String cardNumber,
            String cardHolderName,
            String expiryDate,
            String cvv) {
        
        // Step 1: Tokenize the card data (CVV is used here but never stored)
        String paymentToken = tokenizationService.tokenize(
            cardNumber,
            cardHolderName,
            expiryDate,
            cvv
        );
        
        // Step 2: Get last 4 digits for display purposes (safe to store)
        String lastFour = tokenizationService.getLastFourDigitsByToken(paymentToken);
        
        // Step 3: Detect card type from card number
        String cardType = detectCardType(cardNumber);
        
        // Step 4: Create entity with only safe data
        PaymentInfoEntity entity = new PaymentInfoEntity();
        entity.setUserId(userId);
        entity.setPaymentToken(paymentToken);
        entity.setCardHolderName(cardHolderName);
        entity.setLastFourDigits(lastFour);
        entity.setExpirationDate(expiryDate);
        entity.setCardType(cardType);
        
        // Step 5: Save to database (no sensitive data stored)
        return paymentInfoRepository.save(entity);
    }
    
    /**
     * Process a payment using a stored payment token.
     * 
     * @param paymentId The payment info ID
     * @param amount The amount to charge
     * @return true if payment was successful
     */
    @Transactional
    public boolean processPayment(String paymentId, double amount) {
        PaymentInfoEntity paymentInfo = paymentInfoRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment info not found"));
        
        // Use the token to process payment (handled by payment processor)
        return tokenizationService.processPayment(paymentInfo.getPaymentToken(), amount);
    }
    
    /**
     * Detects the card type from the card number.
     * Basic implementation - can be enhanced with more card types.
     */
    private String detectCardType(String cardNumber) {
        String cleaned = cardNumber.replaceAll("\\s+", "");
        
        if (cleaned.startsWith("4")) {
            return "Visa";
        } else if (cleaned.matches("^5[1-5].*")) {
            return "Mastercard";
        } else if (cleaned.matches("^3[47].*")) {
            return "American Express";
        } else if (cleaned.matches("^6(?:011|5).*")) {
            return "Discover";
        }
        
        return "Unknown";
    }
}
