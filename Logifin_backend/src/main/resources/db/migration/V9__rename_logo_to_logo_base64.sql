-- V9__rename_logo_to_logo_base64.sql
-- Rename logo column to logo_base64 and change type to TEXT for Base64 storage

-- Rename the column from logo to logo_base64
ALTER TABLE companies RENAME COLUMN logo TO logo_base64;

-- Change the column type from VARCHAR(500) to TEXT to accommodate Base64 encoded images
ALTER TABLE companies ALTER COLUMN logo_base64 TYPE TEXT;

-- Update the comment
COMMENT ON COLUMN companies.logo_base64 IS 'Base64 encoded company logo image';
