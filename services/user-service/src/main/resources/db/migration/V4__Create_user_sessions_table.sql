-- V4__Create_user_sessions_table.sql
-- Create user sessions table

CREATE TABLE user_sessions (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               session_token VARCHAR(2000) UNIQUE NOT NULL,
                               refresh_token VARCHAR(2000) UNIQUE NOT NULL,
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               status session_status NOT NULL DEFAULT 'ACTIVE',
                               expires_at TIMESTAMP NOT NULL,
                               refresh_expires_at TIMESTAMP NOT NULL,
                               ip_address VARCHAR(45),
                               user_agent TEXT,
                               device_info VARCHAR(255),
                               last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);