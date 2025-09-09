-- V2__Add_missing_user_service_components.sql
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
CREATE TABLE IF NOT EXISTS user_sessions (
                                             session_id VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_info VARCHAR(500),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Indexes
    INDEX idx_user_sessions_user_id (user_id),
    INDEX idx_user_sessions_active (user_id, is_active),
    INDEX idx_user_sessions_expires (expires_at)
    );

-- Create user audit log table for tracking important events
CREATE TABLE IF NOT EXISTS user_audit_log (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    performed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Indexes
    INDEX idx_user_audit_log_user_id (user_id),
    INDEX idx_user_audit_log_action (action),
    INDEX idx_user_audit_log_created_at (created_at),
    INDEX idx_user_audit_log_performed_by (performed_by)
    );

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
  AND u.account_locked = FALSE
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

-- Create trigger to update user statistics on assignment
CREATE OR REPLACE FUNCTION trigger_update_user_assignment()
RETURNS TRIGGER AS $$
BEGIN
    -- This would be called from vehicle service when assignments are created
    -- For now, it's a placeholder for future integration
RETURN NEW;
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

-- Create view for driver statistics
CREATE OR REPLACE VIEW driver_statistics AS
SELECT
    u.company_id,
    COUNT(*) as total_drivers,
    COUNT(CASE WHEN u.status = 'ACTIVE' THEN 1 END) as active_drivers,
    COUNT(CASE WHEN u.status = 'INACTIVE' THEN 1 END) as inactive_drivers,
    COUNT(CASE WHEN u.is_email_verified = TRUE THEN 1 END) as verified_drivers,
    COUNT(CASE WHEN u.is_email_verified = FALSE THEN 1 END) as unverified_drivers,
    AVG(u.total_assignments) as avg_assignments_per_driver,
    MAX(u.last_assignment_date) as latest_assignment_date
FROM users u
WHERE u.role = 'DRIVER'
GROUP BY u.company_id;

COMMENT ON VIEW driver_statistics IS 'Aggregated statistics for drivers by company';

-- Insert sample data for testing (remove in production)
-- This is commented out - uncomment only for development/testing

/*
-- Sample email verification token (expired for testing)
INSERT INTO email_verification_tokens (
    user_id,
    token,
    token_type,
    expiry_date,
    is_used,
    created_at
) VALUES (
    (SELECT id FROM users WHERE username = 'test_driver' LIMIT 1),
    'sample_verification_token_12345',
    'EMAIL_VERIFICATION',
    NOW() - INTERVAL '1 hour', -- Expired token for testing
    FALSE,
    NOW() - INTERVAL '2 hours'
) ON CONFLICT DO NOTHING;
*/

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_users_company_email_verified ON users(company_id, is_email_verified);
CREATE INDEX IF NOT EXISTS idx_users_role_status_verified ON users(role, status, is_email_verified);
CREATE INDEX IF NOT EXISTS idx_email_tokens_user_type_active ON email_verification_tokens(user_id, token_type) WHERE is_used = FALSE;

-- Function to get user verification statistics for a company
CREATE OR REPLACE FUNCTION get_verification_statistics(company_uuid UUID)
RETURNS TABLE (
    total_users BIGINT,
    verified_users BIGINT,
    pending_verifications BIGINT,
    verification_rate DECIMAL
) AS $
BEGIN
RETURN QUERY
SELECT
    COUNT(*) as total_users,
    COUNT(CASE WHEN u.is_email_verified = TRUE THEN 1 END) as verified_users,
    COUNT(CASE WHEN u.is_email_verified = FALSE THEN 1 END) as pending_verifications,
    ROUND(
            (COUNT(CASE WHEN u.is_email_verified = TRUE THEN 1 END)::DECIMAL / COUNT(*)::DECIMAL) * 100,
            2
    ) as verification_rate
FROM users u
WHERE u.company_id = company_uuid;
END;
$ LANGUAGE plpgsql;

-- Function to cleanup old audit logs (for maintenance)
CREATE OR REPLACE FUNCTION cleanup_old_audit_logs(days_to_keep INTEGER DEFAULT 90)
RETURNS INTEGER AS $
DECLARE
deleted_count INTEGER;
BEGIN
DELETE FROM user_audit_log
WHERE created_at < NOW() - (days_to_keep || ' days')::INTERVAL;

GET DIAGNOSTICS deleted_count = ROW_COUNT;

RETURN deleted_count;
END;
$ LANGUAGE plpgsql;

-- Function to get driver availability summary
CREATE OR REPLACE FUNCTION get_driver_availability_summary(company_uuid UUID)
RETURNS TABLE (
    total_drivers BIGINT,
    available_drivers BIGINT,
    assigned_drivers BIGINT,
    unavailable_drivers BIGINT
) AS $
BEGIN
RETURN QUERY
SELECT
    COUNT(*) as total_drivers,
    COUNT(CASE
              WHEN u.status = 'ACTIVE'
                  AND u.is_email_verified = TRUE
                  AND u.account_locked = FALSE
                  THEN 1
        END) as available_drivers,
    -- This would need integration with vehicle service to get actual assignments
    0::BIGINT as assigned_drivers,
    COUNT(CASE
              WHEN u.status != 'ACTIVE'
              OR u.is_email_verified = FALSE
              OR u.account_locked = TRUE
          THEN 1
          END) as unavailable_drivers
FROM users u
WHERE u.company_id = company_uuid
  AND u.role = 'DRIVER';
END;
$ LANGUAGE plpgsql;

-- Add triggers for audit logging
CREATE OR REPLACE FUNCTION audit_user_changes()
RETURNS TRIGGER AS $
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_audit_log (user_id, action, details)
        VALUES (NEW.id, 'USER_CREATED', row_to_json(NEW));
RETURN NEW;
ELSIF TG_OP = 'UPDATE' THEN
        -- Log significant changes
        IF OLD.status != NEW.status THEN
            INSERT INTO user_audit_log (user_id, action, details)
            VALUES (NEW.id, 'STATUS_CHANGED', json_build_object(
                'old_status', OLD.status,
                'new_status', NEW.status
            ));
END IF;

        IF OLD.role != NEW.role THEN
            INSERT INTO user_audit_log (user_id, action, details)
            VALUES (NEW.id, 'ROLE_CHANGED', json_build_object(
                'old_role', OLD.role,
                'new_role', NEW.role
            ));
END IF;

        IF OLD.is_email_verified != NEW.is_email_verified AND NEW.is_email_verified = TRUE THEN
            INSERT INTO user_audit_log (user_id, action, details)
            VALUES (NEW.id, 'EMAIL_VERIFIED', json_build_object(
                'verified_at', NEW.email_verified_at
            ));
END IF;

RETURN NEW;
ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO user_audit_log (user_id, action, details)
        VALUES (OLD.id, 'USER_DELETED', row_to_json(OLD));
RETURN OLD;
END IF;

RETURN NULL;
END;
$ LANGUAGE plpgsql;

-- Create the audit trigger
DROP TRIGGER IF EXISTS trigger_audit_user_changes ON users;
CREATE TRIGGER trigger_audit_user_changes
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_user_changes();

-- Create trigger for email verification token audit
CREATE OR REPLACE FUNCTION audit_verification_token_changes()
RETURNS TRIGGER AS $
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_audit_log (user_id, action, details)
        VALUES (NEW.user_id, 'VERIFICATION_TOKEN_CREATED', json_build_object(
            'token_type', NEW.token_type,
            'expiry_date', NEW.expiry_date
        ));
RETURN NEW;
ELSIF TG_OP = 'UPDATE' AND OLD.is_used = FALSE AND NEW.is_used = TRUE THEN
        INSERT INTO user_audit_log (user_id, action, details)
        VALUES (NEW.user_id, 'VERIFICATION_TOKEN_USED', json_build_object(
            'token_type', NEW.token_type,
            'used_at', NEW.used_at
        ));
RETURN NEW;
END IF;

RETURN NULL;
END;
$ LANGUAGE plpgsql;

-- Create the verification token audit trigger
DROP TRIGGER IF EXISTS trigger_audit_verification_token_changes ON email_verification_tokens;
CREATE TRIGGER trigger_audit_verification_token_changes
    AFTER INSERT OR UPDATE ON email_verification_tokens
                        FOR EACH ROW EXECUTE FUNCTION audit_verification_token_changes();