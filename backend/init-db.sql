-- FinanceFlow Database Initialization Script
-- This script runs when PostgreSQL container starts for the first time

-- Create schemas for each service (optional - can use single schema)
-- CREATE SCHEMA IF NOT EXISTS auth;
-- CREATE SCHEMA IF NOT EXISTS accounts;
-- CREATE SCHEMA IF NOT EXISTS transactions;
-- CREATE SCHEMA IF NOT EXISTS analytics;

-- =====================
-- USERS TABLE (Auth Service)
-- =====================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- =====================
-- REFRESH TOKENS TABLE (Auth Service)
-- =====================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT false
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- =====================
-- ACCOUNTS TABLE (Account Service)
-- =====================
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT')),
    account_name VARCHAR(100),
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_user ON accounts(user_id);
CREATE INDEX idx_accounts_number ON accounts(account_number);

-- =====================
-- TRANSACTIONS TABLE (Transaction Service)
-- =====================
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')),
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    category VARCHAR(50),
    description VARCHAR(500),
    recipient_account_id UUID REFERENCES accounts(id),
    reference_number VARCHAR(50) UNIQUE,
    status VARCHAR(20) DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    idempotency_key VARCHAR(64) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(created_at);
CREATE INDEX idx_transactions_category ON transactions(category);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);

-- =====================
-- CATEGORIES TABLE (for transaction categorization)
-- =====================
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(50),
    color VARCHAR(7),
    is_system BOOLEAN DEFAULT false
);

-- Insert default categories
INSERT INTO categories (name, icon, color, is_system) VALUES
    ('Food & Dining', 'restaurant', '#FF6B6B', true),
    ('Transportation', 'directions_car', '#4ECDC4', true),
    ('Shopping', 'shopping_cart', '#45B7D1', true),
    ('Entertainment', 'movie', '#96CEB4', true),
    ('Bills & Utilities', 'receipt', '#FFEAA7', true),
    ('Healthcare', 'local_hospital', '#DDA0DD', true),
    ('Education', 'school', '#98D8C8', true),
    ('Travel', 'flight', '#F7DC6F', true),
    ('Income', 'attach_money', '#82E0AA', true),
    ('Transfer', 'swap_horiz', '#85C1E9', true),
    ('Other', 'more_horiz', '#D5DBDB', true)
ON CONFLICT (name) DO NOTHING;

-- =====================
-- SEED DATA: Demo Users
-- =====================
-- Password: password123 (BCrypt hash with cost 10)
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, is_active, email_verified) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'john.doe@example.com', '$2a$10$d.igkx5jHgBqcENcqN2mmeoSQHoKQwTpQkA5RNbyhFExTyFn5d4Hq', 'John', 'Doe', '+1-555-0101', true, true),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'jane.smith@example.com', '$2a$10$d.igkx5jHgBqcENcqN2mmeoSQHoKQwTpQkA5RNbyhFExTyFn5d4Hq', 'Jane', 'Smith', '+1-555-0102', true, true),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'demo.user@example.com', '$2a$10$d.igkx5jHgBqcENcqN2mmeoSQHoKQwTpQkA5RNbyhFExTyFn5d4Hq', 'Demo', 'User', '+1-555-0103', true, true)
ON CONFLICT (email) DO NOTHING;

-- =====================
-- SEED DATA: Demo Accounts
-- =====================
INSERT INTO accounts (id, user_id, account_number, account_type, account_name, balance, currency) VALUES
    -- John Doe's accounts
    ('11111111-1111-1111-1111-111111111111', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'CHK-001-0001', 'CHECKING', 'Primary Checking', 5420.50, 'USD'),
    ('22222222-2222-2222-2222-222222222222', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'SAV-001-0001', 'SAVINGS', 'Emergency Fund', 15000.00, 'USD'),
    ('33333333-3333-3333-3333-333333333333', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'CRD-001-0001', 'CREDIT', 'Rewards Credit', -1250.75, 'USD'),
    -- Jane Smith's accounts
    ('44444444-4444-4444-4444-444444444444', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'CHK-002-0001', 'CHECKING', 'Main Checking', 8750.25, 'USD'),
    ('55555555-5555-5555-5555-555555555555', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'SAV-002-0001', 'SAVINGS', 'Vacation Fund', 3500.00, 'USD'),
    -- Demo User's accounts
    ('66666666-6666-6666-6666-666666666666', 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'CHK-003-0001', 'CHECKING', 'Checking', 2500.00, 'USD')
ON CONFLICT (account_number) DO NOTHING;

-- =====================
-- SEED DATA: Demo Transactions (last 6 months)
-- =====================
-- Helper function to generate dates
CREATE OR REPLACE FUNCTION random_date(start_date DATE, end_date DATE)
RETURNS TIMESTAMP AS $$
BEGIN
    RETURN start_date + (random() * (end_date - start_date))::INTEGER + (random() * INTERVAL '23 hours 59 minutes');
END;
$$ LANGUAGE plpgsql;

-- John Doe's transactions (Checking Account)
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, category, description, reference_number, created_at) VALUES
    -- Income
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 3500.00, 'Income', 'Salary Deposit - Tech Corp', 'REF-001', NOW() - INTERVAL '180 days'),
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 7000.00, 'Income', 'Salary Deposit - Tech Corp', 'REF-002', NOW() - INTERVAL '150 days'),
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 8200.00, 'Income', 'Salary Deposit - Tech Corp', 'REF-003', NOW() - INTERVAL '120 days'),
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 9100.00, 'Income', 'Salary Deposit - Tech Corp', 'REF-004', NOW() - INTERVAL '90 days'),
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 7420.50, 'Income', 'Salary Deposit - Tech Corp', 'REF-005', NOW() - INTERVAL '60 days'),
    ('11111111-1111-1111-1111-111111111111', 'DEPOSIT', 3500.00, 8920.50, 'Income', 'Salary Deposit - Tech Corp', 'REF-006', NOW() - INTERVAL '30 days'),
    
    -- Food & Dining
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 45.50, 6954.50, 'Food & Dining', 'Whole Foods Market', 'REF-101', NOW() - INTERVAL '145 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 32.00, 8168.00, 'Food & Dining', 'Chipotle Mexican Grill', 'REF-102', NOW() - INTERVAL '115 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 78.90, 9021.10, 'Food & Dining', 'Trader Joes', 'REF-103', NOW() - INTERVAL '85 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 25.00, 7395.50, 'Food & Dining', 'Starbucks', 'REF-104', NOW() - INTERVAL '55 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 120.00, 8800.50, 'Food & Dining', 'Restaurant Week Dinner', 'REF-105', NOW() - INTERVAL '25 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 55.00, 5865.50, 'Food & Dining', 'DoorDash Order', 'REF-106', NOW() - INTERVAL '5 days'),
    
    -- Transportation
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 50.00, 6904.50, 'Transportation', 'Shell Gas Station', 'REF-201', NOW() - INTERVAL '140 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 127.00, 8041.00, 'Transportation', 'Monthly Metro Pass', 'REF-202', NOW() - INTERVAL '110 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 45.00, 8976.10, 'Transportation', 'Uber Rides', 'REF-203', NOW() - INTERVAL '80 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 55.00, 7340.50, 'Transportation', 'Shell Gas Station', 'REF-204', NOW() - INTERVAL '50 days'),
    
    -- Shopping
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 250.00, 6654.50, 'Shopping', 'Amazon.com', 'REF-301', NOW() - INTERVAL '135 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 89.99, 7951.01, 'Shopping', 'Target', 'REF-302', NOW() - INTERVAL '105 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 199.00, 8777.10, 'Shopping', 'Best Buy - Electronics', 'REF-303', NOW() - INTERVAL '75 days'),
    
    -- Entertainment
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 15.99, 6638.51, 'Entertainment', 'Netflix Subscription', 'REF-401', NOW() - INTERVAL '130 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 15.99, 7935.02, 'Entertainment', 'Netflix Subscription', 'REF-402', NOW() - INTERVAL '100 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 65.00, 8712.10, 'Entertainment', 'Concert Tickets', 'REF-403', NOW() - INTERVAL '70 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 15.99, 7324.51, 'Entertainment', 'Netflix Subscription', 'REF-404', NOW() - INTERVAL '40 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 15.99, 8784.51, 'Entertainment', 'Netflix Subscription', 'REF-405', NOW() - INTERVAL '10 days'),
    
    -- Bills & Utilities
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 150.00, 6488.51, 'Bills & Utilities', 'Electric Bill', 'REF-501', NOW() - INTERVAL '125 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 85.00, 7850.02, 'Bills & Utilities', 'Internet Bill', 'REF-502', NOW() - INTERVAL '95 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 145.00, 8567.10, 'Bills & Utilities', 'Electric Bill', 'REF-503', NOW() - INTERVAL '65 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 85.00, 7239.51, 'Bills & Utilities', 'Internet Bill', 'REF-504', NOW() - INTERVAL '35 days'),
    
    -- Transfers to Savings
    ('11111111-1111-1111-1111-111111111111', 'TRANSFER_OUT', 500.00, 5988.51, 'Transfer', 'Transfer to Savings', 'REF-601', NOW() - INTERVAL '120 days'),
    ('11111111-1111-1111-1111-111111111111', 'TRANSFER_OUT', 500.00, 7350.02, 'Transfer', 'Transfer to Savings', 'REF-602', NOW() - INTERVAL '90 days'),
    ('11111111-1111-1111-1111-111111111111', 'TRANSFER_OUT', 500.00, 8067.10, 'Transfer', 'Transfer to Savings', 'REF-603', NOW() - INTERVAL '60 days'),
    
    -- Healthcare
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 150.00, 5838.51, 'Healthcare', 'CVS Pharmacy', 'REF-701', NOW() - INTERVAL '118 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 75.00, 7275.02, 'Healthcare', 'Doctor Visit Copay', 'REF-702', NOW() - INTERVAL '88 days'),
    
    -- Recent transactions
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 200.00, 5665.50, 'Shopping', 'Amazon.com', 'REF-801', NOW() - INTERVAL '3 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 89.00, 5576.50, 'Food & Dining', 'Costco', 'REF-802', NOW() - INTERVAL '2 days'),
    ('11111111-1111-1111-1111-111111111111', 'WITHDRAWAL', 156.00, 5420.50, 'Bills & Utilities', 'Electric Bill', 'REF-803', NOW() - INTERVAL '1 day')
ON CONFLICT (reference_number) DO NOTHING;

-- John Doe's Savings Account transactions
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, category, description, reference_number, recipient_account_id, created_at) VALUES
    ('22222222-2222-2222-2222-222222222222', 'DEPOSIT', 10000.00, 10000.00, 'Transfer', 'Initial Deposit', 'REF-S001', NULL, NOW() - INTERVAL '200 days'),
    ('22222222-2222-2222-2222-222222222222', 'TRANSFER_IN', 500.00, 10500.00, 'Transfer', 'Transfer from Checking', 'REF-S002', '11111111-1111-1111-1111-111111111111', NOW() - INTERVAL '120 days'),
    ('22222222-2222-2222-2222-222222222222', 'DEPOSIT', 2000.00, 12500.00, 'Income', 'Tax Refund', 'REF-S003', NULL, NOW() - INTERVAL '100 days'),
    ('22222222-2222-2222-2222-222222222222', 'TRANSFER_IN', 500.00, 13000.00, 'Transfer', 'Transfer from Checking', 'REF-S004', '11111111-1111-1111-1111-111111111111', NOW() - INTERVAL '90 days'),
    ('22222222-2222-2222-2222-222222222222', 'TRANSFER_IN', 500.00, 13500.00, 'Transfer', 'Transfer from Checking', 'REF-S005', '11111111-1111-1111-1111-111111111111', NOW() - INTERVAL '60 days'),
    ('22222222-2222-2222-2222-222222222222', 'DEPOSIT', 1500.00, 15000.00, 'Income', 'Birthday Gift', 'REF-S006', NULL, NOW() - INTERVAL '30 days')
ON CONFLICT (reference_number) DO NOTHING;

-- Jane Smith's transactions
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, category, description, reference_number, created_at) VALUES
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 4500.00, 4500.00, 'Income', 'Salary Deposit', 'REF-J001', NOW() - INTERVAL '150 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 200.00, 4300.00, 'Shopping', 'Nordstrom', 'REF-J002', NOW() - INTERVAL '145 days'),
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 4500.00, 8800.00, 'Income', 'Salary Deposit', 'REF-J003', NOW() - INTERVAL '120 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 350.00, 8450.00, 'Travel', 'Flight Tickets', 'REF-J004', NOW() - INTERVAL '90 days'),
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 4500.00, 12950.00, 'Income', 'Salary Deposit', 'REF-J005', NOW() - INTERVAL '90 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 1500.00, 11450.00, 'Travel', 'Hotel Booking', 'REF-J006', NOW() - INTERVAL '60 days'),
    ('44444444-4444-4444-4444-444444444444', 'TRANSFER_OUT', 2000.00, 9450.00, 'Transfer', 'Transfer to Vacation Fund', 'REF-J007', NOW() - INTERVAL '45 days'),
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 4500.00, 13950.00, 'Income', 'Salary Deposit', 'REF-J008', NOW() - INTERVAL '60 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 450.00, 13500.00, 'Food & Dining', 'Birthday Dinner', 'REF-J009', NOW() - INTERVAL '30 days'),
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 4500.00, 18000.00, 'Income', 'Salary Deposit', 'REF-J010', NOW() - INTERVAL '30 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 250.00, 17750.00, 'Shopping', 'Amazon', 'REF-J011', NOW() - INTERVAL '15 days'),
    ('44444444-4444-4444-4444-444444444444', 'TRANSFER_OUT', 1500.00, 16250.00, 'Transfer', 'Transfer to Vacation Fund', 'REF-J012', NOW() - INTERVAL '10 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 500.00, 15750.00, 'Entertainment', 'Concert VIP', 'REF-J013', NOW() - INTERVAL '5 days'),
    ('44444444-4444-4444-4444-444444444444', 'DEPOSIT', 3000.00, 18750.00, 'Income', 'Bonus', 'REF-J014', NOW() - INTERVAL '3 days'),
    ('44444444-4444-4444-4444-444444444444', 'WITHDRAWAL', 10000.00, 8750.25, 'Other', 'Investment Transfer', 'REF-J015', NOW() - INTERVAL '1 day')
ON CONFLICT (reference_number) DO NOTHING;

-- Jane's Vacation Fund
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, category, description, reference_number, created_at) VALUES
    ('55555555-5555-5555-5555-555555555555', 'TRANSFER_IN', 2000.00, 2000.00, 'Transfer', 'From Checking', 'REF-JV01', NOW() - INTERVAL '45 days'),
    ('55555555-5555-5555-5555-555555555555', 'TRANSFER_IN', 1500.00, 3500.00, 'Transfer', 'From Checking', 'REF-JV02', NOW() - INTERVAL '10 days')
ON CONFLICT (reference_number) DO NOTHING;

-- Clean up helper function
DROP FUNCTION IF EXISTS random_date(DATE, DATE);

-- Grant permissions (if needed for specific users)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO financeflow;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO financeflow;
