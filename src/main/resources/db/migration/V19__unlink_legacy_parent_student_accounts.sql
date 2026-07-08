-- Preserve parent/student profiles while making legacy portal users unable to authenticate.
UPDATE users SET status = 'INACTIVE', updated_at = NOW()
WHERE id IN (
    SELECT user_id FROM students WHERE user_id IS NOT NULL
    UNION
    SELECT user_id FROM parents WHERE user_id IS NOT NULL
);

UPDATE students
SET user_id = NULL, access_mode = 'NO_ACCOUNT', updated_at = NOW()
WHERE user_id IS NOT NULL OR access_mode IS DISTINCT FROM 'NO_ACCOUNT';

UPDATE parents
SET user_id = NULL, updated_at = NOW()
WHERE user_id IS NOT NULL;
