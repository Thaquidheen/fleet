-- V6__Create_indexes.sql
-- Create indexes for performance optimization

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);

-- User sessions table indexes
CREATE INDEX IF NOT EXISTS idx_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_session_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_status ON user_sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON user_sessions(expires_at);

-- User permissions table indexes
CREATE INDEX IF NOT EXISTS idx_permission_user_id ON user_permissions(user_id);
CREATE INDEX IF NOT EXISTS idx_permission_resource ON user_permissions(resource);
CREATE INDEX IF NOT EXISTS idx_permission_type ON user_permissions(permission_type);