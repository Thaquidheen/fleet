-- V8__Complete_enum_updates.sql
-- Ensure all enum values are present

-- Add any missing values to company_status enum (should be complete now)
ALTER TYPE company_status ADD VALUE IF NOT EXISTS 'CANCELLED';
ALTER TYPE company_status ADD VALUE IF NOT EXISTS 'EXPIRED';

-- Verify final enum values
-- company_status should have: TRIAL, ACTIVE, SUSPENDED, CANCELLED, EXPIRED
-- subscription_plan should have: BASIC, PREMIUM, ENTERPRISE (matches Java enum)