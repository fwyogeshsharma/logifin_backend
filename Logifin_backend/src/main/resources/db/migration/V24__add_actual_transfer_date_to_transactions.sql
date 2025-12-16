-- =====================================================================
-- Add Actual Transfer Date to Transactions
-- Version: V24
-- Description: Add actual_transfer_date column to transactions table
-- Date: 2025-12-16
-- =====================================================================

-- Add actual_transfer_date column to transactions table
ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS actual_transfer_date TIMESTAMP;

-- Create index on actual_transfer_date for query performance
CREATE INDEX IF NOT EXISTS idx_transaction_actual_transfer_date
ON transactions(actual_transfer_date DESC);

-- Add comment to document the column
COMMENT ON COLUMN transactions.actual_transfer_date IS 'Date when amount is actually transferred to other account';

-- =====================================================================
-- End of Migration
-- =====================================================================
