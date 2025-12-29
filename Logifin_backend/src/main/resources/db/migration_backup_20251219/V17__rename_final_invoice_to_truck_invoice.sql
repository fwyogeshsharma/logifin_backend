-- V17: Rename FINAL_INVOICE to TRUCK_INVOICE in document_types table
-- This migration updates the document type code and display name

UPDATE document_types
SET code = 'TRUCK_INVOICE',
    display_name = 'Truck Invoice',
    description = 'Truck billing invoice for the trip',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'FINAL_INVOICE';

-- Add comment for documentation
COMMENT ON COLUMN document_types.code IS 'Unique identifier code: EWAY_BILL, BILTY, ADVANCE_INVOICE, POD, TRUCK_INVOICE';
