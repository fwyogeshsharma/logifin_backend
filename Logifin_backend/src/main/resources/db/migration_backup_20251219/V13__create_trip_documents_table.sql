-- V13__create_trip_documents_table.sql
-- Create trip_documents table for storing multiple document types per trip

CREATE TABLE IF NOT EXISTS trip_documents (
    id BIGSERIAL PRIMARY KEY,

    -- Trip Reference
    trip_id BIGINT NOT NULL,

    -- Document Information
    document_type VARCHAR(30) NOT NULL,
    document_name VARCHAR(255),
    document_data BYTEA,
    content_type VARCHAR(100),
    file_size BIGINT,
    description VARCHAR(500),

    -- Audit Fields
    uploaded_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT fk_trip_document_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_document_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_document_type CHECK (document_type IN ('EWAY_BILL', 'BILTY', 'ADVANCE_INVOICE', 'POD', 'FINAL_INVOICE'))
);

-- Create Indexes
CREATE INDEX IF NOT EXISTS idx_trip_document_trip_id ON trip_documents(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_document_type ON trip_documents(document_type);
CREATE INDEX IF NOT EXISTS idx_trip_document_trip_type ON trip_documents(trip_id, document_type);
CREATE INDEX IF NOT EXISTS idx_trip_document_uploaded_by ON trip_documents(uploaded_by_user_id);

-- Add Comments
COMMENT ON TABLE trip_documents IS 'Stores documents attached to trips (E-Way Bill, Bilty, Invoices, POD)';
COMMENT ON COLUMN trip_documents.document_type IS 'Type: EWAY_BILL, BILTY, ADVANCE_INVOICE, POD, FINAL_INVOICE';
COMMENT ON COLUMN trip_documents.document_data IS 'Base64 decoded document stored as bytea';
COMMENT ON COLUMN trip_documents.content_type IS 'MIME type of the document (e.g., image/png, application/pdf)';
