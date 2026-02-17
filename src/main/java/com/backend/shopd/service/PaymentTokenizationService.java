package com.backend.shopd.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock payment tokenization service that simulates a payment processor (e.g., Stripe, PayPal).
 * In production, replace this with an actual payment processor SDK/API.
 *
 * This service:
 * - Accepts sensitive card data
 * - Returns a secure token
 * - Stores the mapping internally (in production, this happens on the processor's servers)
 */
@Service
public class PaymentTokenizationService {
    
    // In production, this would be handled by the payment processor's servers
    private final Map<String, CardDetails> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Tokenizes payment information and returns a secure token.
     * In production, this would call a payment processor API (Stripe, PayPal, etc.)
     * 
     * @param cardNumber The card number (only last 4 digits should be stored locally)
     * @param cardHolderName The name on the card
     * @param expiryDate The card expiry date
     * @param cvv The CVV (NEVER stored, used only for tokenization)
     * @return A secure token representing the payment method
     */
    public String tokenize(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        // Validate card details
        validateCardDetails(cardNumber, expiryDate, cvv);
        
        // Generate a secure token
        String token = generateSecureToken();
        
        // In production, you would call the payment processor API here:
        // Example: stripe.tokens.create({ card: { number, exp_month, exp_year, cvc } })
        
        // Store card details (simulating what happens on the processor's side)
        // NOTE: In production, you NEVER store these details yourself
        CardDetails details = new CardDetails(
            cardNumber,
            cardHolderName,
            expiryDate,
            extractLastFourDigits(cardNumber)
        );
        tokenStore.put(token, details);
        
        return token;
    }
    
    /**
     * Retrieves the last 4 digits of the card for display purposes.
     * This is safe to store locally.
     */
    public String getLastFourDigitsByToken(String token) {
        CardDetails details = tokenStore.get(token);
        return details != null ? details.lastFourDigits : null;
    }
    
    /**
     * Simulates charging a card using the token.
     * In production, this would call: stripe.charges.create({ amount, token })
     */
    public boolean processPayment(String token, double amount) {
        if (!tokenStore.containsKey(token)) {
            throw new IllegalArgumentException("Invalid payment token");
        }
        
        // Simulate payment processing
        System.out.println("Processing payment of $" + amount + " with token: " + token);
        return true; // Simulated success
    }
    
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    private String extractLastFourDigits(String cardNumber) {
        String cleaned = cardNumber.replaceAll("\\s+", "");
        return cleaned.length() >= 4 ? cleaned.substring(cleaned.length() - 4) : cleaned;
    }
    
    private void validateCardDetails(String cardNumber, String expiryDate, String cvv) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }
        
        // Basic validation (in production, use a proper card validation library)
        String cleanedNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanedNumber.length() < 13 || cleanedNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number length");
        }
        
        if (cvv.length() < 3 || cvv.length() > 4) {
            throw new IllegalArgumentException("Invalid CVV length");
        }
    }
    
    /**
     * Internal class to store card details.
     * In production, this data lives on the payment processor's PCI-compliant servers.
     */
    private static class CardDetails {
        final String cardNumber;
        final String cardHolderName;
        final String expiryDate;
        final String lastFourDigits;
        
        CardDetails(String cardNumber, String cardHolderName, String expiryDate, String lastFourDigits) {
            this.cardNumber = cardNumber;
            this.cardHolderName = cardHolderName;
            this.expiryDate = expiryDate;
            this.lastFourDigits = lastFourDigits;
        }
    }
}
