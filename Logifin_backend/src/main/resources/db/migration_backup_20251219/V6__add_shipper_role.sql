-- V6__add_shipper_role.sql
-- Add Shipper role to user_roles table

INSERT INTO user_roles (role_name, description)
VALUES ('ROLE_SHIPPER', 'Load provider/Load Owner role');
