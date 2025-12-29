-- =====================================================================
-- Platform Fee Tracking Migration Script
-- Version: V27
-- Description: Add fields to track platform fees and original principal amounts
-- Date: 2025-12-18
-- =====================================================================

-- Add platform fee tracking columns to transactions table
ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS gross_amount NUMERIC(19, 4),
ADD COLUMN IF NOT EXISTS platform_fee_amount NUMERIC(19, 4),
ADD COLUMN IF NOT EXISTS net_amount NUMERIC(19, 4);

-- Add comments
COMMENT ON COLUMN transactions.gross_amount IS 'Original amount before platform fee deduction (used for interest calculation)';
COMMENT ON COLUMN transactions.platform_fee_amount IS 'Platform fee deducted from the transfer';
COMMENT ON COLUMN transactions.net_amount IS 'Net amount after platform fee deduction (actual amount transferred)';

-- Create trip_financials table to track original amounts for interest calculation
CREATE TABLE IF NOT EXISTS trip_financials (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    financing_transaction_id UUID,
    repayment_transaction_id UUID,

    -- Original amounts (before fees)
    original_principal_amount NUMERIC(19, 4) NOT NULL,
    platform_fee_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    net_amount_to_transporter NUMERIC(19, 4) NOT NULL,

    -- Interest tracking
    interest_rate NUMERIC(5, 2) NOT NULL,
    financing_date TIMESTAMP NOT NULL,
    repayment_date TIMESTAMP,
    days_used INTEGER,
    calculated_interest NUMERIC(19, 4),

    -- Repayment tracking
    total_repayment_amount NUMERIC(19, 4),
    principal_repaid NUMERIC(19, 4),
    interest_repaid NUMERIC(19, 4),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'FINANCED',

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_trip_financial_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_financial_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_financial_financing_txn FOREIGN KEY (financing_transaction_id) REFERENCES transactions(transaction_id) ON DELETE SET NULL,
    CONSTRAINT fk_trip_financial_repayment_txn FOREIGN KEY (repayment_transaction_id) REFERENCES transactions(transaction_id) ON DELETE SET NULL,
    CONSTRAINT chk_trip_financial_status CHECK (status IN ('FINANCED', 'REPAID', 'DEFAULTED'))
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_trip_financials_trip ON trip_financials(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_financials_contract ON trip_financials(contract_id);
CREATE INDEX IF NOT EXISTS idx_trip_financials_status ON trip_financials(status);
CREATE INDEX IF NOT EXISTS idx_trip_financials_financing_date ON trip_financials(financing_date);

-- Add comments
COMMENT ON TABLE trip_financials IS 'Tracks original principal amounts and platform fees for accurate interest calculation';
COMMENT ON COLUMN trip_financials.original_principal_amount IS 'Original amount financed (before platform fee) - used for interest calculation';
COMMENT ON COLUMN trip_financials.net_amount_to_transporter IS 'Net amount received by transporter after platform fee deduction';
COMMENT ON COLUMN trip_financials.platform_fee_amount IS 'Platform fee deducted during financing';

-- =====================================================================
-- End of Migration
-- =====================================================================
