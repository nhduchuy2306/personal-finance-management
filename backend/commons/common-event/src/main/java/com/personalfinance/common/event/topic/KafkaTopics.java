package com.personalfinance.common.event.topic;

/**
 * All Kafka topic constants.
 * One place to see the complete event catalog.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    // ── Transaction ──
    public static final String TRANSACTION_CREATED = "transaction.created";
    public static final String TRANSACTION_CONFIRMED = "transaction.confirmed";

    // ── Budget ──
    public static final String BUDGET_WARNING = "budget.warning";
    public static final String BUDGET_CRITICAL = "budget.critical";
    public static final String BUDGET_DRAFT_CREATED = "budget.draft_created";

    // ── Receipt / OCR ──
    public static final String RECEIPT_UPLOADED = "receipt.uploaded";
    public static final String RECEIPT_PARSED = "receipt.parsed";
    public static final String RECEIPT_CONFIRMED = "receipt.confirmed";
    public static final String RECEIPT_CONFIRMED_GROUP = "receipt.confirmed_group";

    // ── Group Expense ──
    public static final String GROUP_EXPENSE_CREATED = "group_expense.created";
    public static final String SETTLEMENT_COMPLETED = "settlement.completed";
    public static final String GROUP_SETTLE_REMINDER = "group_settle.reminder";

    // ── Recurring Bills ──
    public static final String RECURRING_BILL_DUE_SOON = "recurring_bill.due_soon";
    public static final String RECURRING_BILL_OVERDUE = "recurring_bill.overdue";
    public static final String RECURRING_BILL_PAID = "recurring_bill.paid";

    // ── Savings ──
    public static final String SAVING_CONTRIBUTION = "saving.contribution";
    public static final String SAVING_GOAL_COMPLETED = "saving.goal_completed";
    public static final String SAVING_BEHIND_SCHEDULE = "saving.behind_schedule";
    public static final String SAVING_REMINDER = "saving.reminder";
}
