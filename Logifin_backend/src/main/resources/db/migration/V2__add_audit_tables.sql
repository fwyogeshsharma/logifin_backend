-- V2__add_audit_tables.sql
-- Add audit logging table for tracking changes

-- Create audit_log table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(100),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit_log
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_performed_at ON audit_log(performed_at);

-- Add comment to users table
COMMENT ON TABLE users IS 'Stores user information for Logifin system';
COMMENT ON COLUMN users.version IS 'Optimistic locking version number';
