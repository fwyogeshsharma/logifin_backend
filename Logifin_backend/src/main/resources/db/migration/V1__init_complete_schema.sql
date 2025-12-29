-- =====================================================================
-- LOGIFIN PRODUCTION DATABASE - CONSOLIDATED SCHEMA
-- Version: V1-V30 Combined
-- Description: Complete database schema combining all migrations V1 through V30
-- Date: 2025-12-19
-- =====================================================================
-- This consolidated schema includes:
-- 1. All DDL statements (CREATE TABLE, ALTER TABLE, CREATE INDEX)
-- 2. All DML statements (INSERT INTO)
-- 3. Proper execution order to handle dependencies
-- =====================================================================

-- =====================================================================
-- SECTION 1: CORE TABLES - USERS & ROLES
-- =====================================================================

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255),
    company_id BIGINT,
    role_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for users table
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_active ON users(active);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create index for role_name
CREATE INDEX IF NOT EXISTS idx_role_name ON user_roles(role_name);

-- Add foreign key constraint for role_id in users table
ALTER TABLE users ADD CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES user_roles(id);

-- Create index for role_id in users table
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Add comments
COMMENT ON TABLE users IS 'Stores user information for Logifin system';
COMMENT ON COLUMN users.version IS 'Optimistic locking version number';
COMMENT ON COLUMN users.role_id IS 'Foreign key reference to user_roles table';
COMMENT ON COLUMN users.password IS 'Encrypted password for user authentication';
COMMENT ON TABLE user_roles IS 'Stores user role definitions for RBAC';

-- =====================================================================
-- SECTION 2: AUDIT & LOGGING TABLES
-- =====================================================================

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

-- =====================================================================
-- SECTION 3: PASSWORD RESET TOKENS
-- =====================================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
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

-- Create indexes for password_reset_tokens
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_status ON password_reset_tokens(status);
CREATE INDEX IF NOT EXISTS idx_password_reset_expiry ON password_reset_tokens(expiry_date);

-- Add comments
COMMENT ON TABLE password_reset_tokens IS 'Stores password reset tokens for users';
COMMENT ON COLUMN password_reset_tokens.token IS 'Unique secure token for password reset';
COMMENT ON COLUMN password_reset_tokens.expiry_date IS 'Token expiration timestamp';
COMMENT ON COLUMN password_reset_tokens.status IS 'Token status: ACTIVE, USED, or EXPIRED';

-- =====================================================================
-- SECTION 4: COMPANY & ORGANIZATION TABLES
-- =====================================================================

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(150),
    logo_base64 TEXT,
    description TEXT,
    website VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    country VARCHAR(100) DEFAULT 'India',
    gst_number VARCHAR(20),
    pan_number VARCHAR(20),
    company_registration_number VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verified_at TIMESTAMP,
    verified_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_company_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
);

-- Create indexes for companies table
CREATE INDEX IF NOT EXISTS idx_company_name ON companies(name);
CREATE INDEX IF NOT EXISTS idx_company_email ON companies(email);
CREATE INDEX IF NOT EXISTS idx_company_gst ON companies(gst_number);
CREATE INDEX IF NOT EXISTS idx_company_pan ON companies(pan_number);
CREATE INDEX IF NOT EXISTS idx_company_is_active ON companies(is_active);
CREATE INDEX IF NOT EXISTS idx_company_is_verified ON companies(is_verified);

-- Add unique constraints for companies
ALTER TABLE companies ADD CONSTRAINT uk_company_email UNIQUE (email);
ALTER TABLE companies ADD CONSTRAINT uk_company_gst UNIQUE (gst_number);
ALTER TABLE companies ADD CONSTRAINT uk_company_pan UNIQUE (pan_number);
ALTER TABLE companies ADD CONSTRAINT uk_company_registration UNIQUE (company_registration_number);

-- Add company_id column to users table
ALTER TABLE users ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id) REFERENCES companies(id);

-- Create index for company_id in users table
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);

-- Add comments for companies table
COMMENT ON TABLE companies IS 'Stores company/organization information';
COMMENT ON COLUMN companies.name IS 'Legal company name';
COMMENT ON COLUMN companies.display_name IS 'Display name for UI';
COMMENT ON COLUMN companies.logo_base64 IS 'Base64 encoded company logo image';
COMMENT ON COLUMN companies.gst_number IS 'GST registration number';
COMMENT ON COLUMN companies.pan_number IS 'PAN number';
COMMENT ON COLUMN companies.company_registration_number IS 'Company registration number (CIN/LLPIN)';
COMMENT ON COLUMN companies.verified_by IS 'User ID who verified the company';
COMMENT ON COLUMN users.company_id IS 'Foreign key reference to companies table';

-- Create company_admins table
CREATE TABLE IF NOT EXISTS company_admins (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_company_admin_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_company_admin_company UNIQUE (company_id)
);

-- Create indexes for company_admins
CREATE INDEX IF NOT EXISTS idx_company_admin_company_id ON company_admins(company_id);
CREATE INDEX IF NOT EXISTS idx_company_admin_user_id ON company_admins(user_id);

-- Add comments for company_admins
COMMENT ON TABLE company_admins IS 'Stores company admin/owner information. Each company has exactly one admin.';
COMMENT ON COLUMN company_admins.company_id IS 'Reference to the company';
COMMENT ON COLUMN company_admins.user_id IS 'Reference to the user who is the company admin';
COMMENT ON COLUMN company_admins.created_at IS 'Timestamp when the admin was assigned';
COMMENT ON COLUMN company_admins.updated_at IS 'Timestamp when the admin was last updated';

-- =====================================================================
-- SECTION 5: CONTRACT MODULE TABLES (Must be before TRIPS)
-- =====================================================================

-- Create loan_stages table (renamed from load_stages in V21)
CREATE TABLE IF NOT EXISTS loan_stages (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    stage_order INTEGER NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT chk_stage_order_positive CHECK (stage_order > 0)
);

-- Create indexes for loan_stages
CREATE INDEX IF NOT EXISTS idx_loan_stage_name ON loan_stages(stage_name);
CREATE INDEX IF NOT EXISTS idx_loan_stage_order ON loan_stages(stage_order);

-- Add comments for loan_stages
COMMENT ON TABLE loan_stages IS 'Loan stages master data (renamed from load_stages)';

-- Create contract_types table
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

-- Create indexes for contract_types
CREATE INDEX IF NOT EXISTS idx_contract_type_name ON contract_types(type_name);
CREATE INDEX IF NOT EXISTS idx_contract_type_party_count ON contract_types(party_count);

-- Add comments for contract_types
COMMENT ON TABLE contract_types IS 'Master data table for contract type definitions';
COMMENT ON COLUMN contract_types.type_name IS 'Unique contract type identifier';
COMMENT ON COLUMN contract_types.party_count IS 'Number of parties involved (1-5)';

-- Create contracts table
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
    interest_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    maturity_days INTEGER NOT NULL DEFAULT 30,

    -- Contract Metadata
    contract_number VARCHAR(50) UNIQUE,
    expiry_date DATE NOT NULL,

    -- Foreign Keys
    contract_type_id BIGINT NOT NULL,
    contract_manager_id BIGINT NOT NULL,
    consigner_company_id BIGINT NOT NULL,
    loan_stage_id BIGINT,

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
    CONSTRAINT fk_loan_stage FOREIGN KEY (loan_stage_id) REFERENCES loan_stages(id) ON DELETE SET NULL,
    CONSTRAINT fk_contract_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_loan_percent_range CHECK (loan_percent >= 0 AND loan_percent <= 100),
    CONSTRAINT chk_ltv_range CHECK (ltv >= 0 AND ltv <= 100),
    CONSTRAINT chk_penalty_ratio_range CHECK (penalty_ratio >= 0 AND penalty_ratio <= 100),
    CONSTRAINT chk_expiry_date_future CHECK (expiry_date > CURRENT_DATE),
    CONSTRAINT chk_contract_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'TERMINATED', 'COMPLETED')),
    CONSTRAINT chk_contract_interest_rate CHECK (interest_rate >= 0.00 AND interest_rate <= 100.00),
    CONSTRAINT chk_contract_maturity_days CHECK (maturity_days >= 1 AND maturity_days <= 365)
);

-- Create Indexes for contracts
CREATE INDEX IF NOT EXISTS idx_contract_number ON contracts(contract_number);
CREATE INDEX IF NOT EXISTS idx_contract_type ON contracts(contract_type_id);
CREATE INDEX IF NOT EXISTS idx_contract_manager ON contracts(contract_manager_id);
CREATE INDEX IF NOT EXISTS idx_consigner_company ON contracts(consigner_company_id);
CREATE INDEX IF NOT EXISTS idx_contract_loan_stage ON contracts(loan_stage_id);
CREATE INDEX IF NOT EXISTS idx_contract_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contract_expiry_date ON contracts(expiry_date);
CREATE INDEX IF NOT EXISTS idx_contract_created_at ON contracts(created_at);
CREATE INDEX IF NOT EXISTS idx_contract_created_by ON contracts(created_by_user_id);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_contract_status_expiry ON contracts(status, expiry_date);
CREATE INDEX IF NOT EXISTS idx_contract_company_status ON contracts(consigner_company_id, status);
CREATE INDEX IF NOT EXISTS idx_contract_manager_status ON contracts(contract_manager_id, status);

-- Add Comments for contracts
COMMENT ON TABLE contracts IS 'Main table storing contract agreement information';
COMMENT ON COLUMN contracts.contract_document IS 'Contract document stored as base16 (hexadecimal) encoded string';
COMMENT ON COLUMN contracts.loan_percent IS 'Loan percentage (0-100)';
COMMENT ON COLUMN contracts.ltv IS 'Loan-to-value ratio percentage (0-100)';
COMMENT ON COLUMN contracts.penalty_ratio IS 'Penalty ratio percentage (0-100)';
COMMENT ON COLUMN contracts.contract_number IS 'Unique contract reference number';
COMMENT ON COLUMN contracts.expiry_date IS 'Contract expiration date';
COMMENT ON COLUMN contracts.contract_manager_id IS 'User managing this contract';
COMMENT ON COLUMN contracts.consigner_company_id IS 'Company that is the consigner';
COMMENT ON COLUMN contracts.loan_stage_id IS 'Current loan stage (optional)';
COMMENT ON COLUMN contracts.status IS 'Contract status: ACTIVE, EXPIRED, TERMINATED, COMPLETED';
COMMENT ON COLUMN contracts.interest_rate IS 'Annual interest rate percentage for the contract';
COMMENT ON COLUMN contracts.maturity_days IS 'Number of days until maturity for loans under this contract (1-365)';

-- Create contract_parties table (Junction Table)
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

-- Create Indexes for contract_parties
CREATE INDEX IF NOT EXISTS idx_contract_parties_contract ON contract_parties(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_parties_user ON contract_parties(user_id);

-- Add Comments for contract_parties
COMMENT ON TABLE contract_parties IS 'Junction table storing parties involved in each contract';
COMMENT ON COLUMN contract_parties.contract_id IS 'Reference to the contract';
COMMENT ON COLUMN contract_parties.user_id IS 'Reference to the user who is a party';
COMMENT ON COLUMN contract_parties.signed_at IS 'Timestamp when the party signed the contract';

-- =====================================================================
-- SECTION 6: TRIPS & TRIP RELATED TABLES
-- =====================================================================

-- Create trips table
CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,

    -- Route Information
    pickup VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,

    -- Parties Involved (now using user references - V29)
    sender_user_id BIGINT NOT NULL,
    receiver VARCHAR(150) NOT NULL,
    transporter_user_id BIGINT NOT NULL,

    -- Financial Terms
    loan_amount NUMERIC(17, 2) NOT NULL,
    interest_rate NUMERIC(5, 2) NOT NULL,
    maturity_days INTEGER NOT NULL,

    -- Cargo Details (Optional)
    distance_km NUMERIC(10, 2),
    load_type VARCHAR(100),
    weight_kg NUMERIC(10, 2),

    -- Additional Information
    notes TEXT,
    status VARCHAR(30) DEFAULT 'ACTIVE',

    -- References
    created_by_user_id BIGINT,
    company_id BIGINT,
    contract_id BIGINT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Foreign Key Constraints
    CONSTRAINT fk_trip_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_trip_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,
    CONSTRAINT fk_trip_sender FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trip_transporter FOREIGN KEY (transporter_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trip_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_loan_amount_positive CHECK (loan_amount > 0),
    CONSTRAINT chk_interest_rate_range CHECK (interest_rate >= 0 AND interest_rate <= 100),
    CONSTRAINT chk_maturity_days_range CHECK (maturity_days >= 1 AND maturity_days <= 365),
    CONSTRAINT chk_distance_non_negative CHECK (distance_km IS NULL OR distance_km >= 0),
    CONSTRAINT chk_weight_non_negative CHECK (weight_kg IS NULL OR weight_kg >= 0)
);

-- Create Indexes for trips
CREATE INDEX IF NOT EXISTS idx_trip_created_at ON trips(created_at);
CREATE INDEX IF NOT EXISTS idx_trip_pickup ON trips(pickup);
CREATE INDEX IF NOT EXISTS idx_trip_destination ON trips(destination);
CREATE INDEX IF NOT EXISTS idx_trip_created_by ON trips(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_company ON trips(company_id);
CREATE INDEX IF NOT EXISTS idx_trip_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trip_receiver ON trips(receiver);
CREATE INDEX IF NOT EXISTS idx_trip_sender ON trips(sender_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_transporter ON trips(transporter_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_transporter_status ON trips(transporter_user_id, status);
CREATE INDEX IF NOT EXISTS idx_trip_created_at_status ON trips(created_at DESC, status);
CREATE INDEX IF NOT EXISTS idx_trip_contract ON trips(contract_id);

-- Add Comments for trips
COMMENT ON TABLE trips IS 'Stores trip/shipment information including financial terms';
COMMENT ON COLUMN trips.loan_amount IS 'Loan amount for the trip';
COMMENT ON COLUMN trips.interest_rate IS 'Interest rate percentage (0-100)';
COMMENT ON COLUMN trips.maturity_days IS 'Number of days until maturity (1-365)';
COMMENT ON COLUMN trips.distance_km IS 'Distance of the trip in kilometers';
COMMENT ON COLUMN trips.weight_kg IS 'Weight of cargo in kilograms';
COMMENT ON COLUMN trips.status IS 'Trip status: ACTIVE, IN_TRANSIT, COMPLETED, CANCELLED';
COMMENT ON COLUMN trips.sender_user_id IS 'User ID of the sender (foreign key to users table)';
COMMENT ON COLUMN trips.transporter_user_id IS 'User ID of the transporter (foreign key to users table)';
COMMENT ON COLUMN trips.contract_id IS 'Reference to the contract under which this trip is financed';

-- =====================================================================
-- SECTION 7: DOCUMENT TYPES & TRIP DOCUMENTS
-- =====================================================================

-- Create document_types table
CREATE TABLE IF NOT EXISTS document_types (
    id BIGSERIAL PRIMARY KEY,

    -- Document Type Information
    code VARCHAR(30) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Index for document_types
CREATE INDEX IF NOT EXISTS idx_document_type_code ON document_types(code);
CREATE INDEX IF NOT EXISTS idx_document_type_active ON document_types(is_active);

-- Add Comments for document_types
COMMENT ON TABLE document_types IS 'Master table for document types used in trip documents';
COMMENT ON COLUMN document_types.code IS 'Unique identifier code: EWAY_BILL, BILTY, TRUCK_INVOICE, POD, FINAL_INVOICE';
COMMENT ON COLUMN document_types.display_name IS 'Human readable name for the document type';
COMMENT ON COLUMN document_types.sort_order IS 'Order for display purposes';

-- Create trip_documents table
CREATE TABLE IF NOT EXISTS trip_documents (
    id BIGSERIAL PRIMARY KEY,

    -- Trip Reference
    trip_id BIGINT NOT NULL,

    -- Document Information
    document_type_id BIGINT NOT NULL,
    document_number VARCHAR(100),
    document_data BYTEA,
    content_type VARCHAR(100),
    file_size BIGINT,

    -- Audit Fields
    uploaded_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT fk_trip_document_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_document_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_trip_documents_document_type FOREIGN KEY (document_type_id) REFERENCES document_types(id)
);

-- Create Indexes for trip_documents
CREATE INDEX IF NOT EXISTS idx_trip_document_trip_id ON trip_documents(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_document_type_id ON trip_documents(document_type_id);
CREATE INDEX IF NOT EXISTS idx_trip_document_unique_type ON trip_documents(trip_id, document_type_id);
CREATE INDEX IF NOT EXISTS idx_trip_document_uploaded_by ON trip_documents(uploaded_by_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_document_number ON trip_documents(document_number);

-- Add Comments for trip_documents
COMMENT ON TABLE trip_documents IS 'Stores all documents attached to trips. Each trip can have one document per document type.';
COMMENT ON COLUMN trip_documents.document_type_id IS 'Reference to document_types table';
COMMENT ON COLUMN trip_documents.document_number IS 'Reference number like E-way Bill Number, Bilty Number, Invoice Number, etc.';
COMMENT ON COLUMN trip_documents.document_data IS 'Base64 decoded document stored as bytea (optional)';
COMMENT ON COLUMN trip_documents.content_type IS 'MIME type of the document (e.g., image/png, application/pdf)';

-- =====================================================================
-- SECTION 8: WALLET & TRANSACTION SYSTEM
-- =====================================================================

-- Create wallets table
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

-- Create transactions table (business events)
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    metadata JSONB,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    actual_transfer_date TIMESTAMP,
    trip_id BIGINT,
    contract_id BIGINT,
    transaction_purpose VARCHAR(50),
    gross_amount NUMERIC(19, 4),
    platform_fee_amount NUMERIC(19, 4),
    net_amount NUMERIC(19, 4),

    CONSTRAINT fk_transaction_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN
        ('MANUAL_CREDIT', 'MANUAL_DEBIT', 'TRANSFER', 'ADJUSTMENT', 'REVERSAL')),
    CONSTRAINT chk_transaction_status CHECK (status IN
        ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED')),
    CONSTRAINT chk_transaction_purpose CHECK (
        transaction_purpose IS NULL OR
        transaction_purpose IN ('FINANCING', 'REPAYMENT', 'PORTAL_FEE', 'INTEREST_PAYMENT', 'PRINCIPAL_PAYMENT', 'OTHER')
    )
);

-- Create transaction_entries table (double-entry ledger - source of truth)
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

-- Create manual_transfer_requests table (manual entry metadata)
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

-- Create transaction_documents table (proof images - separate for performance)
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

-- Create Indexes for Wallet System
CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_status ON wallets(status);

-- Transaction indexes
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_type_status ON transactions(transaction_type, status);
CREATE INDEX IF NOT EXISTS idx_transaction_creator ON transactions(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_actual_transfer_date ON transactions(actual_transfer_date DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_trip ON transactions(trip_id);
CREATE INDEX IF NOT EXISTS idx_transaction_contract ON transactions(contract_id);
CREATE INDEX IF NOT EXISTS idx_transaction_purpose ON transactions(transaction_purpose);

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

-- Add Comments for Wallet System
COMMENT ON TABLE wallets IS 'User wallets - NO balance field (calculated from ledger)';
COMMENT ON TABLE transactions IS 'Immutable business events (transaction wrapper)';
COMMENT ON TABLE transaction_entries IS 'Double-entry ledger - SOURCE OF TRUTH for all balances';
COMMENT ON TABLE manual_transfer_requests IS 'Metadata for manually entered transactions';
COMMENT ON TABLE transaction_documents IS 'Proof images stored separately to avoid JOIN overhead';

COMMENT ON COLUMN transaction_entries.balance_after IS 'Denormalized snapshot for query performance';
COMMENT ON COLUMN transaction_entries.amount IS 'Always positive - type determines debit/credit';
COMMENT ON COLUMN transaction_entries.entry_sequence IS 'Order within a transaction (1 for single, 1,2 for transfers)';
COMMENT ON COLUMN transactions.actual_transfer_date IS 'Date when amount is actually transferred to other account';
COMMENT ON COLUMN transactions.trip_id IS 'Reference to trip if transaction is related to a specific trip';
COMMENT ON COLUMN transactions.contract_id IS 'Reference to contract if transaction is related to a specific contract';
COMMENT ON COLUMN transactions.transaction_purpose IS 'Purpose of the transaction: FINANCING, REPAYMENT, PORTAL_FEE, etc.';
COMMENT ON COLUMN transactions.gross_amount IS 'Original amount before platform fee deduction (used for interest calculation)';
COMMENT ON COLUMN transactions.platform_fee_amount IS 'Platform fee deducted from the transfer';
COMMENT ON COLUMN transactions.net_amount IS 'Net amount after platform fee deduction (actual amount transferred)';

-- =====================================================================
-- SECTION 10: TRIP FINANCIALS & CONFIGURATIONS
-- =====================================================================

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

-- Create indexes for trip_financials
CREATE INDEX IF NOT EXISTS idx_trip_financials_trip ON trip_financials(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_financials_contract ON trip_financials(contract_id);
CREATE INDEX IF NOT EXISTS idx_trip_financials_status ON trip_financials(status);
CREATE INDEX IF NOT EXISTS idx_trip_financials_financing_date ON trip_financials(financing_date);

-- Add Comments for trip_financials
COMMENT ON TABLE trip_financials IS 'Tracks original principal amounts and platform fees for accurate interest calculation';
COMMENT ON COLUMN trip_financials.original_principal_amount IS 'Original amount financed (before platform fee) - used for interest calculation';
COMMENT ON COLUMN trip_financials.net_amount_to_transporter IS 'Net amount received by transporter after platform fee deduction';
COMMENT ON COLUMN trip_financials.platform_fee_amount IS 'Platform fee deducted during financing';

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

-- Create indexes for configurations
CREATE INDEX IF NOT EXISTS idx_config_key ON configurations(config_key);
CREATE INDEX IF NOT EXISTS idx_config_active ON configurations(is_active);

-- Add comments for configurations
COMMENT ON TABLE configurations IS 'System configuration settings for portal behavior';
COMMENT ON COLUMN configurations.config_key IS 'Unique key identifier for the configuration';
COMMENT ON COLUMN configurations.config_value IS 'Configuration value stored as text';
COMMENT ON COLUMN configurations.config_type IS 'Data type of the configuration value';
COMMENT ON COLUMN configurations.is_active IS 'Whether this configuration is currently active';

-- =====================================================================
-- SECTION 11: DATA INSERTION - ROLES
-- =====================================================================

-- Insert initial user roles
INSERT INTO user_roles (role_name, description) VALUES
    ('ROLE_LENDER', 'Lender role with access to lending operations'),
    ('ROLE_TRANSPORTER', 'Transporter role with access to transport operations'),
    ('ROLE_TRUST_ACCOUNT', 'Trust account role with access to trust account operations'),
    ('ROLE_CSR', 'Customer Service Representative role'),
    ('ROLE_ADMIN', 'Administrator role with elevated privileges'),
    ('ROLE_SUPER_ADMIN', 'Super Administrator role with full system access'),
    ('ROLE_SHIPPER', 'Load provider/Load Owner role');

-- =====================================================================
-- SECTION 12: DATA INSERTION - USERS
-- =====================================================================

-- Insert sample users
INSERT INTO users (first_name, last_name, email, phone, active) VALUES
    ('Admin', 'User', 'admin@logifin.com', '+1234567890', true),
    ('Test', 'User', 'test@logifin.com', '+0987654321', true);

-- Update existing users with roles and passwords
-- Admin user gets ROLE_SUPER_ADMIN (id=6), Test user gets ROLE_CSR (id=4)
-- Default password is 'admin123' (BCrypt encoded)
UPDATE users SET role_id = 6, password = '$2a$10$4uIJUcewOG8eWRjDKui0iu68zJBw./frLjW1uM7Mq9VNGTIoYYEri' WHERE email = 'admin@logifin.com';
UPDATE users SET role_id = 4, password = '$2a$10$4uIJUcewOG8eWRjDKui0iu68zJBw./frLjW1uM7Mq9VNGTIoYYEri' WHERE email = 'test@logifin.com';

-- Insert Super Admin user: Alok Pancholi
-- Password: faber@123 (BCrypt encoded with strength 10)
INSERT INTO users (first_name, last_name, email, phone, active, password, role_id)
VALUES (
    'alok',
    'pancholi',
    'alok@faberwork.com',
    '1234567890',
    true,
    '$2a$10$EqKcp1WFKVQISheBxkV.qOXQgzfr/nLJoU5kJ/OIJjPz/yw/v1poy',
    6
);

-- =====================================================================
-- SECTION 13: DATA INSERTION - LOAN STAGES
-- =====================================================================

-- Insert master data for loan stages
INSERT INTO loan_stages (stage_name, description, stage_order) VALUES
    ('PENDING', 'Initial stage - contract created but no documents uploaded', 1),
    ('BILTY_UPLOADED', 'Bilty document has been uploaded', 2),
    ('TRUCK_INVOICE_UPLOADED', 'Truck invoice document has been uploaded', 3),
    ('POD_UPLOADED', 'Proof of Delivery (POD) document has been uploaded', 4),
    ('FINAL_INVOICE', 'Final invoice stage - contract completion', 5);

-- =====================================================================
-- SECTION 14: DATA INSERTION - CONTRACT TYPES
-- =====================================================================

-- Insert master data for contract types
INSERT INTO contract_types (type_name, description, party_count) VALUES
    ('SINGLE_PARTY_WITH_LOGIFIN', 'Single party contract with Logifin', 1),
    ('TWO_PARTY_WITH_LOGIFIN', 'Two party contract with Logifin', 2),
    ('THREE_PARTY_WITH_LOGIFIN', 'Three party contract with Logifin', 3),
    ('FOUR_PARTY_WITH_LOGIFIN', 'Four party contract with Logifin', 4),
    ('FIVE_PARTY_WITH_LOGIFIN', 'Five party contract with Logifin', 5);

-- =====================================================================
-- SECTION 15: DATA INSERTION - DOCUMENT TYPES
-- =====================================================================

-- Insert default document types
INSERT INTO document_types (code, display_name, description, is_active, sort_order, created_at, updated_at) VALUES
    ('EWAY_BILL', 'E-Way Bill', 'Electronic Way Bill for goods transportation', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BILTY', 'Bilty', 'Transport receipt / consignment note', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TRUCK_INVOICE', 'Truck Invoice', 'Truck billing invoice for the trip', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('POD', 'Proof of Delivery', 'Document confirming delivery of goods', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FINAL_INVOICE', 'Final Invoice', 'Final billing invoice for the trip', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================================
-- SECTION 16: DATA INSERTION - COMPANIES
-- =====================================================================

-- Insert Jaipur Golden company
INSERT INTO companies (name, display_name, is_active, is_verified)
VALUES (
    'Jaipur Golden',
    'Jaipur Golden',
    true,
    true
);

-- =====================================================================
-- SECTION 17: DATA INSERTION - WALLETS
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

-- Create wallet for Super Admin (alok@faberwork.com)
INSERT INTO wallets (user_id, currency_code, status, created_at, updated_at, version)
SELECT id, 'INR', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM users
WHERE email = 'alok@faberwork.com'
AND NOT EXISTS (
    SELECT 1 FROM wallets WHERE user_id = (SELECT id FROM users WHERE email = 'alok@faberwork.com')
);

-- =====================================================================
-- SECTION 18: DATA INSERTION - CONFIGURATIONS
-- =====================================================================

-- Insert default portal service charge configuration
INSERT INTO configurations (config_key, config_value, config_type, description, is_active)
VALUES
    ('portal_service_charge', '0.5', 'PERCENTAGE', 'Portal service charge percentage deducted from transfers (0.5%)', TRUE),
    ('interest_calculation_method', 'DAILY', 'STRING', 'Method for calculating interest: DAILY, MONTHLY, YEARLY', TRUE),
    ('allow_negative_balance', 'true', 'BOOLEAN', 'Allow wallets to go into negative balance (borrowing)', TRUE);

-- =====================================================================
-- END OF CONSOLIDATED MIGRATION - V1 THROUGH V30
-- =====================================================================
