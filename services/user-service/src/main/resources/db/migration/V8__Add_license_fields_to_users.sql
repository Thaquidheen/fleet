-- V8__Add_license_fields_to_users.sql
-- Add license-related fields to users table


ALTER TABLE users
ADD COLUMN license_number VARCHAR(50),
ADD COLUMN license_expiry TIMESTAMP;