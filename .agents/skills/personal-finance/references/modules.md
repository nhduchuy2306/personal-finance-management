# Modules Reference — Personal Finance Manager

## Table of Contents
1. [Module 1: User & Account](#1-module-1-user--account)
2. [Module 2: Budget Period](#2-module-2-budget-period)
3. [Module 3: Daily/Monthly Expense](#3-module-3-dailymonthly-expense)
4. [Module 4: OCR Receipt Scanning](#4-module-4-ocr-receipt-scanning)
5. [Module 5: Group Expense Splitting](#5-module-5-group-expense-splitting)
6. [Module 6: Notification](#6-module-6-notification)
7. [Module 7: Recurring Bills](#7-module-7-recurring-bills)
8. [Module 8: Savings Goal](#8-module-8-savings-goal)
9. [Kafka Event Catalog](#9-kafka-event-catalog)
10. [Expense Type Classification](#10-expense-type-classification)

---

## 1. Module 1: User & Account

**Service**: auth-service | **Has Adapter**: YES (auth-adapter)

### Business Rules
- **BR-1.1**: 1 user = 1 account. No sub-accounts. Auth via JWT (access + refresh token).
- **BR-1.2**: Profile: name, email (unique login), password (BCrypt), avatar (optional), telegram_chat_id (optional nullable).
- **BR-1.3**: Telegram link via OTP: /start → Bot sends 6-digit OTP → user enters in app → system saves chat_id. Optional — system works without Telegram.

### Database Tables
- `users` (id, email, password_hash, display_name, avatar_url, telegram_chat_id, is_active, created_at, updated_at)
- `refresh_tokens` (id, user_id, token, expires_at, created_at)

### REST Endpoints
- POST /api/v1/auth/register, /login, /refresh
- GET /api/v1/users/me | PUT /api/v1/users/me
- POST /api/v1/users/me/telegram/link | DELETE /api/v1/users/me/telegram/link

### gRPC (auth-adapter)
- GetUserById, GetUsersByIds, GetUserTelegramChatId

### Redis Keys
- `user:{id}` (profile cache, TTL 30min)
- `telegram:otp:{code}` (OTP, TTL 5min)

---

## 2. Module 2: Budget Period

**Service**: budget-service | **Has Adapter**: YES (budget-adapter)

### Business Rules
- **BR-2.1**: Budget period: start_month, end_month (1-4 months), total_amount. Only 1 ACTIVE period at a time.
- **BR-2.2**: Category allocation per month. limit_type: DAILY or MONTHLY. Total allocations ≤ monthly budget. Allow unallocated buffer.
- **BR-2.3**: Auto-fill draft 3 days before period ends. Copy last allocation. Draft NEVER auto-confirms. Notify via Telegram. Expenses during no-budget period: flagged "no_active_budget", no threshold alerts.
- **BR-2.4**: Large known expenses (plane tickets) — user adjusts category allocation when confirming draft. No separate module needed.
- **BR-2.5**: When creating/auto-filling draft, call recurring-bill-service (gRPC) to list bills due in period. Show as "Fixed & Recurring" section.

### Database Tables
- `budget_periods` (id, user_id, start_month, end_month, total_amount, status [DRAFT/ACTIVE/COMPLETED])
- `monthly_budgets` (id, budget_period_id, month, allocated_amount)
- `category_allocations` (id, monthly_budget_id, category_id, limit_type, limit_amount)
- `categories` (id, user_id [null=system default], name, icon, is_active)

### Default Categories (seed)
Ăn uống (DAILY), Di chuyển (DAILY), Giải trí (MONTHLY), Mua sắm (MONTHLY), Sức khỏe (MONTHLY), Giáo dục (MONTHLY), Du lịch/Đi lại xa (MONTHLY), Tiết kiệm (MONTHLY), Hóa đơn (MONTHLY), Khác (MONTHLY).

### REST Endpoints
- POST /api/v1/budgets | GET /active | GET /draft
- PUT /{id}/confirm | PUT /{id}/allocations | GET /{id}/summary
- CRUD /api/v1/categories

### gRPC (budget-adapter)
- GetActiveBudget, GetCategoryAllocation, GetDailyRemaining, GetMonthlyRemaining

### Kafka Events
- Produces: budget.draft_created
- Consumes: transaction.created (update spending cache)

### Redis Keys
- `budget:active:{userId}` (TTL 1h)
- `budget:daily:{userId}:{categoryId}:{date}` (TTL end of day)
- `budget:monthly:{userId}:{categoryId}:{month}` (TTL 1h)

### Scheduled Jobs
- Daily 00:00: check period expiring in 3 days → create draft → publish budget.draft_created

---

## 3. Module 3: Daily/Monthly Expense

**Service**: transaction-service | **Has Adapter**: YES (transaction-adapter)

### Business Rules
- **BR-3.1**: Transaction: user_id, category_id, amount (BIGINT VNĐ), type (EXPENSE/INCOME), note, date, source (MANUAL/OCR/GROUP_SPLIT). Income recorded separately, does NOT add to budget.
- **BR-3.2**: Remaining calculation — DAILY: daily_limit − day's expenses. MONTHLY: monthly_limit − month-to-date expenses. No "borrowing from tomorrow" — negative balance shown as-is.
- **BR-3.3**: Alert at 80% (warning) and 100% (critical). Real-time via Kafka. Never block transactions.
- **BR-3.4**: Anti-duplicate alerts: Redis flag per user+category+alertType+date. No re-send same type same day. Exception: new milestones (150%, 200%).
- **BR-3.5**: Transactions without active budget: allowed, flagged "no_active_budget", no threshold alerts.

### Database Tables
- `transactions` (id, user_id, category_id, amount, type, note, transaction_date, source, receipt_id, recurring_bill_id, group_expense_id, no_active_budget, created_at)
- Indexes: (user_id, transaction_date), (user_id, category_id, transaction_date)

### REST Endpoints
- CRUD /api/v1/transactions (with filters: date, category, type, pagination)
- GET /summary/daily?date= | /summary/monthly?month=
- GET /remaining/daily?date= | /remaining/monthly?month=

### Kafka Events
- Produces: transaction.created, budget.warning, budget.critical
- Consumes: transaction.confirmed (from OCR), settlement.completed (from group expense)

### Business Logic Flow (create EXPENSE)
1. Save transaction to DB
2. INCRBY Redis spending cache
3. gRPC call → get category allocation (limit_type + limit_amount)
4. Calculate percentage = current_spending / limit * 100
5. Check Redis alert dedup flag
6. If ≥80% and no warning sent → publish budget.warning
7. If ≥100% and no critical sent → publish budget.critical
8. Publish transaction.created

### Redis Keys
- `spending:daily:{userId}:{categoryId}:{date}` | `spending:monthly:{userId}:{categoryId}:{month}`
- `alert:sent:{userId}:{categoryId}:{type}:{date}` (TTL end of day)

---

## 4. Module 4: OCR Receipt Scanning

**Service**: ocr-service | **Has Adapter**: NO

### Business Rules
- **BR-4.1**: Upload image → call OCR/LLM Vision API → return parsed items. Provider: Google Cloud Vision or LLM Vision (strategy pattern, configurable).
- **BR-4.2**: Result always DRAFT. User MUST confirm/edit before saving. Can: edit item, merge items, delete wrong items, add missing items, discard entire receipt.
- **BR-4.3**: Store original image (MinIO). Link receipt to transactions for audit trail.
- **BR-4.4**: OCR fail → allow manual input. Never block main flow.
- **BR-4.5**: On confirm, user can choose "split as group expense" → routes to Module 5 instead.

### Database Tables
- `receipts` (id, user_id, image_path, status [PROCESSING/PARSED/CONFIRMED/FAILED/DISCARDED], parsed_data JSONB, confirmed_data JSONB, total_amount, receipt_date, created_at, updated_at)

### parsed_data JSON format
```json
{ "items": [{"name": "Thịt bò", "price": 85000, "category_id": null}], "total": 100000, "date": "2026-08-30" }
```

### REST Endpoints
- POST /api/v1/receipts/upload (multipart)
- GET /{id} | PUT /{id}/edit | POST /{id}/confirm | POST /{id}/confirm-group | POST /{id}/discard

### Kafka Events
- Produces: receipt.uploaded, receipt.parsed, receipt.confirmed, receipt.confirmed_group
- Consumes: receipt.uploaded (self-consume for async OCR processing)

---

## 5. Module 5: Group Expense Splitting

**Service**: group-expense-service | **Has Adapter**: YES (group-expense-adapter)

### Business Rules
- **BR-5.1**: Groups with members (real users or "ghost members" — name only, no account needed). Groups are independent.
- **BR-5.2**: Split methods: EQUAL / BY_PERCENTAGE / BY_EXACT_AMOUNT / BY_ITEM. Total splits must equal total exactly. Round-off → added to payer.
- **BR-5.3**: Debt as net balance between pairs. Debt simplification algorithm: calculate net balance → greedy match largest debtor with largest creditor.
- **BR-5.4**: Settlement creates separate record. Never modifies original shared_expense (audit trail).
- **BR-5.5**: paid_by = user → auto count as personal expense (trừ budget). User owes others (unpaid) → shown as "debt", NOT deducted from budget until settled.

### Database Tables
- `groups` (id, name, created_by, created_at)
- `group_members` (id, group_id, user_id [nullable], display_name, is_ghost, created_at)
- `shared_expenses` (id, group_id, paid_by_member_id, total_amount, description, receipt_id, split_method, expense_date, created_at)
- `expense_splits` (id, shared_expense_id, member_id, amount, item_name, created_at)
- `settlements` (id, group_id, from_member_id, to_member_id, amount, settled_at)

### REST Endpoints
- POST /api/v1/groups | GET /groups | POST /{id}/members
- POST /{id}/expenses | GET /{id}/balances | GET /{id}/simplified-debts | POST /{id}/settlements

### Kafka Events
- Produces: group_expense.created, settlement.completed
- Consumes: receipt.confirmed_group

---

## 6. Module 6: Notification

**Service**: notification-service | **Has Adapter**: NO

### Business Rules
- **BR-6.1**: Telegram Bot only (HTTP API). Token via config.
- **BR-6.2**: Types: BUDGET_WARNING, BUDGET_CRITICAL, BUDGET_DRAFT_READY, BILL_DUE_SOON, BILL_OVERDUE, GROUP_SETTLE_REMIND, SAVING_REMINDER, SAVING_BEHIND, SAVING_COMPLETED.
- **BR-6.3**: Anti-spam: no duplicate type+entity_id same day (Redis flag). Exception: new milestones.
- **BR-6.4**: No Telegram linked → log as SKIPPED_NO_TELEGRAM. API fail → retry 2x, then log FAILED.

### Database Tables
- `notification_logs` (id, user_id, type, channel, content, entity_id, status [SENT/FAILED/SKIPPED_NO_TELEGRAM], error_message, created_at)

### Kafka Events
- Consumes ALL: budget.warning, budget.critical, budget.draft_created, recurring_bill.due_soon, recurring_bill.overdue, group_settle.reminder, saving.reminder, saving.behind_schedule, saving.goal_completed

### Redis Keys
- `notif:sent:{userId}:{type}:{entityId}:{date}` (TTL end of day)

---

## 7. Module 7: Recurring Bills

**Service**: recurring-bill-service | **Has Adapter**: YES (recurring-bill-adapter)

### Business Rules
- **BR-7.1**: Bill: name, category, cycle_type (MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/CUSTOM_DAYS), estimated_amount, is_fixed_amount, due_day_of_cycle, next_due_date.
- **BR-7.2**: Bills appear in budget ONLY in the month of next_due_date. NO pre-allocation for long-cycle bills.
- **BR-7.3**: Remind 3 days before due (configurable per bill). If overdue, remind daily.
- **BR-7.4**: After payment: calculate next_due_date. If variable bill and actual differs >20% from estimated → suggest update (average of last 3 payments). Never auto-change.
- **BR-7.5**: next_due_date calc: MONTHLY +1mo, QUARTERLY +3mo, SEMI_ANNUAL +6mo, ANNUAL +12mo, CUSTOM +N days.

### Database Tables
- `recurring_bills` (id, user_id, name, category_id, cycle_type, cycle_value, estimated_amount, is_fixed_amount, due_day_of_cycle, next_due_date, reminder_days_before, is_active, created_at, updated_at)
- `bill_payments` (id, recurring_bill_id, transaction_id, actual_amount, payment_date, created_at)

### REST Endpoints
- CRUD /api/v1/recurring-bills
- POST /{id}/pay | GET /upcoming?months=1

### gRPC (recurring-bill-adapter)
- GetBillsDueInPeriod, GetBillEstimateAdjustment

### Kafka Events
- Produces: recurring_bill.due_soon, recurring_bill.overdue, recurring_bill.paid

### Scheduled Jobs
- Daily 08:00: check due_soon and overdue → publish events

---

## 8. Module 8: Savings Goal

**Service**: saving-service | **Has Adapter**: NO

### Business Rules
- **BR-8.1**: Goal: name, target_amount, target_date, contribution_frequency (DAILY/WEEKLY), current_saved_amount (default 0), status (ACTIVE/COMPLETED/CANCELLED). Multiple goals allowed.
- **BR-8.2**: required_contribution = (target − current) / remaining periods. Auto-recalculate on any change.
- **BR-8.3**: Contribution creates saving_contribution record. Also publishes event → transaction-service creates EXPENSE in "Tiết kiệm" category. User confirms each contribution manually.
- **BR-8.4**: current_saved ≥ target → COMPLETED, publish saving.goal_completed.
- **BR-8.5**: Behind schedule: daily check, if current < expected linear progress → publish saving.behind_schedule.

### Database Tables
- `savings_goals` (id, user_id, name, target_amount, target_date, contribution_frequency, current_saved_amount, status, created_at, updated_at)
- `saving_contributions` (id, savings_goal_id, amount, note, contributed_at)

### REST Endpoints
- POST /api/v1/savings | GET / | GET /{id} | PUT /{id} | POST /{id}/contribute | POST /{id}/cancel

### Kafka Events
- Produces: saving.contribution, saving.goal_completed, saving.behind_schedule, saving.reminder

### Scheduled Jobs
- Daily 08:00: DAILY goals → publish saving.reminder
- Weekly Monday 08:00: WEEKLY goals → publish saving.reminder
- Daily 09:00: check behind schedule → publish saving.behind_schedule

---

## 9. Kafka Event Catalog

| Event | Producer | Consumer(s) | Commit Mode |
|-------|----------|-------------|-------------|
| transaction.created | transaction-service | budget-service | MANUAL |
| transaction.confirmed | ocr-service | transaction-service | MANUAL |
| budget.warning | transaction-service | notification-service | AUTO |
| budget.critical | transaction-service | notification-service | AUTO |
| budget.draft_created | budget-service | notification-service | AUTO |
| receipt.uploaded | ocr-service | ocr-service (self) | MANUAL |
| receipt.parsed | ocr-service | (UI polling) | — |
| receipt.confirmed | ocr-service | transaction-service | MANUAL |
| receipt.confirmed_group | ocr-service | group-expense-service | MANUAL |
| group_expense.created | group-expense-service | transaction-service | MANUAL |
| settlement.completed | group-expense-service | transaction-service, notification-service | MANUAL |
| group_settle.reminder | scheduled-job | notification-service | AUTO |
| recurring_bill.due_soon | scheduled-job | notification-service | AUTO |
| recurring_bill.overdue | scheduled-job | notification-service | AUTO |
| recurring_bill.paid | transaction-service | recurring-bill-service | MANUAL |
| saving.contribution | saving-service | transaction-service | MANUAL |
| saving.goal_completed | saving-service | notification-service | AUTO |
| saving.behind_schedule | scheduled-job | notification-service | AUTO |
| saving.reminder | scheduled-job | notification-service | AUTO |

---

## 10. Expense Type Classification

| Type | Example | Module | When deducted from budget |
|------|---------|--------|--------------------------|
| Daily variable expense | food, transport, coffee | Module 3 | Immediately on creation |
| Fixed recurring bill | electricity, water, internet | Module 7 | Added to Fixed & Recurring in due month |
| Large one-off (known ahead) | train/plane tickets, events | Module 2+3 | On creation, after user adjusts budget allocation via draft confirm |
| Savings goal contribution | gift, iPhone, travel fund | Module 8 | Each time user confirms a contribution |