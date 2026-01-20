-- Sample user data for testing purposes
-- Passwords are hashed using BCrypt
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE) VALUES (gen_random_uuid(), 'admin', '$2a$10$Dow1jZp8K1JY5Z6bX9EeuOa8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'admin@example.com', 'ADMIN');
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE) VALUES (gen_random_uuid(), 'user', '$2a$10$7QeW8y5Z6bX9EeuOa  8hFG1QeW8y5Z6bX9EeuOa8hFG1QeW8y', 'user@example.com', 'USER');
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, EMAIL, ACCOUNT_TYPE) VALUES (gen_random_uuid(), 'guest', '$2a$10$UctvIHF0pireThgeV9rFOeNZY.3OnNM34ljQ.Y/U5fFOjiBcLQxO.', 'guest@example.com', 'GUEST');