-- V11__create_trips_table.sql
-- Create trips table for trip management system

CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,

    -- E-way Bill Information
    eway_bill_number VARCHAR(50) NOT NULL UNIQUE,
    eway_bill_image BYTEA,
    eway_bill_image_content_type VARCHAR(100),

    -- Route Information
    pickup VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,

    -- Parties Involved
    sender VARCHAR(150) NOT NULL,
    receiver VARCHAR(150) NOT NULL,
    transporter VARCHAR(150) NOT NULL,

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

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Foreign Key Constraints
    CONSTRAINT fk_trip_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_trip_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_loan_amount_positive CHECK (loan_amount > 0),
    CONSTRAINT chk_interest_rate_range CHECK (interest_rate >= 0 AND interest_rate <= 100),
    CONSTRAINT chk_maturity_days_range CHECK (maturity_days >= 1 AND maturity_days <= 365),
    CONSTRAINT chk_distance_non_negative CHECK (distance_km IS NULL OR distance_km >= 0),
    CONSTRAINT chk_weight_non_negative CHECK (weight_kg IS NULL OR weight_kg >= 0)
);

-- Create Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_trip_eway_bill_number ON trips(eway_bill_number);
CREATE INDEX IF NOT EXISTS idx_trip_transporter ON trips(transporter);
CREATE INDEX IF NOT EXISTS idx_trip_created_at ON trips(created_at);
CREATE INDEX IF NOT EXISTS idx_trip_pickup ON trips(pickup);
CREATE INDEX IF NOT EXISTS idx_trip_destination ON trips(destination);
CREATE INDEX IF NOT EXISTS idx_trip_created_by ON trips(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_trip_company ON trips(company_id);
CREATE INDEX IF NOT EXISTS idx_trip_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trip_sender ON trips(sender);
CREATE INDEX IF NOT EXISTS idx_trip_receiver ON trips(receiver);

-- Composite index for common search patterns
CREATE INDEX IF NOT EXISTS idx_trip_transporter_status ON trips(transporter, status);
CREATE INDEX IF NOT EXISTS idx_trip_created_at_status ON trips(created_at DESC, status);

-- Add Comments
COMMENT ON TABLE trips IS 'Stores trip/shipment information including e-way bills and financial terms';
COMMENT ON COLUMN trips.eway_bill_number IS 'Unique E-way Bill Number for the trip';
COMMENT ON COLUMN trips.eway_bill_image IS 'Base64 decoded image stored as bytea';
COMMENT ON COLUMN trips.loan_amount IS 'Loan amount for the trip';
COMMENT ON COLUMN trips.interest_rate IS 'Interest rate percentage (0-100)';
COMMENT ON COLUMN trips.maturity_days IS 'Number of days until maturity (1-365)';
COMMENT ON COLUMN trips.distance_km IS 'Distance of the trip in kilometers';
COMMENT ON COLUMN trips.weight_kg IS 'Weight of cargo in kilograms';
COMMENT ON COLUMN trips.status IS 'Trip status: ACTIVE, IN_TRANSIT, COMPLETED, CANCELLED';
