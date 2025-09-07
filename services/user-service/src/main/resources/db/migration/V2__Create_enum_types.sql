-- V2__Create_enum_types.sql
-- Create enum types for user service

CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'COMPANY_ADMIN', 'FLEET_MANAGER', 'DRIVER', 'VIEWER');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'EXPIRED', 'REVOKED');
CREATE TYPE permission_type AS ENUM ('READ', 'WRITE', 'DELETE', 'ADMIN');