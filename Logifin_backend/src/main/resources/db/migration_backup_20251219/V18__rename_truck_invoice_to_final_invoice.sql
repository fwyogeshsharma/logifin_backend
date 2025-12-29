-- V18: Rename TRUCK_INVOICE back to FINAL_INVOICE in document_types table
-- This migration updates the document type code and display name

UPDATE document_types
SET code = 'FINAL_INVOICE',
    display_name = 'Final Invoice',
    description = 'Final billing invoice for the trip',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'TRUCK_INVOICE';

-- Add comment for documentation
COMMENT ON COLUMN document_types.code IS 'Unique identifier code: EWAY_BILL, BILTY, ADVANCE_INVOICE, POD, FINAL_INVOICE';
