-- EVS Testing Framework - PostgreSQL Initialization
-- This script sets up the test database schema

-- Create test database (if not exists)
CREATE DATABASE IF NOT EXISTS evs_test;

-- Connect to test database
\c evs_test;

-- Create test users table
CREATE TABLE IF NOT EXISTS test_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create test sessions table
CREATE TABLE IF NOT EXISTS test_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES test_users(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create test reports table
CREATE TABLE IF NOT EXISTS test_reports (
    id SERIAL PRIMARY KEY,
    test_name VARCHAR(255) NOT NULL,
    test_suite VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    duration INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test data
INSERT INTO test_users (username, email, password_hash, role) VALUES
('testuser', 'test@example.com', '$2a$10$hashedpassword1', 'user'),
('admin', 'admin@example.com', '$2a$10$hashedpassword2', 'admin'),
('epgu_user', 'epgu@example.com', '$2a$10$hashedpassword3', 'epgu')
ON CONFLICT (username) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_test_users_username ON test_users(username);
CREATE INDEX IF NOT EXISTS idx_test_users_email ON test_users(email);
CREATE INDEX IF NOT EXISTS idx_test_sessions_token ON test_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_test_reports_status ON test_reports(status);
CREATE INDEX IF NOT EXISTS idx_test_reports_created_at ON test_reports(created_at);

-- Grant permissions (for test purposes)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO evs_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO evs_user;