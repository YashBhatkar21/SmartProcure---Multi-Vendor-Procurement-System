ALTER TABLE tokens MODIFY token VARCHAR(512);
DELETE FROM tokens WHERE user_id IN (SELECT id FROM users WHERE email='testuser@example.com');
DELETE FROM users WHERE email='testuser@example.com';
