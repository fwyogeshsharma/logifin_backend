-- V3__add_user_roles.sql
-- Create user_roles table and add role_id to users table

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create index for role_name
CREATE INDEX IF NOT EXISTS idx_role_name ON user_roles(role_name);

-- Insert initial roles
INSERT INTO user_roles (role_name, description) VALUES
    ('ROLE_LENDER', 'Lender role with access to lending operations'),
    ('ROLE_TRANSPORTER', 'Transporter role with access to transport operations'),
    ('ROLE_TRUST_ACCOUNT', 'Trust account role with access to trust account operations'),
    ('ROLE_CSR', 'Customer Service Representative role'),
    ('ROLE_ADMIN', 'Administrator role with elevated privileges'),
    ('ROLE_SUPER_ADMIN', 'Super Administrator role with full system access');

-- Add role_id column to users table
ALTER TABLE users ADD COLUMN role_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES user_roles(id);

-- Create index for role_id in users table
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Add password column to users table for authentication
ALTER TABLE users ADD COLUMN password VARCHAR(255);

-- Update existing users to have default role and password
-- Admin user gets ROLE_SUPER_ADMIN (id=6), Test user gets ROLE_CSR (id=4)
-- Default password is 'admin123' (BCrypt encoded)
UPDATE users SET role_id = 6, password = '$2a$10$4uIJUcewOG8eWRjDKui0iu68zJBw./frLjW1uM7Mq9VNGTIoYYEri' WHERE email = 'admin@logifin.com';
UPDATE users SET role_id = 4, password = '$2a$10$4uIJUcewOG8eWRjDKui0iu68zJBw./frLjW1uM7Mq9VNGTIoYYEri' WHERE email = 'test@logifin.com';

-- Add comments
COMMENT ON TABLE user_roles IS 'Stores user role definitions for RBAC';
COMMENT ON COLUMN users.role_id IS 'Foreign key reference to user_roles table';
COMMENT ON COLUMN users.password IS 'Encrypted password for user authentication';
