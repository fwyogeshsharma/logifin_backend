-- V1__init.sql
-- Initial database schema for Logifin Backend

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Create index on active status for filtering
CREATE INDEX IF NOT EXISTS idx_user_active ON users(active);

-- Insert sample data
INSERT INTO users (first_name, last_name, email, phone, active) VALUES
    ('Admin', 'User', 'admin@logifin.com', '+1234567890', true),
    ('Test', 'User', 'test@logifin.com', '+0987654321', true);
