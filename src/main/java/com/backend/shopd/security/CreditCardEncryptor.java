package com.backend.shopd.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts credit card numbers
 * when saving to and reading from the database
 */
@Converter(autoApply = true)
@Component
public class CreditCardEncryptor implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService service) {
        CreditCardEncryptor.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isEmpty() || plainText.equals("-1")) {
            return plainText;
        }
        return encryptionService.encrypt(plainText);
    }

    @Override
    public String convertToEntityAttribute(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty() || encryptedText.equals("-1")) {
            return encryptedText;
        }
        try {
            return encryptionService.decrypt(encryptedText);
        } catch (Exception e) {
            // If decryption fails, return the original value (for backward compatibility)
            return encryptedText;
        }
    }
}
