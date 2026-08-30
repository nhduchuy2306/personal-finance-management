-- Create databases for each microservice (database-per-service pattern)
-- This script runs on first PostgreSQL start via docker-entrypoint-initdb.d

CREATE
DATABASE auth_db;
CREATE
DATABASE budget_db;
CREATE
DATABASE transaction_db;
CREATE
DATABASE ocr_db;
CREATE
DATABASE group_expense_db;
CREATE
DATABASE notification_db;
CREATE
DATABASE recurring_bill_db;
CREATE
DATABASE saving_db;

-- Grant all privileges to the default postgres user
GRANT ALL PRIVILEGES ON DATABASE
auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
budget_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
ocr_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
group_expense_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
notification_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
recurring_bill_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE
saving_db TO postgres;
