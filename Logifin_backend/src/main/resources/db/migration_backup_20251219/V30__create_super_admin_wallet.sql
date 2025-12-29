-- =====================================================================
-- Super Admin Wallet Migration Script
-- Version: V30
-- Description: Create wallet for Super Admin (alok@faberwork.com)
-- Date: 2025-12-19
-- =====================================================================

-- Create wallet for Super Admin user if not exists
INSERT INTO wallets (user_id, currency_code, status, created_at, updated_at, version)
SELECT id, 'INR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM users
WHERE email = 'alok@faberwork.com'
AND NOT EXISTS (
    SELECT 1 FROM wallets WHERE user_id = (SELECT id FROM users WHERE email = 'alok@faberwork.com')
);

-- =====================================================================
-- End of Migration
-- =====================================================================
