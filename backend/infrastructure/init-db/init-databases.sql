-- Create shared database for all microservices
-- All services use a single database (personal_finance_db)
-- This script runs on first PostgreSQL start via docker-entrypoint-initdb.d

CREATE DATABASE personal_finance_db;

-- Grant all privileges to the default postgres user
GRANT ALL PRIVILEGES ON DATABASE personal_finance_db TO postgres;
