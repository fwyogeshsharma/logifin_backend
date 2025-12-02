-- V5__add_super_admin_alok.sql
-- Add Super Admin user: Alok Pancholi

-- Insert Super Admin user
-- Password: faber@123 (BCrypt encoded with strength 10)
-- BCrypt hash generated for 'faber@123'
INSERT INTO users (first_name, last_name, email, phone, active, password, role_id)
VALUES (
    'alok',
    'pancholi',
    'alok@faberwork.com',
    '1234567890',
    true,
    '$2a$10$EqKcp1WFKVQISheBxkV.qOXQgzfr/nLJoU5kJ/OIJjPz/yw/v1poy',
    6
);

-- Add comment
COMMENT ON TABLE users IS 'Super Admin alok@faberwork.com added via V5 migration';
