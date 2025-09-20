-- services/device-service/src/main/resources/db/migration/V1__Create_device_tables.sql

-- Main devices table
CREATE TABLE devices (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         device_id VARCHAR(20) NOT NULL UNIQUE,
                         company_id UUID NOT NULL,
                         traccar_id BIGINT UNIQUE,
                         device_name VARCHAR(50) NOT NULL,
                         device_type VARCHAR(30) NOT NULL,
                         device_brand VARCHAR(30),
                         device_model VARCHAR(50),
                         sim_card_number VARCHAR(20),
                         phone_number VARCHAR(20),
                         status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTIVATION',
                         vehicle_id UUID,
                         assigned_user_id UUID,
                         installation_date TIMESTAMP,
                         last_communication TIMESTAMP,
                         firmware_version VARCHAR(20),
                         configuration JSONB,
                         is_active BOOLEAN NOT NULL DEFAULT true,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_by UUID,
                         updated_by UUID
);

-- Device types table
CREATE TABLE device_types (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              type_name VARCHAR(30) NOT NULL UNIQUE,
                              description TEXT,
                              manufacturer VARCHAR(50),
                              default_config JSONB,
                              supported_features TEXT[],
                              is_active BOOLEAN NOT NULL DEFAULT true,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Device brands table
CREATE TABLE device_brands (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               brand_name VARCHAR(30) NOT NULL UNIQUE,
                               manufacturer VARCHAR(50),
                               website VARCHAR(100),
                               support_contact VARCHAR(100),
                               is_active BOOLEAN NOT NULL DEFAULT true,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for devices table
CREATE INDEX idx_devices_company_id ON devices(company_id);
CREATE INDEX idx_devices_device_id ON devices(device_id);
CREATE INDEX idx_devices_traccar_id ON devices(traccar_id);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_vehicle_id ON devices(vehicle_id);
CREATE INDEX idx_devices_assigned_user_id ON devices(assigned_user_id);
CREATE INDEX idx_devices_created_at ON devices(created_at);
CREATE INDEX idx_devices_last_communication ON devices(last_communication);

-- Comments
COMMENT ON TABLE devices IS 'Main table for all GPS tracking devices';
COMMENT ON COLUMN devices.device_id IS 'IMEI or unique device identifier';
COMMENT ON COLUMN devices.traccar_id IS 'Reference to Traccar device ID';
COMMENT ON COLUMN devices.configuration IS 'JSON configuration for device-specific settings';
