-- services/device-service/src/main/resources/db/migration/V3__Create_sensor_and_assignment_tables.sql

-- Sensor types table
CREATE TABLE sensor_types (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              sensor_name VARCHAR(50) NOT NULL UNIQUE,
                              description TEXT,
                              unit_of_measurement VARCHAR(20),
                              data_type VARCHAR(20) NOT NULL,
                              default_monthly_price DECIMAL(10,2),
                              is_active BOOLEAN NOT NULL DEFAULT true,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Device sensors table
CREATE TABLE device_sensors (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                sensor_type_id UUID NOT NULL REFERENCES sensor_types(id),
                                sensor_name VARCHAR(50) NOT NULL,
                                configuration JSONB,
                                is_enabled BOOLEAN NOT NULL DEFAULT true,
                                calibration_data JSONB,
                                last_reading_time TIMESTAMP,
                                last_reading_value DECIMAL(15,5),
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sensor subscriptions table
CREATE TABLE sensor_subscriptions (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                      sensor_type_id UUID NOT NULL REFERENCES sensor_types(id),
                                      monthly_price DECIMAL(10,2) NOT NULL,
                                      is_active BOOLEAN NOT NULL DEFAULT true,
                                      billing_start_date DATE NOT NULL,
                                      billing_end_date DATE,
                                      created_by UUID NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                      UNIQUE(device_id, sensor_type_id)
);

-- Device vehicle assignments table
CREATE TABLE device_vehicle_assignments (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                            vehicle_id UUID NOT NULL,
                                            assignment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            unassignment_date TIMESTAMP,
                                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                            assigned_by UUID NOT NULL,
                                            unassigned_by UUID,
                                            notes TEXT,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Device user assignments table
CREATE TABLE device_user_assignments (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                                         user_id UUID NOT NULL,
                                         assignment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         unassignment_date TIMESTAMP,
                                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                         assigned_by UUID NOT NULL,
                                         unassigned_by UUID,
                                         notes TEXT,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_device_sensors_device_id ON device_sensors(device_id);
CREATE INDEX idx_device_sensors_sensor_type_id ON device_sensors(sensor_type_id);
CREATE INDEX idx_sensor_subscriptions_device_id ON sensor_subscriptions(device_id);
CREATE INDEX idx_sensor_subscriptions_active ON sensor_subscriptions(is_active);
CREATE INDEX idx_device_vehicle_assignments_device_id ON device_vehicle_assignments(device_id);
CREATE INDEX idx_device_vehicle_assignments_vehicle_id ON device_vehicle_assignments(vehicle_id);
CREATE INDEX idx_device_user_assignments_device_id ON device_user_assignments(device_id);
CREATE INDEX idx_device_user_assignments_user_id ON device_user_assignments(user_id);

-- Insert default sensor types
INSERT INTO sensor_types (sensor_name, description, unit_of_measurement, data_type, default_monthly_price) VALUES
                                                                                                               ('FUEL_LEVEL', 'Fuel level monitoring', 'Liters', 'DECIMAL', 15.00),
                                                                                                               ('TEMPERATURE', 'Temperature monitoring', 'Celsius', 'DECIMAL', 10.00),
                                                                                                               ('DOOR_STATUS', 'Door open/close monitoring', 'Boolean', 'BOOLEAN', 8.00),
                                                                                                               ('ENGINE_STATUS', 'Engine on/off monitoring', 'Boolean', 'BOOLEAN', 12.00),
                                                                                                               ('SPEED', 'Vehicle speed monitoring', 'KM/H', 'DECIMAL', 5.00),
                                                                                                               ('BATTERY_VOLTAGE', 'Battery voltage monitoring', 'Volts', 'DECIMAL', 7.00),
                                                                                                               ('PANIC_BUTTON', 'Emergency panic button', 'Boolean', 'BOOLEAN', 20.00),
                                                                                                               ('CARGO_WEIGHT', 'Cargo weight monitoring', 'Kilograms', 'DECIMAL', 25.00);
