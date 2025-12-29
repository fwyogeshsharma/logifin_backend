-- V15: Create trip_bids table for bidding system on trips
-- Lenders can place bids on trips, transporters can accept/reject/counter

CREATE TABLE trip_bids (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign key references
    trip_id BIGINT NOT NULL,
    lender_id BIGINT NOT NULL,          -- user making the bid (must have LENDER role)
    company_id BIGINT NOT NULL,         -- lender's company

    -- Bid details
    bid_amount NUMERIC(12,2) NOT NULL,  -- amount offered
    currency VARCHAR(10) DEFAULT 'INR',
    interest_rate NUMERIC(5,2),         -- proposed interest rate
    maturity_days INTEGER,              -- proposed maturity period

    -- Status tracking
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- Possible values: PENDING, ACCEPTED, REJECTED, CANCELLED, EXPIRED, COUNTERED

    -- Counter offer fields (filled by transporter)
    counter_amount NUMERIC(12,2),       -- optional counter-offer amount
    counter_interest_rate NUMERIC(5,2), -- counter-offer interest rate
    counter_maturity_days INTEGER,      -- counter-offer maturity days
    counter_notes TEXT,                 -- notes for counter offer
    countered_at TIMESTAMP,             -- when counter was made
    countered_by BIGINT,                -- user who made counter offer

    -- Response tracking
    responded_at TIMESTAMP,             -- when bid was accepted/rejected
    responded_by BIGINT,                -- user who accepted/rejected

    -- General fields
    notes TEXT,                         -- general remark from lender
    rejection_reason TEXT,              -- reason if rejected

    -- Expiry
    expires_at TIMESTAMP,               -- when bid expires automatically

    -- Audit fields
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    version BIGINT DEFAULT 0,

    -- Foreign key constraints
    CONSTRAINT fk_trip_bid_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_bid_lender FOREIGN KEY (lender_id) REFERENCES users(id),
    CONSTRAINT fk_trip_bid_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_trip_bid_countered_by FOREIGN KEY (countered_by) REFERENCES users(id),
    CONSTRAINT fk_trip_bid_responded_by FOREIGN KEY (responded_by) REFERENCES users(id),

    -- Constraints
    CONSTRAINT chk_trip_bid_amount_positive CHECK (bid_amount > 0),
    CONSTRAINT chk_trip_bid_counter_amount_positive CHECK (counter_amount IS NULL OR counter_amount > 0),
    CONSTRAINT chk_trip_bid_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'EXPIRED', 'COUNTERED'))
);

-- Indexes for performance
CREATE INDEX idx_trip_bids_trip_id ON trip_bids(trip_id);
CREATE INDEX idx_trip_bids_lender_id ON trip_bids(lender_id);
CREATE INDEX idx_trip_bids_company_id ON trip_bids(company_id);
CREATE INDEX idx_trip_bids_status ON trip_bids(status);
CREATE INDEX idx_trip_bids_created_at ON trip_bids(created_at);
CREATE INDEX idx_trip_bids_expires_at ON trip_bids(expires_at);

-- Composite index for common queries
CREATE INDEX idx_trip_bids_trip_status ON trip_bids(trip_id, status);
CREATE INDEX idx_trip_bids_lender_status ON trip_bids(lender_id, status);

-- Note: ROLE_LENDER already exists from V3__add_user_roles.sql migration

COMMENT ON TABLE trip_bids IS 'Stores bids placed by lenders on trips';
COMMENT ON COLUMN trip_bids.status IS 'PENDING: New bid, ACCEPTED: Bid accepted by transporter, REJECTED: Bid rejected, CANCELLED: Cancelled by lender, EXPIRED: Auto-expired, COUNTERED: Counter offer made';
