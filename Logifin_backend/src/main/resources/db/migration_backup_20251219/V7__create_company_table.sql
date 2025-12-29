-- V7__create_company_table.sql
-- Create company table for organization management

CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(150),
    logo VARCHAR(500),
    description TEXT,
    website VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    country VARCHAR(100) DEFAULT 'India',
    gst_number VARCHAR(20),
    pan_number VARCHAR(20),
    company_registration_number VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verified_at TIMESTAMP,
    verified_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_company_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_company_name ON companies(name);
CREATE INDEX IF NOT EXISTS idx_company_email ON companies(email);
CREATE INDEX IF NOT EXISTS idx_company_gst ON companies(gst_number);
CREATE INDEX IF NOT EXISTS idx_company_pan ON companies(pan_number);
CREATE INDEX IF NOT EXISTS idx_company_is_active ON companies(is_active);
CREATE INDEX IF NOT EXISTS idx_company_is_verified ON companies(is_verified);

-- Add unique constraints
ALTER TABLE companies ADD CONSTRAINT uk_company_email UNIQUE (email);
ALTER TABLE companies ADD CONSTRAINT uk_company_gst UNIQUE (gst_number);
ALTER TABLE companies ADD CONSTRAINT uk_company_pan UNIQUE (pan_number);
ALTER TABLE companies ADD CONSTRAINT uk_company_registration UNIQUE (company_registration_number);

-- Add comments
COMMENT ON TABLE companies IS 'Stores company/organization information';
COMMENT ON COLUMN companies.name IS 'Legal company name';
COMMENT ON COLUMN companies.display_name IS 'Display name for UI';
COMMENT ON COLUMN companies.logo IS 'URL to company logo';
COMMENT ON COLUMN companies.gst_number IS 'GST registration number';
COMMENT ON COLUMN companies.pan_number IS 'PAN number';
COMMENT ON COLUMN companies.company_registration_number IS 'Company registration number (CIN/LLPIN)';
COMMENT ON COLUMN companies.verified_by IS 'User ID who verified the company';
