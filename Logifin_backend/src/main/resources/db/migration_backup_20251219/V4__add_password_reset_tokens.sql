-- Password Reset Tokens Table
-- Stores tokens for password reset functionality

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_token_status
        CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED'))
);

-- Create indexes for efficient lookups
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_status ON password_reset_tokens(status);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);

-- Comment on table
COMMENT ON TABLE password_reset_tokens IS 'Stores password reset tokens for users';
COMMENT ON COLUMN password_reset_tokens.token IS 'Unique secure token for password reset';
COMMENT ON COLUMN password_reset_tokens.expiry_date IS 'Token expiration timestamp';
COMMENT ON COLUMN password_reset_tokens.status IS 'Token status: ACTIVE, USED, or EXPIRED';
