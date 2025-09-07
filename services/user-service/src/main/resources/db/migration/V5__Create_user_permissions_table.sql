-- V5__Create_user_permissions_table.sql
-- Create user permissions table

CREATE TABLE user_permissions (
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