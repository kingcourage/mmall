-- Alter mmall_user table to support longer password hashes
ALTER TABLE mmall_user MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT 'password';