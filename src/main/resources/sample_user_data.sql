-- Sample user data for testing purposes
-- Passwords are hashed using BCrypt
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, ADDRESS_INFO, DEFAULT_ADDRESS, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER) 
VALUES (gen_random_uuid(), 'admin', '$2a$10$Dow1jZp8K1JY5Z6bX9EeuOa8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'admin@example.com', 'ADMIN', '["123 Admin St", "456 Admin Ave"]', '123 Admin St', '["visa-1234", "mastercard-5678"]', 'visa-1234', '555-0001');

INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, ADDRESS_INFO, DEFAULT_ADDRESS, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER) 
VALUES (gen_random_uuid(), 'user', '$2a$10$7QeW8y5Z6bX9EeuOa8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'user@example.com', 'USER', '["789 User Ln", "321 User Blvd"]', '789 User Ln', '["amex-9999"]', 'amex-9999', '555-0002');

INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, ADDRESS_INFO, DEFAULT_ADDRESS, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER) 
VALUES (gen_random_uuid(), 'guest', '$2a$10$UctvIHF0pireThgeV9rFOeNZY.3OnNM34ljQ.Y/U5fFOjiBcLQxO.', 'guest@example.com', 'GUEST', '["555 Guest Pl"]', '555 Guest Pl', NULL, NULL, '555-0003');