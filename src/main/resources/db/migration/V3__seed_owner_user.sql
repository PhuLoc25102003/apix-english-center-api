-- V3__seed_owner_user.sql
-- Seed the first OWNER user for APIX English Center
-- 
-- Local development credentials:
-- Email: owner@apixenglish.com
-- Password: Admin@123456

-- 1. Insert the OWNER user
INSERT INTO users (email, password_hash, full_name, status, email_verified)
VALUES (
    'owner@apixenglish.com',
    '$2a$10$j2Zg2ixM1bZRazZSjY4tneGasecQWZm114aiWB5K.QjUibti5g9l2',
    'APIX Owner',
    'ACTIVE',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- 2. Assign the OWNER role to this user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'owner@apixenglish.com'
  AND r.code = 'OWNER'
ON CONFLICT (user_id, role_id) DO NOTHING;
