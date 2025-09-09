-- Vehicle Service Database Schema
-- Fleet Management System - Vehicle Operations

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create vehicle status enum
CREATE TYPE vehicle_status AS ENUM ('ACTIVE', 'MAINTENANCE', 'RETIRED', 'SOLD', 'INACTIVE');

-- Create vehicle type enum
CREATE TYPE vehicle_type AS ENUM ('CAR', 'TRUCK', 'MOTORCYCLE', 'BUS', 'VAN', 'CONSTRUCTION', 'MARINE', 'AGRICULTURAL');

-- Create vehicle category enum
CREATE TYPE vehicle_category AS ENUM ('DELIVERY', 'PASSENGER', 'HEAVY_EQUIPMENT', 'CONSTRUCTION', 'EMERGENCY', 'COMMERCIAL', 'PERSONAL');

-- Create fuel type enum
CREATE TYPE fuel_type AS ENUM ('GASOLINE', 'DIESEL', 'ELECTRIC', 'HYBRID', 'CNG', 'LPG');

-- Create assignment status enum
CREATE TYPE assignment_status AS ENUM ('ASSIGNED', 'UNASSIGNED', 'TEMPORARY', 'EXPIRED');

-- Create group type enum
CREATE TYPE group_type AS ENUM ('DEPARTMENT', 'REGION', 'TYPE', 'CUSTOM', 'LOCATION');

-- Create document type enum
CREATE TYPE document_type AS ENUM ('REGISTRATION', 'INSURANCE', 'INSPECTION', 'LICENSE', 'OTHER');

-- Main vehicles table
CREATE TABLE vehicles (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          company_id UUID NOT NULL,

    -- Basic Information
                          name VARCHAR(255) NOT NULL,
                          vin VARCHAR(17) UNIQUE,
                          license_plate VARCHAR(20) NOT NULL,
                          make VARCHAR(100) NOT NULL,
                          model VARCHAR(100) NOT NULL,
                          year INTEGER NOT NULL,
                          color VARCHAR(50),

    -- Vehicle Classification
                          vehicle_type vehicle_type NOT NULL,
                          vehicle_category vehicle_category,
                          fuel_type fuel_type NOT NULL,

    -- Specifications
                          engine_size VARCHAR(20),
                          transmission VARCHAR(20),
                          seating_capacity INTEGER,
                          cargo_capacity DECIMAL(10,2),
                          gross_weight DECIMAL(10,2),

    -- Dimensions
                          length_mm INTEGER,
                          width_mm INTEGER,
                          height_mm INTEGER,
                          wheelbase_mm INTEGER,

    -- Status and Lifecycle
                          status vehicle_status DEFAULT 'ACTIVE',
                          purchase_date DATE,
                          purchase_price DECIMAL(12,2),
                          current_mileage INTEGER DEFAULT 0,

    -- Location and Assignment
                          current_location_lat DECIMAL(10,8),
                          current_location_lng DECIMAL(11,8),
                          home_location VARCHAR(255),
                          current_driver_id UUID,

    -- Maintenance
                          last_service_date DATE,
                          next_service_due_date DATE,
                          next_service_due_mileage INTEGER,

    -- Insurance and Registration
                          insurance_provider VARCHAR(100),
                          insurance_policy_number VARCHAR(50),
                          insurance_expiry_date DATE,
                          registration_expiry_date DATE,

    -- Additional Information
                          notes TEXT,
                          custom_fields JSONB,

    -- Audit
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by UUID,
                          updated_by UUID,
                          version BIGINT DEFAULT 0,

    -- Constraints
                          CONSTRAINT chk_year CHECK (year >= 1900 AND year <= EXTRACT(YEAR FROM CURRENT_DATE) + 2),
    CONSTRAINT chk_seating_capacity CHECK (seating_capacity > 0),
    CONSTRAINT chk_mileage CHECK (current_mileage >= 0)
);

-- Vehicle groups for fleet organization
CREATE TABLE vehicle_groups (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                company_id UUID NOT NULL,
                                name VARCHAR(100) NOT NULL,
                                description TEXT,
                                parent_group_id UUID REFERENCES vehicle_groups(id),
                                group_type group_type DEFAULT 'CUSTOM',

    -- Group Configuration
                                max_vehicles INTEGER,
                                is_active BOOLEAN DEFAULT true,
                                sort_order INTEGER DEFAULT 0,

    -- Additional Information
                                location VARCHAR(255),
                                manager_id UUID,
                                custom_fields JSONB,

    -- Audit
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by UUID,
                                updated_by UUID,

    -- Constraints
                                CONSTRAINT uk_vehicle_groups_company_name UNIQUE (company_id, name),
                                CONSTRAINT chk_max_vehicles CHECK (max_vehicles > 0)
);

-- Many-to-many relationship between vehicles and groups
CREATE TABLE vehicle_group_memberships (
                                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                           vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                           vehicle_group_id UUID NOT NULL REFERENCES vehicle_groups(id) ON DELETE CASCADE,

    -- Membership Details
                                           assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           assigned_by UUID,
                                           is_primary BOOLEAN DEFAULT false,

    -- Audit
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                           CONSTRAINT uk_vehicle_group_membership UNIQUE (vehicle_id, vehicle_group_id)
);

-- Vehicle assignments to drivers
CREATE TABLE vehicle_assignments (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                     driver_id UUID NOT NULL,
                                     company_id UUID NOT NULL,

    -- Assignment Details
                                     assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     start_date DATE NOT NULL,
                                     end_date DATE,
                                     status assignment_status DEFAULT 'ASSIGNED',

    -- Assignment Type
                                     assignment_type VARCHAR(20) DEFAULT 'PERMANENT', -- PERMANENT, TEMPORARY, SHIFT
                                     shift_start_time TIME,
                                     shift_end_time TIME,

    -- Check-in/Check-out
                                     last_checkin_time TIMESTAMP,
                                     last_checkout_time TIMESTAMP,
                                     checkin_location_lat DECIMAL(10,8),
                                     checkin_location_lng DECIMAL(11,8),

    -- Assignment Notes
                                     notes TEXT,
                                     restrictions JSONB, -- Any special restrictions or permissions

    -- Audit
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by UUID,
                                     updated_by UUID,

    -- Constraints
                                     CONSTRAINT chk_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date),
                                     CONSTRAINT chk_shift_times CHECK (
                                         (shift_start_time IS NULL AND shift_end_time IS NULL) OR
                                         (shift_start_time IS NOT NULL AND shift_end_time IS NOT NULL)
                                         )
);

-- Vehicle documents storage
CREATE TABLE vehicle_documents (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                   company_id UUID NOT NULL,

    -- Document Information
                                   document_type document_type NOT NULL,
                                   document_name VARCHAR(255) NOT NULL,
                                   document_number VARCHAR(100),
                                   file_path VARCHAR(500),
                                   file_name VARCHAR(255),
                                   file_size BIGINT,
                                   mime_type VARCHAR(100),

    -- Document Validity
                                   issue_date DATE,
                                   expiry_date DATE,
                                   issuing_authority VARCHAR(255),

    -- Document Status
                                   is_active BOOLEAN DEFAULT true,
                                   is_verified BOOLEAN DEFAULT false,
                                   verification_date TIMESTAMP,
                                   verified_by UUID,

    -- Additional Information
                                   description TEXT,
                                   tags VARCHAR(500), -- Comma-separated tags for search

    -- Audit
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   uploaded_by UUID,

    -- Constraints
                                   CONSTRAINT chk_document_dates CHECK (expiry_date IS NULL OR expiry_date >= issue_date),
                                   CONSTRAINT chk_file_size CHECK (file_size > 0)
);

-- Vehicle maintenance schedules
CREATE TABLE vehicle_maintenance_schedules (
                                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                               vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                               company_id UUID NOT NULL,

    -- Maintenance Information
                                               maintenance_type VARCHAR(100) NOT NULL, -- OIL_CHANGE, TIRE_ROTATION, INSPECTION, etc.
                                               description TEXT,

    -- Scheduling
                                               frequency_type VARCHAR(20) NOT NULL, -- MILEAGE, TIME, BOTH
                                               frequency_value INTEGER NOT NULL, -- Miles or days
                                               last_maintenance_date DATE,
                                               last_maintenance_mileage INTEGER,
                                               next_due_date DATE,
                                               next_due_mileage INTEGER,

    -- Maintenance Details
                                               estimated_cost DECIMAL(10,2),
                                               estimated_duration_hours DECIMAL(5,2),
                                               priority_level INTEGER DEFAULT 3, -- 1=Critical, 2=High, 3=Normal, 4=Low

    -- Service Provider
                                               preferred_service_provider VARCHAR(255),
                                               service_provider_contact VARCHAR(255),

    -- Status
                                               is_active BOOLEAN DEFAULT true,
                                               is_overdue BOOLEAN DEFAULT false,

    -- Additional Information
                                               parts_required TEXT,
                                               special_instructions TEXT,

    -- Audit
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               created_by UUID,
                                               updated_by UUID,

    -- Constraints
                                               CONSTRAINT chk_frequency_value CHECK (frequency_value > 0),
                                               CONSTRAINT chk_priority_level CHECK (priority_level BETWEEN 1 AND 4),
                                               CONSTRAINT chk_estimated_cost CHECK (estimated_cost >= 0),
                                               CONSTRAINT chk_estimated_duration CHECK (estimated_duration_hours > 0)
);

-- Vehicle maintenance history
CREATE TABLE vehicle_maintenance_history (
                                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                             vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                             maintenance_schedule_id UUID REFERENCES vehicle_maintenance_schedules(id),
                                             company_id UUID NOT NULL,

    -- Maintenance Details
                                             maintenance_type VARCHAR(100) NOT NULL,
                                             maintenance_date DATE NOT NULL,
                                             vehicle_mileage INTEGER,
                                             description TEXT,

    -- Cost Information
                                             actual_cost DECIMAL(10,2),
                                             labor_cost DECIMAL(10,2),
                                             parts_cost DECIMAL(10,2),
                                             other_costs DECIMAL(10,2),

    -- Service Information
                                             service_provider VARCHAR(255),
                                             service_provider_contact VARCHAR(255),
                                             service_reference_number VARCHAR(100),
                                             technician_name VARCHAR(255),

    -- Parts and Labor
                                             parts_used TEXT,
                                             labor_hours DECIMAL(5,2),
                                             warranty_expiry_date DATE,
                                             warranty_mileage INTEGER,

    -- Quality Assessment
                                             quality_rating INTEGER, -- 1-5 stars
                                             customer_satisfaction INTEGER, -- 1-5 stars
                                             service_notes TEXT,

    -- Status
                                             is_warranty_work BOOLEAN DEFAULT false,
                                             is_recall_work BOOLEAN DEFAULT false,
                                             requires_followup BOOLEAN DEFAULT false,
                                             followup_date DATE,

    -- Audit
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             created_by UUID,

    -- Constraints
                                             CONSTRAINT chk_maintenance_costs CHECK (
                                                 actual_cost >= 0 AND labor_cost >= 0 AND
                                                 parts_cost >= 0 AND other_costs >= 0
                                                 ),
                                             CONSTRAINT chk_quality_ratings CHECK (
                                                 (quality_rating IS NULL OR quality_rating BETWEEN 1 AND 5) AND
                                                 (customer_satisfaction IS NULL OR customer_satisfaction BETWEEN 1 AND 5)
                                                 ),
                                             CONSTRAINT chk_labor_hours CHECK (labor_hours >= 0)
);

-- Fleet analytics summary (for caching aggregated data)
CREATE TABLE fleet_analytics_cache (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       company_id UUID NOT NULL,

    -- Cache Information
                                       cache_type VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
                                       cache_date DATE NOT NULL,

    -- Vehicle Metrics
                                       total_vehicles INTEGER DEFAULT 0,
                                       active_vehicles INTEGER DEFAULT 0,
                                       maintenance_vehicles INTEGER DEFAULT 0,
                                       retired_vehicles INTEGER DEFAULT 0,

    -- Assignment Metrics
                                       assigned_vehicles INTEGER DEFAULT 0,
                                       unassigned_vehicles INTEGER DEFAULT 0,

    -- Utilization Metrics
                                       average_utilization_rate DECIMAL(5,2), -- Percentage
                                       total_mileage INTEGER DEFAULT 0,
                                       average_mileage_per_vehicle DECIMAL(10,2),

    -- Cost Metrics
                                       total_maintenance_cost DECIMAL(12,2) DEFAULT 0,
                                       average_maintenance_cost_per_vehicle DECIMAL(10,2),

    -- Performance Metrics
                                       vehicles_due_maintenance INTEGER DEFAULT 0,
                                       overdue_maintenance_vehicles INTEGER DEFAULT 0,

    -- Additional Metrics
                                       fuel_consumption_total DECIMAL(12,2),
                                       average_fuel_efficiency DECIMAL(8,2), -- km/l or mpg

    -- Cache Metadata
                                       calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       expires_at TIMESTAMP,

    -- Constraints
                                       CONSTRAINT uk_fleet_analytics_cache UNIQUE (company_id, cache_type, cache_date),
                                       CONSTRAINT chk_vehicle_counts CHECK (
                                           total_vehicles >= 0 AND active_vehicles >= 0 AND
                                           maintenance_vehicles >= 0 AND retired_vehicles >= 0
                                           ),
                                       CONSTRAINT chk_utilization_rate CHECK (
                                           average_utilization_rate IS NULL OR
                                           (average_utilization_rate >= 0 AND average_utilization_rate <= 100)
                                           )
);

-- Create indexes for performance optimization
CREATE INDEX idx_vehicles_company_id ON vehicles(company_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_type ON vehicles(vehicle_type);
CREATE INDEX idx_vehicles_current_driver ON vehicles(current_driver_id);
CREATE INDEX idx_vehicles_vin ON vehicles(vin);
CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_created_at ON vehicles(created_at);

CREATE INDEX idx_vehicle_groups_company_id ON vehicle_groups(company_id);
CREATE INDEX idx_vehicle_groups_parent ON vehicle_groups(parent_group_id);
CREATE INDEX idx_vehicle_groups_type ON vehicle_groups(group_type);

CREATE INDEX idx_vehicle_assignments_vehicle_id ON vehicle_assignments(vehicle_id);
CREATE INDEX idx_vehicle_assignments_driver_id ON vehicle_assignments(driver_id);
CREATE INDEX idx_vehicle_assignments_company_id ON vehicle_assignments(company_id);
CREATE INDEX idx_vehicle_assignments_status ON vehicle_assignments(status);
CREATE INDEX idx_vehicle_assignments_dates ON vehicle_assignments(start_date, end_date);

CREATE INDEX idx_vehicle_documents_vehicle_id ON vehicle_documents(vehicle_id);
CREATE INDEX idx_vehicle_documents_type ON vehicle_documents(document_type);
CREATE INDEX idx_vehicle_documents_expiry ON vehicle_documents(expiry_date);
CREATE INDEX idx_vehicle_documents_active ON vehicle_documents(is_active);

CREATE INDEX idx_maintenance_schedules_vehicle_id ON vehicle_maintenance_schedules(vehicle_id);
CREATE INDEX idx_maintenance_schedules_due_date ON vehicle_maintenance_schedules(next_due_date);
CREATE INDEX idx_maintenance_schedules_due_mileage ON vehicle_maintenance_schedules(next_due_mileage);
CREATE INDEX idx_maintenance_schedules_overdue ON vehicle_maintenance_schedules(is_overdue);

CREATE INDEX idx_maintenance_history_vehicle_id ON vehicle_maintenance_history(vehicle_id);
CREATE INDEX idx_maintenance_history_date ON vehicle_maintenance_history(maintenance_date);
CREATE INDEX idx_maintenance_history_schedule_id ON vehicle_maintenance_history(maintenance_schedule_id);

CREATE INDEX idx_fleet_analytics_company_date ON fleet_analytics_cache(company_id, cache_date);
CREATE INDEX idx_fleet_analytics_type ON fleet_analytics_cache(cache_type);
CREATE INDEX idx_fleet_analytics_expires ON fleet_analytics_cache(expires_at);

-- Create triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$ language 'plpgsql';

CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicle_groups_updated_at BEFORE UPDATE ON vehicle_groups
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicle_assignments_updated_at BEFORE UPDATE ON vehicle_assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicle_documents_updated_at BEFORE UPDATE ON vehicle_documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_maintenance_schedules_updated_at BEFORE UPDATE ON vehicle_maintenance_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create function to automatically update vehicle version for optimistic locking
CREATE OR REPLACE FUNCTION increment_vehicle_version()
RETURNS TRIGGER AS $
BEGIN
    NEW.version = OLD.version + 1;
RETURN NEW;
END;
$ language 'plpgsql';

CREATE TRIGGER increment_vehicle_version_trigger BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION increment_vehicle_version();

-- Create function to validate VIN format (basic validation)
CREATE OR REPLACE FUNCTION validate_vin(vin_input TEXT)
RETURNS BOOLEAN AS $
BEGIN
    -- Basic VIN validation: 17 characters, alphanumeric (excluding I, O, Q)
    IF vin_input IS NULL OR LENGTH(vin_input) != 17 THEN
        RETURN FALSE;
END IF;

    IF vin_input ~ '^[A-HJ-NPR-Z0-9]{17} THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;
$ language 'plpgsql';

-- Add VIN validation constraint
ALTER TABLE vehicles ADD CONSTRAINT chk_vin_format
    CHECK (vin IS NULL OR validate_vin(vin));

-- Create function to check for assignment conflicts
CREATE OR REPLACE FUNCTION check_assignment_conflict(
    p_vehicle_id UUID,
    p_driver_id UUID,
    p_start_date DATE,
    p_end_date DATE,
    p_assignment_id UUID DEFAULT NULL
)
RETURNS BOOLEAN AS $
DECLARE
    conflict_count INTEGER;
BEGIN
    -- Check for overlapping assignments for the same vehicle
    SELECT COUNT(*) INTO conflict_count
    FROM vehicle_assignments
    WHERE vehicle_id = p_vehicle_id
    AND status = 'ASSIGNED'
    AND (p_assignment_id IS NULL OR id != p_assignment_id)
    AND (
        (p_end_date IS NULL AND (end_date IS NULL OR end_date >= p_start_date)) OR
        (p_end_date IS NOT NULL AND start_date <= p_end_date AND (end_date IS NULL OR end_date >= p_start_date))
    );

    -- Check for overlapping assignments for the same driver
    SELECT COUNT(*) + conflict_count INTO conflict_count
    FROM vehicle_assignments
    WHERE driver_id = p_driver_id
    AND status = 'ASSIGNED'
    AND (p_assignment_id IS NULL OR id != p_assignment_id)
    AND (
        (p_end_date IS NULL AND (end_date IS NULL OR end_date >= p_start_date)) OR
        (p_end_date IS NOT NULL AND start_date <= p_end_date AND (end_date IS NULL OR end_date >= p_start_date))
    );

    RETURN conflict_count = 0;
END;
$ language 'plpgsql';

-- Create some sample data for testing (optional - remove in production)
-- INSERT INTO vehicle_groups (company_id, name, description, group_type) VALUES
-- (uuid_generate_v4(), 'Default Group', 'Default vehicle group', 'DEPARTMENT');

COMMENT ON TABLE vehicles IS 'Main vehicles table storing all vehicle information';
COMMENT ON TABLE vehicle_groups IS 'Vehicle groups for fleet organization and management';
COMMENT ON TABLE vehicle_assignments IS 'Driver-vehicle assignments with time tracking';
COMMENT ON TABLE vehicle_documents IS 'Vehicle documents storage and management';
COMMENT ON TABLE vehicle_maintenance_schedules IS 'Maintenance scheduling and tracking';
COMMENT ON TABLE vehicle_maintenance_history IS 'Historical maintenance records';
COMMENT ON TABLE fleet_analytics_cache IS 'Cached analytics data for performance optimization';