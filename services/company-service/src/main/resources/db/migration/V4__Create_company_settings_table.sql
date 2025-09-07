CREATE TABLE company_settings (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  company_id UUID NOT NULL,
                                  setting_key VARCHAR(255) NOT NULL,
                                  setting_value JSONB NOT NULL,
                                  description TEXT,
                                  is_encrypted BOOLEAN DEFAULT false,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  created_by UUID,
                                  updated_by UUID,
                                  CONSTRAINT uk_company_settings_company_key UNIQUE (company_id, setting_key)
);