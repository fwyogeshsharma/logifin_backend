-- V14__create_document_types_table.sql
-- Create document_types master table and update trip_documents to reference it

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

-- Create Index
CREATE INDEX IF NOT EXISTS idx_document_type_code ON document_types(code);
CREATE INDEX IF NOT EXISTS idx_document_type_active ON document_types(is_active);

-- Insert default document types
INSERT INTO document_types (code, display_name, description, is_active, sort_order, created_at, updated_at) VALUES
    ('EWAY_BILL', 'E-Way Bill', 'Electronic Way Bill for goods transportation', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BILTY', 'Bilty', 'Transport receipt / consignment note', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ADVANCE_INVOICE', 'Advance Invoice', 'Invoice for advance payment', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('POD', 'Proof of Delivery', 'Document confirming delivery of goods', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FINAL_INVOICE', 'Final Invoice', 'Final billing invoice for the trip', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Add document_type_id column to trip_documents table
ALTER TABLE trip_documents ADD COLUMN IF NOT EXISTS document_type_id BIGINT;

-- Update existing records to reference document_types table
UPDATE trip_documents td
SET document_type_id = dt.id
FROM document_types dt
WHERE td.document_type = dt.code
AND td.document_type_id IS NULL;

-- Add foreign key constraint
ALTER TABLE trip_documents
ADD CONSTRAINT fk_trip_document_document_type
FOREIGN KEY (document_type_id) REFERENCES document_types(id) ON DELETE RESTRICT;

-- Create index for the new foreign key
CREATE INDEX IF NOT EXISTS idx_trip_document_document_type_id ON trip_documents(document_type_id);

-- Update unique constraint to use document_type_id instead of document_type
DROP INDEX IF EXISTS idx_trip_document_trip_type;
CREATE UNIQUE INDEX idx_trip_document_trip_type_id ON trip_documents(trip_id, document_type_id);

-- Add Comments
COMMENT ON TABLE document_types IS 'Master table for document types used in trip documents';
COMMENT ON COLUMN document_types.code IS 'Unique identifier code: EWAY_BILL, BILTY, ADVANCE_INVOICE, POD, FINAL_INVOICE';
COMMENT ON COLUMN document_types.display_name IS 'Human readable name for the document type';
COMMENT ON COLUMN document_types.sort_order IS 'Order for display purposes';
