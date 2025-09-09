-- V9__Add_missing_user_service_components.sql
-- Migration to add missing components for User Service integration

-- Add missing fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_assignments INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_assignment_date TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP;

-- Create email verification tokens table
CREATE TABLE IF NOT EXISTS email_verification_tokens (
                                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_token_type CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'TWO_FACTOR_AUTH'))
    );

-- Create indexes for email verification tokens
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_token ON email_verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_type ON email_verification_tokens(token_type);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_expiry ON email_verification_tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_active ON email_verification_tokens(user_id, token_type, is_used, expiry_date);

-- Add driver-specific indexes to users table
CREATE INDEX IF NOT EXISTS idx_users_company_role ON users(company_id, role) WHERE role = 'DRIVER';
CREATE INDEX IF NOT EXISTS idx_users_drivers_active ON users(company_id, role, status) WHERE role = 'DRIVER' AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(is_email_verified);

-- Create user sessions tracking table (optional - Redis is primary storage)
-- FIXED: Removed inline INDEX declarations and create them separately
CREATE TABLE IF NOT EXISTS user_sessions (
                                             session_id VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_info VARCHAR(500),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Create indexes for user_sessions table separately
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_active ON user_sessions(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires ON user_sessions(expires_at);

-- Create user audit log table for tracking important events
-- FIXED: Removed inline INDEX declarations and create them separately
CREATE TABLE IF NOT EXISTS user_audit_log (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    performed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Create indexes for user_audit_log table separately
CREATE INDEX IF NOT EXISTS idx_user_audit_log_user_id ON user_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_user_audit_log_action ON user_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_user_audit_log_created_at ON user_audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_user_audit_log_performed_by ON user_audit_log(performed_by);

-- Update existing users to have email_verified = true for existing active users (migration)
UPDATE users
SET is_email_verified = TRUE, email_verified_at = created_at
WHERE status = 'ACTIVE' AND is_email_verified = FALSE;

-- Create function to clean up expired verification tokens
CREATE OR REPLACE FUNCTION cleanup_expired_verification_tokens()
RETURNS INTEGER AS $$
DECLARE
deleted_count INTEGER;
BEGIN
DELETE FROM email_verification_tokens
WHERE expiry_date < NOW() - INTERVAL '24 hours';

GET DIAGNOSTICS deleted_count = ROW_COUNT;

RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create function to get available drivers for a company
CREATE OR REPLACE FUNCTION get_available_drivers(company_uuid UUID)
RETURNS TABLE (
    id UUID,
    username VARCHAR,
    first_name VARCHAR,
    last_name VARCHAR,
    email VARCHAR,
    phone_number VARCHAR,
    total_assignments INTEGER,
    last_assignment_date TIMESTAMP
) AS $$
BEGIN
RETURN QUERY
SELECT
    u.id,
    u.username,
    u.first_name,
    u.last_name,
    u.email,
    u.phone_number,
    u.total_assignments,
    u.last_assignment_date
FROM users u
WHERE u.company_id = company_uuid
  AND u.role = 'DRIVER'
  AND u.status = 'ACTIVE'
  AND u.is_email_verified = TRUE
  AND u.account_locked_until IS NULL
ORDER BY u.last_assignment_date ASC NULLS FIRST;
END;
$$ LANGUAGE plpgsql;

-- Create function to update user assignment statistics
CREATE OR REPLACE FUNCTION update_user_assignment_stats(user_uuid UUID)
RETURNS VOID AS $$
BEGIN
UPDATE users
SET
    total_assignments = COALESCE(total_assignments, 0) + 1,
    last_assignment_date = NOW()
WHERE id = user_uuid;
END;
$$ LANGUAGE plpgsql;

-- Add constraints for data integrity
ALTER TABLE email_verification_tokens
    ADD CONSTRAINT chk_expiry_date_future
        CHECK (expiry_date > created_at);

ALTER TABLE email_verification_tokens
    ADD CONSTRAINT chk_used_at_logic
        CHECK (
            (is_used = FALSE AND used_at IS NULL) OR
            (is_used = TRUE AND used_at IS NOT NULL)
            );

-- Add comments for documentation
COMMENT ON TABLE email_verification_tokens IS 'Stores email verification tokens for user email confirmation';
COMMENT ON TABLE user_sessions IS 'Optional backup storage for user sessions (primary storage is Redis)';
COMMENT ON TABLE user_audit_log IS 'Audit trail for important user actions and changes';

COMMENT ON COLUMN users.total_assignments IS 'Total number of vehicle assignments for drivers';
COMMENT ON COLUMN users.last_assignment_date IS 'Date of last vehicle assignment for drivers';
COMMENT ON COLUMN users.is_email_verified IS 'Whether user email has been verified';
COMMENT ON COLUMN users.email_verified_at IS 'Timestamp when email was verified';