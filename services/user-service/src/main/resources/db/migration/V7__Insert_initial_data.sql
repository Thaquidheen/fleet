-- V7__Insert_initial_data.sql
-- Insert initial users and data

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('admin', 'admin@fleetmanagement.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeJ0VGa5YcwV7VEUOIgOKNIQ5vKXUu9zu', 'Admin', 'User', 'SUPER_ADMIN', 'ACTIVE', true)
    ON CONFLICT (username) DO NOTHING;

-- Insert test user (password: password123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('testuser349', 'testuser349@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeJ0VGa5YcwV7VEUOIgOKNIQ5vKXUu9zu', 'Test', 'User', 'DRIVER', 'ACTIVE', true)
    ON CONFLICT (username) DO NOTHING;