-- V3__Add_company_user_management.sql
-- Migration to add user management capabilities to Company Service

-- Add missing user tracking fields to companies table
ALTER TABLE companies ADD COLUMN IF NOT EXISTS current_user_count INTEGER DEFAULT 0;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS current_driver_count INTEGER DEFAULT 0;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS last_user_sync_at TIMESTAMP;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS max_users_override INTEGER; -- Override subscription limits if needed

-- Create company settings table for flexible configuration
CREATE TABLE IF NOT EXISTS company_settings (
                                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(20) DEFAULT 'STRING', -- STRING, INTEGER, BOOLEAN, JSON
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT uk_company_settings_key UNIQUE (company_id, setting_key),
    CONSTRAINT chk_setting_type CHECK (setting_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'ENCRYPTED'))
    );

-- Create company audit log table
CREATE TABLE IF NOT EXISTS company_audit_log (
                                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL, -- COMPANY, USER, SUBSCRIPTION, SETTINGS
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[], -- Array of field names that changed
    ip_address INET,
    user_agent TEXT,
    performed_by UUID, -- User who performed the action
    performed_by_role VARCHAR(50),
    reason TEXT, -- Reason for the change
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_company_audit_company_id (company_id),
    INDEX idx_company_audit_action (action),
    INDEX idx_company_audit_entity_type (entity_type),
    INDEX idx_company_audit_created_at (created_at),
    INDEX idx_company_audit_performed_by (performed_by)
    );

-- Create bulk operations tracking table
CREATE TABLE IF NOT EXISTS bulk_operations (
                                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    operation_type VARCHAR(50) NOT NULL, -- CREATE_USERS, UPDATE_USERS, DELETE_USERS
    operation_id VARCHAR(100) UNIQUE NOT NULL, -- External tracking ID
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    total_items INTEGER NOT NULL,
    processed_items INTEGER DEFAULT 0,
    successful_items INTEGER DEFAULT 0,
    failed_items INTEGER DEFAULT 0,
    error_details JSONB,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_completion_at TIMESTAMP,
    performed_by UUID,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_bulk_operations_company_id (company_id),
    INDEX idx_bulk_operations_status (status),
    INDEX idx_bulk_operations_operation_id (operation_id),
    INDEX idx_bulk_operations_created_at (created_at),
    INDEX idx_bulk_operations_performed_by (performed_by),

    CONSTRAINT chk_bulk_operation_type CHECK (operation_type IN ('CREATE_USERS', 'UPDATE_USERS', 'DELETE_USERS', 'ACTIVATE_USERS', 'DEACTIVATE_USERS')),
    CONSTRAINT chk_bulk_operation_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_progress_percentage CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
    );

-- Create company user count history table for tracking changes over time
CREATE TABLE IF NOT EXISTS company_user_count_history (
                                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_count INTEGER NOT NULL,
    driver_count INTEGER NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- INCREMENT, DECREMENT, SYNC, MANUAL
    change_amount INTEGER DEFAULT 0,
    previous_count INTEGER,
    reason TEXT,
    source VARCHAR(50) DEFAULT 'USER_SERVICE', -- USER_SERVICE, MANUAL, BULK_OPERATION, SYNC
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    INDEX idx_company_user_count_history_company_id (company_id),
    INDEX idx_company_user_count_history_created_at (created_at),
    INDEX idx_company_user_count_history_change_type (change_type),

    CONSTRAINT chk_change_type CHECK (change_type IN ('INCREMENT', 'DECREMENT', 'SYNC', 'MANUAL', 'CORRECTION'))
    );

-- Add indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_companies_current_user_count ON companies(current_user_count);
CREATE INDEX IF NOT EXISTS idx_companies_subscription_plan ON companies(subscription_plan);
CREATE INDEX IF NOT EXISTS idx_companies_last_user_sync ON companies(last_user_sync_at);

CREATE INDEX IF NOT EXISTS idx_company_settings_company_id ON company_settings(company_id);
CREATE INDEX IF NOT EXISTS idx_company_settings_key ON company_settings(setting_key);
CREATE INDEX IF NOT EXISTS idx_company_settings_type ON company_settings(setting_type);

-- Create triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_company_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to companies table if not exists
DROP TRIGGER IF EXISTS trigger_companies_updated_at ON companies;
CREATE TRIGGER trigger_companies_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW EXECUTE FUNCTION update_company_updated_at();

-- Apply trigger to company_settings table
DROP TRIGGER IF EXISTS trigger_company_settings_updated_at ON company_settings;
CREATE TRIGGER trigger_company_settings_updated_at
    BEFORE UPDATE ON company_settings
    FOR EACH ROW EXECUTE FUNCTION update_company_updated_at();

-- Apply trigger to bulk_operations table
DROP TRIGGER IF EXISTS trigger_bulk_operations_updated_at ON bulk_operations;
CREATE TRIGGER trigger_bulk_operations_updated_at
    BEFORE UPDATE ON bulk_operations
    FOR EACH ROW EXECUTE FUNCTION update_company_updated_at();

-- Function to get user count statistics for a company
CREATE OR REPLACE FUNCTION get_company_user_statistics(company_uuid UUID)
RETURNS TABLE (
    current_users INTEGER,
    current_drivers INTEGER,
    max_allowed_users INTEGER,
    subscription_plan TEXT,
    last_sync_at TIMESTAMP,
    can_add_users BOOLEAN,
    available_slots INTEGER,
    utilization_percentage DECIMAL
) AS $$
DECLARE
company_record RECORD;
    max_users INTEGER;
BEGIN
    -- Get company information
SELECT c.current_user_count, c.current_driver_count, c.subscription_plan,
       c.last_user_sync_at, c.max_users_override
INTO company_record
FROM companies c
WHERE c.id = company_uuid;

IF NOT FOUND THEN
        RAISE EXCEPTION 'Company not found: %', company_uuid;
END IF;

    -- Determine max users based on subscription plan
    max_users := CASE
        WHEN company_record.max_users_override IS NOT NULL THEN company_record.max_users_override
        WHEN company_record.subscription_plan = 'BASIC' THEN 5
        WHEN company_record.subscription_plan = 'PREMIUM' THEN 50
        WHEN company_record.subscription_plan = 'ENTERPRISE' THEN 1000
        WHEN company_record.subscription_plan = 'OWNER' THEN -1 -- Unlimited
        ELSE 5 -- Default to basic
END;

RETURN QUERY
SELECT
    company_record.current_user_count,
    company_record.current_driver_count,
    max_users,
    company_record.subscription_plan::TEXT,
    company_record.last_user_sync_at,
    (max_users = -1 OR company_record.current_user_count < max_users) as can_add_users,
    CASE
        WHEN max_users = -1 THEN 999999 -- Unlimited
        ELSE GREATEST(0, max_users - company_record.current_user_count)
        END as available_slots,
    CASE
        WHEN max_users = -1 THEN 0.0 -- Unlimited plans show 0% utilization
        WHEN max_users = 0 THEN 0.0
        ELSE ROUND((company_record.current_user_count::DECIMAL / max_users::DECIMAL) * 100, 2)
        END as utilization_percentage;
END;
$ LANGUAGE plpgsql;

-- Function to update user counts with history tracking
CREATE OR REPLACE FUNCTION update_company_user_count(
    company_uuid UUID,
    new_user_count INTEGER,
    new_driver_count INTEGER,
    change_type_param VARCHAR(20) DEFAULT 'MANUAL',
    reason_param TEXT DEFAULT NULL,
    created_by_param UUID DEFAULT NULL
)
RETURNS VOID AS $
DECLARE
current_record RECORD;
    user_change INTEGER;
    driver_change INTEGER;
BEGIN
    -- Get current counts
SELECT current_user_count, current_driver_count
INTO current_record
FROM companies
WHERE id = company_uuid;

IF NOT FOUND THEN
        RAISE EXCEPTION 'Company not found: %', company_uuid;
END IF;

    -- Calculate changes
    user_change := new_user_count - current_record.current_user_count;
    driver_change := new_driver_count - current_record.current_driver_count;

    -- Update company counts
UPDATE companies
SET
    current_user_count = new_user_count,
    current_driver_count = new_driver_count,
    last_user_sync_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = company_uuid;

-- Record history if there was a change
IF user_change != 0 OR driver_change != 0 THEN
        INSERT INTO company_user_count_history (
            company_id,
            user_count,
            driver_count,
            change_type,
            change_amount,
            previous_count,
            reason,
            created_by
        ) VALUES (
            company_uuid,
            new_user_count,
            new_driver_count,
            change_type_param,
            user_change,
            current_record.current_user_count,
            reason_param,
            created_by_param
        );
END IF;
END;
$ LANGUAGE plpgsql;

-- Function to increment user count safely
CREATE OR REPLACE FUNCTION increment_company_user_count(
    company_uuid UUID,
    increment_by INTEGER DEFAULT 1,
    reason_param TEXT DEFAULT 'User created'
)
RETURNS INTEGER AS $
DECLARE
new_count INTEGER;
BEGIN
UPDATE companies
SET
    current_user_count = current_user_count + increment_by,
    last_user_sync_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = company_uuid
    RETURNING current_user_count INTO new_count;

IF NOT FOUND THEN
        RAISE EXCEPTION 'Company not found: %', company_uuid;
END IF;

    -- Record history
INSERT INTO company_user_count_history (
    company_id,
    user_count,
    driver_count,
    change_type,
    change_amount,
    reason
)
SELECT
    company_uuid,
    new_count,
    current_driver_count,
    'INCREMENT',
    increment_by,
    reason_param
FROM companies
WHERE id = company_uuid;

RETURN new_count;
END;
$ LANGUAGE plpgsql;

-- Function to decrement user count safely
CREATE OR REPLACE FUNCTION decrement_company_user_count(
    company_uuid UUID,
    decrement_by INTEGER DEFAULT 1,
    reason_param TEXT DEFAULT 'User deleted'
)
RETURNS INTEGER AS $
DECLARE
new_count INTEGER;
BEGIN
UPDATE companies
SET
    current_user_count = GREATEST(0, current_user_count - decrement_by),
    last_user_sync_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = company_uuid
    RETURNING current_user_count INTO new_count;

IF NOT FOUND THEN
        RAISE EXCEPTION 'Company not found: %', company_uuid;
END IF;

    -- Record history
INSERT INTO company_user_count_history (
    company_id,
    user_count,
    driver_count,
    change_type,
    change_amount,
    reason
)
SELECT
    company_uuid,
    new_count,
    current_driver_count,
    'DECREMENT',
    -decrement_by,
    reason_param
FROM companies
WHERE id = company_uuid;

RETURN new_count;
END;
$ LANGUAGE plpgsql;

-- Function to get company settings
CREATE OR REPLACE FUNCTION get_company_setting(
    company_uuid UUID,
    setting_key_param VARCHAR(100)
)
RETURNS TEXT AS $
DECLARE
setting_value TEXT;
BEGIN
SELECT cs.setting_value
INTO setting_value
FROM company_settings cs
WHERE cs.company_id = company_uuid
  AND cs.setting_key = setting_key_param;

RETURN setting_value;
END;
$ LANGUAGE plpgsql;

-- Function to set company settings
CREATE OR REPLACE FUNCTION set_company_setting(
    company_uuid UUID,
    setting_key_param VARCHAR(100),
    setting_value_param TEXT,
    setting_type_param VARCHAR(20) DEFAULT 'STRING',
    description_param TEXT DEFAULT NULL,
    updated_by_param UUID DEFAULT NULL
)
RETURNS VOID AS $
BEGIN
INSERT INTO company_settings (
    company_id,
    setting_key,
    setting_value,
    setting_type,
    description,
    updated_by
) VALUES (
             company_uuid,
             setting_key_param,
             setting_value_param,
             setting_type_param,
             description_param,
             updated_by_param
         )
    ON CONFLICT (company_id, setting_key)
    DO UPDATE SET
    setting_value = EXCLUDED.setting_value,
               setting_type = EXCLUDED.setting_type,
               description = EXCLUDED.description,
               updated_by = EXCLUDED.updated_by,
               updated_at = CURRENT_TIMESTAMP;
END;
$ LANGUAGE plpgsql;

-- Function to audit company changes
CREATE OR REPLACE FUNCTION audit_company_change(
    company_uuid UUID,
    action_param VARCHAR(100),
    entity_type_param VARCHAR(50),
    entity_id_param UUID DEFAULT NULL,
    old_values_param JSONB DEFAULT NULL,
    new_values_param JSONB DEFAULT NULL,
    performed_by_param UUID DEFAULT NULL,
    reason_param TEXT DEFAULT NULL
)
RETURNS VOID AS $
BEGIN
INSERT INTO company_audit_log (
    company_id,
    action,
    entity_type,
    entity_id,
    old_values,
    new_values,
    performed_by,
    reason
) VALUES (
             company_uuid,
             action_param,
             entity_type_param,
             entity_id_param,
             old_values_param,
             new_values_param,
             performed_by_param,
             reason_param
         );
END;
$ LANGUAGE plpgsql;

-- Function to cleanup old audit logs
CREATE OR REPLACE FUNCTION cleanup_old_company_audit_logs(days_to_keep INTEGER DEFAULT 90)
RETURNS INTEGER AS $
DECLARE
deleted_count INTEGER;
BEGIN
DELETE FROM company_audit_log
WHERE created_at < NOW() - (days_to_keep || ' days')::INTERVAL;

GET DIAGNOSTICS deleted_count = ROW_COUNT;

RETURN deleted_count;
END;
$ LANGUAGE plpgsql;

-- Create view for company dashboard statistics
CREATE OR REPLACE VIEW company_dashboard_stats AS
SELECT
    c.id as company_id,
    c.name as company_name,
    c.subscription_plan,
    c.current_user_count,
    c.current_driver_count,
    c.last_user_sync_at,
    CASE
        WHEN c.subscription_plan = 'BASIC' THEN 5
        WHEN c.subscription_plan = 'PREMIUM' THEN 50
        WHEN c.subscription_plan = 'ENTERPRISE' THEN 1000
        WHEN c.subscription_plan = 'OWNER' THEN -1
        ELSE 5
        END as max_users,
    CASE
        WHEN c.subscription_plan = 'OWNER' THEN 'Unlimited'
        ELSE (CASE
                  WHEN c.subscription_plan = 'BASIC' THEN 5
                  WHEN c.subscription_plan = 'PREMIUM' THEN 50
                  WHEN c.subscription_plan = 'ENTERPRISE' THEN 1000
                  ELSE 5
                  END - c.current_user_count)::TEXT
        END as available_slots,
    CASE
        WHEN c.subscription_plan = 'OWNER' THEN 0.0
        ELSE ROUND((c.current_user_count::DECIMAL / (CASE
            WHEN c.subscription_plan = 'BASIC' THEN 5
            WHEN c.subscription_plan = 'PREMIUM' THEN 50
            WHEN c.subscription_plan = 'ENTERPRISE' THEN 1000
            ELSE 5
        END)::DECIMAL) * 100, 2)
        END as utilization_percentage,
    c.status as company_status,
    c.created_at,
    c.updated_at
FROM companies c;

-- Add comments for documentation
COMMENT ON TABLE company_settings IS 'Flexible key-value settings for companies';
COMMENT ON TABLE company_audit_log IS 'Audit trail for all company-related changes';
COMMENT ON TABLE bulk_operations IS 'Tracking table for bulk user operations';
COMMENT ON TABLE company_user_count_history IS 'Historical tracking of user count changes';

COMMENT ON COLUMN companies.current_user_count IS 'Current number of users in the company';
COMMENT ON COLUMN companies.current_driver_count IS 'Current number of drivers in the company';
COMMENT ON COLUMN companies.last_user_sync_at IS 'Last time user counts were synchronized with User Service';
COMMENT ON COLUMN companies.max_users_override IS 'Override subscription limits if needed for custom agreements';

-- Insert default settings for existing companies
INSERT INTO company_settings (company_id, setting_key, setting_value, setting_type, description)
SELECT
    id,
    'email_notifications_enabled',
    'true',
    'BOOLEAN',
    'Enable email notifications for company events'
FROM companies
WHERE NOT EXISTS (
    SELECT 1 FROM company_settings cs
    WHERE cs.company_id = companies.id
      AND cs.setting_key = 'email_notifications_enabled'
);

INSERT INTO company_settings (company_id, setting_key, setting_value, setting_type, description)
SELECT
    id,
    'auto_user_sync_enabled',
    'true',
    'BOOLEAN',
    'Enable automatic user count synchronization'
FROM companies
WHERE NOT EXISTS (
    SELECT 1 FROM company_settings cs
    WHERE cs.company_id = companies.id
      AND cs.setting_key = 'auto_user_sync_enabled'
);

-- Initialize user counts for existing companies (set to 0 as starting point)
UPDATE companies
SET
    current_user_count = 0,
    current_driver_count = 0,
    last_user_sync_at = CURRENT_TIMESTAMP
WHERE current_user_count IS NULL;