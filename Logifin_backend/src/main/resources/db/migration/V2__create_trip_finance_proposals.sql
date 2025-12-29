-- =====================================================
-- Trip Finance Proposals (Lender Interest in Trips)
-- =====================================================
-- This table stores lender's interest to finance trips
-- Multiple lenders can show interest in the same trip
-- One contract can be used for multiple trips
-- Financial terms come from the referenced contract
-- =====================================================

CREATE TABLE IF NOT EXISTS trip_finance_proposals (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign Keys
    trip_id BIGINT NOT NULL,
    lender_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Timestamps
    proposed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,

    -- Audit Fields
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT fk_tfp_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_tfp_lender FOREIGN KEY (lender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tfp_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_tfp_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_tfp_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,

    -- Unique Constraint: Same lender cannot mark interest twice with same contract for same trip
    CONSTRAINT uq_trip_lender_contract UNIQUE (trip_id, lender_id, contract_id),

    -- Check Constraint: Status must be valid
    CONSTRAINT chk_tfp_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'))
);

-- Indexes for performance
CREATE INDEX idx_tfp_trip_id ON trip_finance_proposals(trip_id);
CREATE INDEX idx_tfp_lender_id ON trip_finance_proposals(lender_id);
CREATE INDEX idx_tfp_contract_id ON trip_finance_proposals(contract_id);
CREATE INDEX idx_tfp_status ON trip_finance_proposals(status);
CREATE INDEX idx_tfp_trip_status ON trip_finance_proposals(trip_id, status);
CREATE INDEX idx_tfp_created_at ON trip_finance_proposals(created_at);

-- Comments
COMMENT ON TABLE trip_finance_proposals IS 'Stores lender interest to finance trips';
COMMENT ON COLUMN trip_finance_proposals.trip_id IS 'Reference to the trip that lender wants to finance';
COMMENT ON COLUMN trip_finance_proposals.lender_id IS 'Reference to the lender (user) showing interest';
COMMENT ON COLUMN trip_finance_proposals.contract_id IS 'Reference to the contract between lender and transporter - contains all financial terms';
COMMENT ON COLUMN trip_finance_proposals.status IS 'Status: PENDING, ACCEPTED, REJECTED, WITHDRAWN';
COMMENT ON COLUMN trip_finance_proposals.proposed_at IS 'When lender marked interest in this trip';
COMMENT ON COLUMN trip_finance_proposals.responded_at IS 'When transporter accepted/rejected this interest';
