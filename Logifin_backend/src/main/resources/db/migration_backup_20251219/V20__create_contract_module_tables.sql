-- V20__create_contract_module_tables.sql
-- Create tables for Contract Agreement Module

-- =====================================================
-- Table 1: load_stages (Master Data)
-- =====================================================
CREATE TABLE IF NOT EXISTS load_stages (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    stage_order INTEGER NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT chk_stage_order_positive CHECK (stage_order > 0)
);

-- Create index
CREATE INDEX IF NOT EXISTS idx_load_stage_name ON load_stages(stage_name);
CREATE INDEX IF NOT EXISTS idx_load_stage_order ON load_stages(stage_order);

-- Insert master data for load stages
INSERT INTO load_stages (stage_name, description, stage_order) VALUES
    ('PENDING', 'Initial stage - contract created but no documents uploaded', 1),
    ('BILTY_UPLOADED', 'Bilty document has been uploaded', 2),
    ('TRUCK_INVOICE_UPLOADED', 'Truck invoice document has been uploaded', 3),
    ('POD_UPLOADED', 'Proof of Delivery (POD) document has been uploaded', 4),
    ('FINAL_INVOICE', 'Final invoice stage - contract completion', 5);

-- Add comments
COMMENT ON TABLE load_stages IS 'Master data table for contract load stages';
COMMENT ON COLUMN load_stages.stage_name IS 'Unique stage name identifier';
COMMENT ON COLUMN load_stages.stage_order IS 'Sequential order of the stage in the workflow';

-- =====================================================
-- Table 2: contract_types (Master Data)
-- =====================================================
CREATE TABLE IF NOT EXISTS contract_types (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    party_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT chk_party_count_range CHECK (party_count >= 1 AND party_count <= 5)
);

-- Create index
CREATE INDEX IF NOT EXISTS idx_contract_type_name ON contract_types(type_name);
CREATE INDEX IF NOT EXISTS idx_contract_type_party_count ON contract_types(party_count);

-- Insert master data for contract types
INSERT INTO contract_types (type_name, description, party_count) VALUES
    ('SINGLE_PARTY_WITH_LOGIFIN', 'Single party contract with Logifin', 1),
    ('TWO_PARTY_WITH_LOGIFIN', 'Two party contract with Logifin', 2),
    ('THREE_PARTY_WITH_LOGIFIN', 'Three party contract with Logifin', 3),
    ('FOUR_PARTY_WITH_LOGIFIN', 'Four party contract with Logifin', 4),
    ('FIVE_PARTY_WITH_LOGIFIN', 'Five party contract with Logifin', 5);

-- Add comments
COMMENT ON TABLE contract_types IS 'Master data table for contract type definitions';
COMMENT ON COLUMN contract_types.type_name IS 'Unique contract type identifier';
COMMENT ON COLUMN contract_types.party_count IS 'Number of parties involved (1-5)';

-- =====================================================
-- Table 3: contracts (Main Table)
-- =====================================================
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,

    -- Contract Document (stored as base16/hex encoded)
    contract_document TEXT NOT NULL,
    contract_document_name VARCHAR(255),
    contract_document_content_type VARCHAR(100),

    -- Financial Terms
    loan_percent NUMERIC(5, 2) NOT NULL,
    ltv NUMERIC(5, 2) NOT NULL,
    penalty_ratio NUMERIC(5, 2) NOT NULL,

    -- Contract Metadata
    contract_number VARCHAR(50) UNIQUE,
    expiry_date DATE NOT NULL,

    -- Foreign Keys
    contract_type_id BIGINT NOT NULL,
    contract_manager_id BIGINT NOT NULL,
    consigner_company_id BIGINT NOT NULL,
    load_stage_id BIGINT,

    -- Status
    status VARCHAR(30) DEFAULT 'ACTIVE',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    version BIGINT DEFAULT 0,

    -- Foreign Key Constraints
    CONSTRAINT fk_contract_type FOREIGN KEY (contract_type_id) REFERENCES contract_types(id),
    CONSTRAINT fk_contract_manager FOREIGN KEY (contract_manager_id) REFERENCES users(id),
    CONSTRAINT fk_consigner_company FOREIGN KEY (consigner_company_id) REFERENCES companies(id),
    CONSTRAINT fk_load_stage FOREIGN KEY (load_stage_id) REFERENCES load_stages(id),
    CONSTRAINT fk_contract_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_loan_percent_range CHECK (loan_percent >= 0 AND loan_percent <= 100),
    CONSTRAINT chk_ltv_range CHECK (ltv >= 0 AND ltv <= 100),
    CONSTRAINT chk_penalty_ratio_range CHECK (penalty_ratio >= 0 AND penalty_ratio <= 100),
    CONSTRAINT chk_expiry_date_future CHECK (expiry_date > CURRENT_DATE),
    CONSTRAINT chk_contract_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'TERMINATED', 'COMPLETED'))
);

-- Create Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_contract_number ON contracts(contract_number);
CREATE INDEX IF NOT EXISTS idx_contract_type ON contracts(contract_type_id);
CREATE INDEX IF NOT EXISTS idx_contract_manager ON contracts(contract_manager_id);
CREATE INDEX IF NOT EXISTS idx_consigner_company ON contracts(consigner_company_id);
CREATE INDEX IF NOT EXISTS idx_contract_load_stage ON contracts(load_stage_id);
CREATE INDEX IF NOT EXISTS idx_contract_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contract_expiry_date ON contracts(expiry_date);
CREATE INDEX IF NOT EXISTS idx_contract_created_at ON contracts(created_at);
CREATE INDEX IF NOT EXISTS idx_contract_created_by ON contracts(created_by_user_id);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_contract_status_expiry ON contracts(status, expiry_date);
CREATE INDEX IF NOT EXISTS idx_contract_company_status ON contracts(consigner_company_id, status);
CREATE INDEX IF NOT EXISTS idx_contract_manager_status ON contracts(contract_manager_id, status);

-- Add Comments
COMMENT ON TABLE contracts IS 'Main table storing contract agreement information';
COMMENT ON COLUMN contracts.contract_document IS 'Contract document stored as base16 (hexadecimal) encoded string';
COMMENT ON COLUMN contracts.loan_percent IS 'Loan percentage (0-100)';
COMMENT ON COLUMN contracts.ltv IS 'Loan-to-value ratio percentage (0-100)';
COMMENT ON COLUMN contracts.penalty_ratio IS 'Penalty ratio percentage (0-100)';
COMMENT ON COLUMN contracts.contract_number IS 'Unique contract reference number';
COMMENT ON COLUMN contracts.expiry_date IS 'Contract expiration date';
COMMENT ON COLUMN contracts.contract_manager_id IS 'User managing this contract';
COMMENT ON COLUMN contracts.consigner_company_id IS 'Company that is the consigner';
COMMENT ON COLUMN contracts.load_stage_id IS 'Current load stage (optional)';
COMMENT ON COLUMN contracts.status IS 'Contract status: ACTIVE, EXPIRED, TERMINATED, COMPLETED';

-- =====================================================
-- Table 4: contract_parties (Junction Table)
-- =====================================================
CREATE TABLE IF NOT EXISTS contract_parties (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    signed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Foreign Key Constraints
    CONSTRAINT fk_contract_party_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_contract_party_user FOREIGN KEY (user_id) REFERENCES users(id),

    -- Unique constraint to prevent duplicate party entries
    CONSTRAINT uk_contract_user UNIQUE (contract_id, user_id)
);

-- Create Indexes
CREATE INDEX IF NOT EXISTS idx_contract_parties_contract ON contract_parties(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_parties_user ON contract_parties(user_id);

-- Add Comments
COMMENT ON TABLE contract_parties IS 'Junction table storing parties involved in each contract';
COMMENT ON COLUMN contract_parties.contract_id IS 'Reference to the contract';
COMMENT ON COLUMN contract_parties.user_id IS 'Reference to the user who is a party';
COMMENT ON COLUMN contract_parties.signed_at IS 'Timestamp when the party signed the contract';
