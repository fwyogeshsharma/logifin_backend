-- =====================================================================
-- Contract-Trip Relationship Migration Script
-- Version: V26
-- Description: Add interest rate to contracts and link trips to contracts
-- Date: 2025-12-18
-- =====================================================================

-- Add interest_rate column to contracts table
ALTER TABLE contracts
ADD COLUMN IF NOT EXISTS interest_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.00
    CHECK (interest_rate >= 0.00 AND interest_rate <= 100.00);

COMMENT ON COLUMN contracts.interest_rate IS 'Annual interest rate percentage for the contract';

-- Add contract_id column to trips table (many trips can be financed under one contract)
ALTER TABLE trips
ADD COLUMN IF NOT EXISTS contract_id BIGINT;

-- Add foreign key constraint
ALTER TABLE trips
ADD CONSTRAINT fk_trip_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_trip_contract ON trips(contract_id);

-- Add trip_id column to transactions for tracking trip-related transactions
ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS trip_id BIGINT,
ADD COLUMN IF NOT EXISTS contract_id BIGINT,
ADD COLUMN IF NOT EXISTS transaction_purpose VARCHAR(50);

-- Add foreign key constraints for transaction tracking
ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_transaction_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL;

-- Add check constraint for transaction purpose
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_purpose CHECK (
    transaction_purpose IS NULL OR
    transaction_purpose IN ('FINANCING', 'REPAYMENT', 'PORTAL_FEE', 'INTEREST_PAYMENT', 'PRINCIPAL_PAYMENT', 'OTHER')
);

-- Create indexes for transaction queries
CREATE INDEX IF NOT EXISTS idx_transaction_trip ON transactions(trip_id);
CREATE INDEX IF NOT EXISTS idx_transaction_contract ON transactions(contract_id);
CREATE INDEX IF NOT EXISTS idx_transaction_purpose ON transactions(transaction_purpose);

-- Add comments
COMMENT ON COLUMN trips.contract_id IS 'Reference to the contract under which this trip is financed';
COMMENT ON COLUMN transactions.trip_id IS 'Reference to trip if transaction is related to a specific trip';
COMMENT ON COLUMN transactions.contract_id IS 'Reference to contract if transaction is related to a specific contract';
COMMENT ON COLUMN transactions.transaction_purpose IS 'Purpose of the transaction: FINANCING, REPAYMENT, PORTAL_FEE, etc.';

-- =====================================================================
-- End of Migration
-- =====================================================================
