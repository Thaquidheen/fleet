-- User Service Database Initialization - COMPLETE VERSION
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create user roles enum
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'COMPANY_ADMIN', 'FLEET_MANAGER', 'DRIVER', 'VIEWER');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'EXPIRED', 'REVOKED');
CREATE TYPE permission_type AS ENUM ('READ', 'WRITE', 'DELETE', 'ADMIN');

-- Users table with ALL fields from JPA Entity
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    role user_role NOT NULL DEFAULT 'VIEWER',
    status user_status NOT NULL DEFAULT 'ACTIVE',
    company_id UUID,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    employee_id VARCHAR(50),
    department VARCHAR(100),
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login_at TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_password_change TIMESTAMP,
    force_password_change BOOLEAN DEFAULT FALSE,
    account_locked_until TIMESTAMP,
    timezone VARCHAR(10) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    profile_picture_url VARCHAR(255),
    profile_image_url VARCHAR(255),
    notes TEXT,
    email_verification_token VARCHAR(255),
    email_verification_expiry TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_expiry TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
    );

-- User Sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_token VARCHAR(2000) UNIQUE NOT NULL,
    refresh_token VARCHAR(2000) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status session_status NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    refresh_expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- User Permissions table
CREATE TABLE IF NOT EXISTS user_permissions (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resource VARCHAR(100) NOT NULL,
    permission_type permission_type NOT NULL,
    granted BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID,
    UNIQUE(user_id, resource, permission_type)
    );

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);

CREATE INDEX IF NOT EXISTS idx_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_session_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_status ON user_sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON user_sessions(expires_at);

CREATE INDEX IF NOT EXISTS idx_permission_user_id ON user_permissions(user_id);
CREATE INDEX IF NOT EXISTS idx_permission_resource ON user_permissions(resource);
CREATE INDEX IF NOT EXISTS idx_permission_type ON user_permissions(permission_type);

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('admin', 'admin@fleetmanagement.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeJ0VGa5YcwV7VEUOIgOKNIQ5vKXUu9zu', 'Admin', 'User', 'SUPER_ADMIN', 'ACTIVE', true)
    ON CONFLICT (username) DO NOTHING;

-- Insert test user (password: password123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('testuser349', 'testuser349@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeJ0VGa5YcwV7VEUOIgOKNIQ5vKXUu9zu', 'Test', 'User', 'DRIVER', 'ACTIVE', true)
    ON CONFLICT (username) DO NOTHING;