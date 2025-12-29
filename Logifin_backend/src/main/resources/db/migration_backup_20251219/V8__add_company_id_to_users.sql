-- V8__add_company_id_to_users.sql
-- Add company_id column to users table

-- Add company_id column
ALTER TABLE users ADD COLUMN company_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id) REFERENCES companies(id);

-- Create index for company_id
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);

-- Add comment
COMMENT ON COLUMN users.company_id IS 'Foreign key reference to companies table';
