-- =====================================================================
-- Default Wallets Migration Script
-- Version: V23
-- Description: Insert default wallet entries for user ID 1 and 2 if not exists
-- Date: 2025-12-16
-- =====================================================================

-- Insert wallet for user ID 1 if not exists
INSERT INTO wallets (user_id, currency_code, status, created_at, updated_at, version)
SELECT 1, 'INR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM wallets WHERE user_id = 1
)
AND EXISTS (
    SELECT 1 FROM users WHERE id = 1
);

-- Insert wallet for user ID 2 if not exists
INSERT INTO wallets (user_id, currency_code, status, created_at, updated_at, version)
SELECT 2, 'INR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (
    SELECT 1 FROM wallets WHERE user_id = 2
)
AND EXISTS (
    SELECT 1 FROM users WHERE id = 2
);

-- =====================================================================
-- End of Migration
-- =====================================================================
