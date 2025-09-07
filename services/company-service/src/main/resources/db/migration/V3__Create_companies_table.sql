-- Create main companies table
CREATE TABLE companies (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name VARCHAR(100) NOT NULL,
                           subdomain VARCHAR(50) UNIQUE,
                           industry VARCHAR(100),
                           phone VARCHAR(20),
                           email VARCHAR(255) UNIQUE NOT NULL,
                           website VARCHAR(255),
                           address TEXT,
                           logo_url VARCHAR(255),
                           status company_status NOT NULL DEFAULT 'TRIAL',
                           subscription_plan subscription_plan NOT NULL DEFAULT 'BASIC',
                           timezone VARCHAR(50) DEFAULT 'UTC',
                           language VARCHAR(5) DEFAULT 'en',
                           notes TEXT,

    -- Subscription limits
                           max_users INTEGER NOT NULL DEFAULT 5,
                           max_vehicles INTEGER NOT NULL DEFAULT 10,
                           current_user_count INTEGER DEFAULT 0,
                           current_vehicle_count INTEGER DEFAULT 0,
                           trial_end_date DATE,

    -- Contact person
                           contact_person_name VARCHAR(100),
                           contact_person_title VARCHAR(100),
                           contact_person_email VARCHAR(255),
                           contact_person_phone VARCHAR(20),

    -- Audit fields
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by UUID,
                           updated_by UUID,
                           version BIGINT DEFAULT 0
);