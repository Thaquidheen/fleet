-- services/device-service/src/main/resources/db/migration/V2__Create_mobile_device_tables.sql

-- Mobile devices table
CREATE TABLE mobile_devices (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                user_id UUID NOT NULL,
                                device_model VARCHAR(50),
                                operating_system VARCHAR(20),
                                os_version VARCHAR(20),
                                app_version VARCHAR(20),
                                push_token VARCHAR(255),
                                tracking_enabled BOOLEAN NOT NULL DEFAULT false,
                                work_hours_only BOOLEAN NOT NULL DEFAULT true,
                                privacy_mode_enabled BOOLEAN NOT NULL DEFAULT true,
                                battery_optimization BOOLEAN NOT NULL DEFAULT true,
                                location_accuracy VARCHAR(10) DEFAULT 'HIGH',
                                update_interval_seconds INTEGER DEFAULT 30,
                                last_tracking_enabled_at TIMESTAMP,
                                last_tracking_disabled_at TIMESTAMP,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Device commands table
CREATE TABLE device_commands (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                 traccar_device_id BIGINT NOT NULL,
                                 command_type VARCHAR(50) NOT NULL,
                                 parameters JSONB,
                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                 result TEXT,
                                 error_message TEXT,
                                 description TEXT,
                                 requested_by UUID NOT NULL,
                                 sent_at TIMESTAMP,
                                 completed_at TIMESTAMP,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Device health records
CREATE TABLE device_health_records (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                       check_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       is_online BOOLEAN NOT NULL,
                                       signal_strength INTEGER,
                                       battery_level DECIMAL(5,2),
                                       last_position_time TIMESTAMP,
                                       gps_accuracy DECIMAL(8,2),
                                       connection_status VARCHAR(20),
                                       error_count INTEGER DEFAULT 0,
                                       warning_count INTEGER DEFAULT 0,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_mobile_devices_device_id ON mobile_devices(device_id);
CREATE INDEX idx_mobile_devices_user_id ON mobile_devices(user_id);
CREATE INDEX idx_device_commands_device_id ON device_commands(device_id);
CREATE INDEX idx_device_commands_status ON device_commands(status);
CREATE INDEX idx_device_commands_sent_at ON device_commands(sent_at);
CREATE INDEX idx_device_health_device_id ON device_health_records(device_id);
CREATE INDEX idx_device_health_check_time ON device_health_records(check_time);

-- Comments
COMMENT ON TABLE mobile_devices IS 'Mobile phone devices used for tracking';
COMMENT ON TABLE device_commands IS 'Commands sent to devices via Traccar';
COMMENT ON TABLE device_health_records IS 'Device health monitoring data';
