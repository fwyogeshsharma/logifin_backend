-- V16__normalize_trip_documents.sql
-- Normalize trips table by moving document-related data to trip_documents table
-- Add document_number field for storing eway bill number, bilty number, etc.

-- Step 1: Drop the NOT NULL constraint on document_type if exists (to allow migration)
ALTER TABLE trip_documents ALTER COLUMN document_type DROP NOT NULL;

-- Step 2: Add document_number column to trip_documents for storing reference numbers
ALTER TABLE trip_documents ADD COLUMN IF NOT EXISTS document_number VARCHAR(100);

-- Step 3: Add document_type_id column if it doesn't exist
ALTER TABLE trip_documents ADD COLUMN IF NOT EXISTS document_type_id BIGINT;

-- Step 4: Populate document_type_id from document_type string for existing records
UPDATE trip_documents td
SET document_type_id = dt.id
FROM document_types dt
WHERE td.document_type = dt.code
  AND td.document_type_id IS NULL;

-- Step 5: Migrate existing eway_bill_number from trips to trip_documents
-- Only create if eway_bill_number exists and no EWAY_BILL document exists yet
INSERT INTO trip_documents (
    trip_id,
    document_type_id,
    document_number,
    uploaded_by_user_id,
    created_at,
    updated_at
)
SELECT
    t.id,
    dt.id,
    t.eway_bill_number,
    t.created_by_user_id,
    t.created_at,
    t.updated_at
FROM trips t
CROSS JOIN document_types dt
WHERE t.eway_bill_number IS NOT NULL
  AND dt.code = 'EWAY_BILL'
  AND NOT EXISTS (
    SELECT 1 FROM trip_documents td
    WHERE td.trip_id = t.id
    AND (td.document_type_id = dt.id OR td.document_type = 'EWAY_BILL')
  );

-- Step 6: If there's existing eway_bill_image data, update the document record
UPDATE trip_documents td
SET
    document_data = t.eway_bill_image,
    content_type = COALESCE(t.eway_bill_image_content_type, 'image/png'),
    file_size = COALESCE(LENGTH(t.eway_bill_image), 0)
FROM trips t
JOIN document_types dt ON dt.code = 'EWAY_BILL'
WHERE td.trip_id = t.id
  AND (td.document_type_id = dt.id OR td.document_type = 'EWAY_BILL')
  AND t.eway_bill_image IS NOT NULL;

-- Step 7: Remove redundant columns from trips table
ALTER TABLE trips DROP COLUMN IF EXISTS eway_bill_image;
ALTER TABLE trips DROP COLUMN IF EXISTS eway_bill_image_content_type;
ALTER TABLE trips DROP COLUMN IF EXISTS eway_bill_number;

-- Step 8: Drop the check constraint on old document_type column if exists
ALTER TABLE trip_documents DROP CONSTRAINT IF EXISTS chk_document_type;

-- Step 9: Remove the old document_type varchar column from trip_documents
ALTER TABLE trip_documents DROP COLUMN IF EXISTS document_type;

-- Step 10: Remove unnecessary columns from trip_documents (simplify structure)
ALTER TABLE trip_documents DROP COLUMN IF EXISTS document_name;
ALTER TABLE trip_documents DROP COLUMN IF EXISTS description;

-- Step 11: Add NOT NULL constraint to document_type_id (now that all records should have it)
-- First, delete any orphan records that don't have a valid document_type_id
DELETE FROM trip_documents WHERE document_type_id IS NULL;

-- Now make document_type_id NOT NULL
ALTER TABLE trip_documents ALTER COLUMN document_type_id SET NOT NULL;

-- Step 12: Add foreign key constraint if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_trip_documents_document_type'
        AND table_name = 'trip_documents'
    ) THEN
        ALTER TABLE trip_documents
        ADD CONSTRAINT fk_trip_documents_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_types(id);
    END IF;
END $$;

-- Step 13: Drop old indexes and create new ones
DROP INDEX IF EXISTS idx_trip_document_trip_type;
DROP INDEX IF EXISTS idx_trip_document_trip_type_id;

-- Create unique constraint to allow only one document per type per trip
DROP INDEX IF EXISTS idx_trip_document_unique_type;
CREATE UNIQUE INDEX idx_trip_document_unique_type
ON trip_documents(trip_id, document_type_id);

-- Index for faster document lookups by type
DROP INDEX IF EXISTS idx_trip_document_type_id;
CREATE INDEX idx_trip_document_type_id ON trip_documents(document_type_id);

-- Index for document number searches
DROP INDEX IF EXISTS idx_trip_document_number;
CREATE INDEX idx_trip_document_number ON trip_documents(document_number);

-- Add Comments
COMMENT ON TABLE trip_documents IS 'Stores all documents attached to trips. Each trip can have one document per document type.';
COMMENT ON COLUMN trip_documents.document_number IS 'Reference number like E-way Bill Number, Bilty Number, Invoice Number, etc.';
COMMENT ON COLUMN trip_documents.document_data IS 'Base64 decoded document stored as bytea (optional)';
