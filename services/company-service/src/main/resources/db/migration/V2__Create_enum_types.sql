-- Create enum types for company service
CREATE TYPE company_status AS ENUM ('TRIAL', 'ACTIVE', 'SUSPENDED');
CREATE TYPE subscription_plan AS ENUM ('BASIC', 'PREMIUM', 'ENTERPRISE');
CREATE TYPE invitation_status AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED');