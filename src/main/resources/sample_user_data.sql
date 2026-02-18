-- Sample user data for testing purposes
-- Passwords are hashed using BCrypt
-- Note: Users are inserted first without address references, then updated after addresses are created

-- Admin user
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin', '$2a$10$Dow1jZp8K1JY5Z6bX9EeuOa8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'admin@example.com', 'ADMIN', '["visa-1234", "mastercard-5678"]', 'visa-1234', '555-0001');

-- Regular user
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER)
VALUES ('22222222-2222-2222-2222-222222222222', 'user', '$2a$10$7QeW8y5Z6bX9EeuOa8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'user@example.com', 'USER', '["amex-9999"]', 'amex-9999', '555-0002');

-- Guest user
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE, PAYMENT_INFO, DEFAULT_PAYMENT, PHONE_NUMBER)
VALUES ('33333333-3333-3333-3333-333333333333', 'guest', '$2a$10$GbRniN8p0v82nevIL4QYYOkvuRK2TNfDfNCajY3kLq8lJqb4MNmTS', 'guest@example.com', 'GUEST', NULL, NULL, '555-0003');