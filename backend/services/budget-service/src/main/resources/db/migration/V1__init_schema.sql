-- Budget Service Schema
-- Tables: budget_periods, monthly_budgets, category_allocations, categories

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID,  -- NULL = system default category
    name        VARCHAR(100) NOT NULL,
    icon        VARCHAR(50),
    is_active   BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE budget_periods (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    start_month     VARCHAR(7) NOT NULL,  -- yyyy-MM
    end_month       VARCHAR(7) NOT NULL,  -- yyyy-MM
    total_amount    BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, ACTIVE, COMPLETED
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_budget_status CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED'))
);

CREATE TABLE monthly_budgets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_period_id    UUID NOT NULL REFERENCES budget_periods(id) ON DELETE CASCADE,
    month               VARCHAR(7) NOT NULL,  -- yyyy-MM
    allocated_amount    BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

CREATE TABLE category_allocations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monthly_budget_id   UUID NOT NULL REFERENCES monthly_budgets(id) ON DELETE CASCADE,
    category_id         UUID NOT NULL REFERENCES categories(id),
    limit_type          VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',  -- DAILY or MONTHLY
    limit_amount        BIGINT NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_limit_type CHECK (limit_type IN ('DAILY', 'MONTHLY'))
);

-- Indexes
CREATE INDEX idx_budget_periods_user ON budget_periods(user_id);
CREATE INDEX idx_budget_periods_status ON budget_periods(user_id, status);
CREATE INDEX idx_monthly_budgets_period ON monthly_budgets(budget_period_id);
CREATE INDEX idx_category_allocations_budget ON category_allocations(monthly_budget_id);
CREATE INDEX idx_categories_user ON categories(user_id);

-- Seed default system categories
INSERT INTO categories (id, user_id, name, icon, is_active) VALUES
    (gen_random_uuid(), NULL, 'Ăn uống', '🍜', true),
    (gen_random_uuid(), NULL, 'Di chuyển', '🚗', true),
    (gen_random_uuid(), NULL, 'Giải trí', '🎬', true),
    (gen_random_uuid(), NULL, 'Mua sắm', '🛒', true),
    (gen_random_uuid(), NULL, 'Sức khỏe', '🏥', true),
    (gen_random_uuid(), NULL, 'Giáo dục', '📚', true),
    (gen_random_uuid(), NULL, 'Du lịch/Đi lại xa', '✈️', true),
    (gen_random_uuid(), NULL, 'Tiết kiệm', '💰', true),
    (gen_random_uuid(), NULL, 'Hóa đơn', '📄', true),
    (gen_random_uuid(), NULL, 'Khác', '📦', true);
