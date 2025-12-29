-- V28__insert_jaipur_golden_company.sql
-- Insert initial company: Jaipur Golden

-- Insert Jaipur Golden company
INSERT INTO companies (name, display_name, is_active, is_verified)
VALUES (
    'Jaipur Golden',
    'Jaipur Golden',
    true,
    true
);

-- Add comment
COMMENT ON TABLE companies IS 'Initial company Jaipur Golden added via V28 migration';
