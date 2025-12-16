-- =====================================================================
-- Wallet System Migration Script
-- Version: V22
-- Description: Create wallet, transactions, and ledger tables
-- Date: 2025-12-16
-- =====================================================================

-- 1. Create wallets table
CREATE TABLE IF NOT EXISTS wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_currency_code CHECK (currency_code ~ '^[A-Z]{3}$')
);

-- 2. Create transactions table (business events)
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    metadata JSONB,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    CONSTRAINT fk_transaction_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN
        ('MANUAL_CREDIT', 'MANUAL_DEBIT', 'TRANSFER', 'ADJUSTMENT', 'REVERSAL')),
    CONSTRAINT chk_transaction_status CHECK (status IN
        ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

-- 3. Create transaction_entries table (double-entry ledger - source of truth)
CREATE TABLE IF NOT EXISTS transaction_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL,
    wallet_id BIGINT NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    balance_after NUMERIC(19, 4),
    entry_sequence SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_entry_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE RESTRICT,
    CONSTRAINT fk_entry_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE RESTRICT,
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT uq_transaction_wallet_sequence UNIQUE (transaction_id, wallet_id, entry_sequence)
);

-- 4. Create manual_transfer_requests table (manual entry metadata)
CREATE TABLE IF NOT EXISTS manual_transfer_requests (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL UNIQUE,
    request_type VARCHAR(20) NOT NULL,
    from_user_id BIGINT,
    to_user_id BIGINT,
    amount NUMERIC(19, 4) NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    remarks TEXT,
    entered_by_user_id BIGINT NOT NULL,
    entered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_manual_request_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE RESTRICT,
    CONSTRAINT fk_manual_from_user FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_manual_to_user FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_manual_entered_by FOREIGN KEY (entered_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_request_type CHECK (request_type IN ('CREDIT', 'DEBIT', 'TRANSFER')),
    CONSTRAINT chk_manual_parties CHECK (
        (request_type = 'CREDIT' AND from_user_id IS NULL AND to_user_id IS NOT NULL) OR
        (request_type = 'DEBIT' AND from_user_id IS NOT NULL AND to_user_id IS NULL) OR
        (request_type = 'TRANSFER' AND from_user_id IS NOT NULL AND to_user_id IS NOT NULL)
    )
);

-- 5. Create transaction_documents table (proof images - separate for performance)
CREATE TABLE IF NOT EXISTS transaction_documents (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL DEFAULT 'PROOF_OF_PAYMENT',
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_data BYTEA NOT NULL,
    file_size INTEGER NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_document_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE RESTRICT,
    CONSTRAINT chk_document_type CHECK (document_type IN
        ('PROOF_OF_PAYMENT', 'RECEIPT', 'INVOICE', 'BANK_STATEMENT'))
);

-- =====================================================================
-- Create Indexes for Performance
-- =====================================================================

-- Wallet indexes
CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_status ON wallets(status);

-- Transaction indexes
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_type_status ON transactions(transaction_type, status);
CREATE INDEX IF NOT EXISTS idx_transaction_creator ON transactions(created_by_user_id);

-- Transaction Entry indexes (CRITICAL for balance queries)
CREATE INDEX IF NOT EXISTS idx_entry_wallet_created ON transaction_entries(wallet_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_entry_transaction ON transaction_entries(transaction_id);
CREATE INDEX IF NOT EXISTS idx_entry_wallet_balance ON transaction_entries(wallet_id, id DESC);

-- Manual Transfer Request indexes
CREATE INDEX IF NOT EXISTS idx_manual_transaction ON manual_transfer_requests(transaction_id);
CREATE INDEX IF NOT EXISTS idx_manual_from_user ON manual_transfer_requests(from_user_id);
CREATE INDEX IF NOT EXISTS idx_manual_to_user ON manual_transfer_requests(to_user_id);
CREATE INDEX IF NOT EXISTS idx_manual_entered_at ON manual_transfer_requests(entered_at DESC);

-- Transaction Document indexes
CREATE INDEX IF NOT EXISTS idx_document_transaction ON transaction_documents(transaction_id);

-- =====================================================================
-- Create Comments for Documentation
-- =====================================================================

COMMENT ON TABLE wallets IS 'User wallets - NO balance field (calculated from ledger)';
COMMENT ON TABLE transactions IS 'Immutable business events (transaction wrapper)';
COMMENT ON TABLE transaction_entries IS 'Double-entry ledger - SOURCE OF TRUTH for all balances';
COMMENT ON TABLE manual_transfer_requests IS 'Metadata for manually entered transactions';
COMMENT ON TABLE transaction_documents IS 'Proof images stored separately to avoid JOIN overhead';

COMMENT ON COLUMN transaction_entries.balance_after IS 'Denormalized snapshot for query performance';
COMMENT ON COLUMN transaction_entries.amount IS 'Always positive - type determines debit/credit';
COMMENT ON COLUMN transaction_entries.entry_sequence IS 'Order within a transaction (1 for single, 1,2 for transfers)';

-- =====================================================================
-- End of Migration
-- =====================================================================
