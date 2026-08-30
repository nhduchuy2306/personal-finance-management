// App-wide constants

export const APP_NAME = "FinanceFlow";
export const APP_DESCRIPTION = "Quản lý tài chính cá nhân thông minh";

// Navigation items
export const NAV_ITEMS = [
  {label: "Tổng quan", href: "/", icon: "LayoutDashboard" as const},
  {label: "Giao dịch", href: "/transactions", icon: "ArrowLeftRight" as const},
  {label: "Ngân sách", href: "/budget", icon: "Wallet" as const},
  {label: "Nhóm chi", href: "/groups", icon: "Users" as const},
  {label: "Hóa đơn", href: "/receipts", icon: "ScanLine" as const},
  {label: "Thanh toán", href: "/bills", icon: "CalendarClock" as const},
  {label: "Tiết kiệm", href: "/savings", icon: "PiggyBank" as const},
  {label: "Cài đặt", href: "/settings", icon: "Settings" as const},
] as const;

// Mobile bottom nav items (subset of NAV_ITEMS)
export const MOBILE_NAV_ITEMS = [
  {label: "Tổng quan", href: "/", icon: "LayoutDashboard" as const},
  {label: "Giao dịch", href: "/transactions", icon: "ArrowLeftRight" as const},
  {label: "Thêm", href: "#add", icon: "PlusCircle" as const},
  {label: "Ngân sách", href: "/budget", icon: "Wallet" as const},
  {label: "Khác", href: "#more", icon: "Menu" as const},
] as const;

// Transaction types
export const TRANSACTION_TYPES = {
  EXPENSE: "EXPENSE",
  INCOME: "INCOME",
} as const;

// Transaction sources
export const TRANSACTION_SOURCES = {
  MANUAL: "MANUAL",
  OCR: "OCR",
  GROUP_SPLIT: "GROUP_SPLIT",
} as const;

// Budget period statuses
export const BUDGET_STATUSES = {
  DRAFT: "DRAFT",
  ACTIVE: "ACTIVE",
  COMPLETED: "COMPLETED",
} as const;

// Limit types
export const LIMIT_TYPES = {
  DAILY: "DAILY",
  MONTHLY: "MONTHLY",
} as const;

// Split methods
export const SPLIT_METHODS = {
  EQUAL: "EQUAL",
  BY_PERCENTAGE: "BY_PERCENTAGE",
  BY_EXACT_AMOUNT: "BY_EXACT_AMOUNT",
  BY_ITEM: "BY_ITEM",
} as const;

// Cycle types for recurring bills
export const CYCLE_TYPES = {
  MONTHLY: "MONTHLY",
  QUARTERLY: "QUARTERLY",
  SEMI_ANNUAL: "SEMI_ANNUAL",
  ANNUAL: "ANNUAL",
  CUSTOM_DAYS: "CUSTOM_DAYS",
} as const;

// Savings goal statuses
export const SAVINGS_STATUSES = {
  ACTIVE: "ACTIVE",
  COMPLETED: "COMPLETED",
  CANCELLED: "CANCELLED",
} as const;

// Contribution frequencies
export const CONTRIBUTION_FREQUENCIES = {
  DAILY: "DAILY",
  WEEKLY: "WEEKLY",
} as const;

// Receipt statuses
export const RECEIPT_STATUSES = {
  PROCESSING: "PROCESSING",
  PARSED: "PARSED",
  CONFIRMED: "CONFIRMED",
  FAILED: "FAILED",
  DISCARDED: "DISCARDED",
} as const;

// Notification types
export const NOTIFICATION_TYPES = {
  BUDGET_WARNING: "BUDGET_WARNING",
  BUDGET_CRITICAL: "BUDGET_CRITICAL",
  BUDGET_DRAFT_READY: "BUDGET_DRAFT_READY",
  BILL_DUE_SOON: "BILL_DUE_SOON",
  BILL_OVERDUE: "BILL_OVERDUE",
  GROUP_SETTLE_REMIND: "GROUP_SETTLE_REMIND",
  SAVING_REMINDER: "SAVING_REMINDER",
  SAVING_BEHIND: "SAVING_BEHIND",
  SAVING_COMPLETED: "SAVING_COMPLETED",
} as const;
