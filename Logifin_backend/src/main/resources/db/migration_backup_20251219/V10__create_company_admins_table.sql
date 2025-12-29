-- V10__create_company_admins_table.sql
-- Create company_admins table for tracking company ownership
-- The first user registered under a company becomes the company admin

CREATE TABLE IF NOT EXISTS company_admins (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_company_admin_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_company_admin_company UNIQUE (company_id)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_company_admin_company_id ON company_admins(company_id);
CREATE INDEX IF NOT EXISTS idx_company_admin_user_id ON company_admins(user_id);

-- Add comments for documentation
COMMENT ON TABLE company_admins IS 'Stores company admin/owner information. Each company has exactly one admin.';
COMMENT ON COLUMN company_admins.company_id IS 'Reference to the company';
COMMENT ON COLUMN company_admins.user_id IS 'Reference to the user who is the company admin';
COMMENT ON COLUMN company_admins.created_at IS 'Timestamp when the admin was assigned';
COMMENT ON COLUMN company_admins.updated_at IS 'Timestamp when the admin was last updated';
