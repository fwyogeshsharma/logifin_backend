-- =====================================================================
-- Configuration Table Migration Script
-- Version: V25
-- Description: Create configurations table for system settings
-- Date: 2025-12-18
-- =====================================================================

-- Create configurations table
CREATE TABLE IF NOT EXISTS configurations (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    updated_by_user_id BIGINT,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_config_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_config_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_config_type CHECK (config_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'PERCENTAGE', 'JSON'))
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_config_key ON configurations(config_key);
CREATE INDEX IF NOT EXISTS idx_config_active ON configurations(is_active);

-- Insert default portal service charge configuration
INSERT INTO configurations (config_key, config_value, config_type, description, is_active)
VALUES
    ('portal_service_charge', '0.5', 'PERCENTAGE', 'Portal service charge percentage deducted from transfers (0.5%)', TRUE),
    ('interest_calculation_method', 'DAILY', 'STRING', 'Method for calculating interest: DAILY, MONTHLY, YEARLY', TRUE),
    ('allow_negative_balance', 'true', 'BOOLEAN', 'Allow wallets to go into negative balance (borrowing)', TRUE);

-- Add comments
COMMENT ON TABLE configurations IS 'System configuration settings for portal behavior';
COMMENT ON COLUMN configurations.config_key IS 'Unique key identifier for the configuration';
COMMENT ON COLUMN configurations.config_value IS 'Configuration value stored as text';
COMMENT ON COLUMN configurations.config_type IS 'Data type of the configuration value';
COMMENT ON COLUMN configurations.is_active IS 'Whether this configuration is currently active';

-- =====================================================================
-- End of Migration
-- =====================================================================
