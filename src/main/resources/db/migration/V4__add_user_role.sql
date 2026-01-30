-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' NOT NULL;

-- Set admin user
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@rumo.com';
