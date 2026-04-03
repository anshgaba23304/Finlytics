-- Finlytics — PostgreSQL (schema finlytics; JPA maps tables here, not public.*)
-- Run this in: Supabase Dashboard → SQL Editor → New query → Run
--
CREATE SCHEMA IF NOT EXISTS finlytics;
--
-- Matches JPA entities:
--   com.example.finlytics.domain.User        → users
--   com.example.finlytics.domain.FinancialRecord → financial_records
--
-- Enum strings (VARCHAR):
--   users.role: VIEWER | ANALYST | ADMIN
--   financial_records.type: INCOME | EXPENSE

-- Optional: ensure extensions Supabase often has enabled already
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS finlytics.users (
                                               id              BIGSERIAL PRIMARY KEY,
                                               username        VARCHAR(80)  NOT NULL,
    email           VARCHAR(160) NOT NULL,
    password_hash   VARCHAR(200) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('VIEWER', 'ANALYST', 'ADMIN'))
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON finlytics.users (email);

COMMENT ON TABLE finlytics.users IS 'Application login users (not Supabase Auth users).';
COMMENT ON COLUMN finlytics.users.password_hash IS 'BCrypt hash from Spring Security.';

-- ---------------------------------------------------------------------------
-- financial_records
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS finlytics.financial_records (
                                                           id           BIGSERIAL PRIMARY KEY,
                                                           amount       NUMERIC(19, 4) NOT NULL,
    type         VARCHAR(20)    NOT NULL,
    category     VARCHAR(120)   NOT NULL,
    record_date  DATE           NOT NULL,
    notes        VARCHAR(2000),
    deleted      BOOLEAN          NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_financial_records_type CHECK (type IN ('INCOME', 'EXPENSE'))
    );

CREATE INDEX IF NOT EXISTS idx_financial_records_record_date
    ON finlytics.financial_records (record_date);

CREATE INDEX IF NOT EXISTS idx_financial_records_deleted
    ON finlytics.financial_records (deleted);

CREATE INDEX IF NOT EXISTS idx_financial_records_category
    ON finlytics.financial_records (category);

COMMENT ON COLUMN finlytics.financial_records.deleted IS 'Soft delete; aggregates exclude deleted = true.';
