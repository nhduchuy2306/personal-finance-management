// TypeScript types matching backend DTOs

export interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  telegramChatId: string | null;
  isActive: boolean;
  createdAt: string;
}

export interface Category {
  id: string;
  userId: string | null; // null = system default
  name: string;
  icon: string;
  isActive: boolean;
}

export interface Transaction {
  id: string;
  userId: string;
  categoryId: string;
  amount: number;
  type: "EXPENSE" | "INCOME";
  note: string;
  transactionDate: string;
  source: "MANUAL" | "OCR" | "GROUP_SPLIT";
  receiptId: string | null;
  recurringBillId: string | null;
  groupExpenseId: string | null;
  noActiveBudget: boolean;
  createdAt: string;
}

export interface BudgetPeriod {
  id: string;
  userId: string;
  startMonth: string;
  endMonth: string;
  totalAmount: number;
  status: "DRAFT" | "ACTIVE" | "COMPLETED";
}

export interface MonthlyBudget {
  id: string;
  budgetPeriodId: string;
  month: string;
  allocatedAmount: number;
}

export interface CategoryAllocation {
  id: string;
  monthlyBudgetId: string;
  categoryId: string;
  limitType: "DAILY" | "MONTHLY";
  limitAmount: number;
}

export interface Receipt {
  id: string;
  userId: string;
  imagePath: string;
  status: "PROCESSING" | "PARSED" | "CONFIRMED" | "FAILED" | "DISCARDED";
  parsedData: {
    items: ReceiptItem[];
    total: number;
    date: string;
  } | null;
  confirmedData: {
    items: ReceiptItem[];
    total: number;
    date: string;
  } | null;
  totalAmount: number | null;
  receiptDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ReceiptItem {
  name: string;
  price: number;
  categoryId: string | null;
}

export interface Group {
  id: string;
  name: string;
  createdBy: string;
  createdAt: string;
  members: GroupMember[];
}

export interface GroupMember {
  id: string;
  groupId: string;
  userId: string | null;
  displayName: string;
  isGhost: boolean;
  createdAt: string;
}

export interface SharedExpense {
  id: string;
  groupId: string;
  paidByMemberId: string;
  totalAmount: number;
  description: string;
  receiptId: string | null;
  splitMethod: "EQUAL" | "BY_PERCENTAGE" | "BY_EXACT_AMOUNT" | "BY_ITEM";
  expenseDate: string;
  createdAt: string;
  splits: ExpenseSplit[];
}

export interface ExpenseSplit {
  id: string;
  sharedExpenseId: string;
  memberId: string;
  amount: number;
  itemName: string | null;
  createdAt: string;
}

export interface Settlement {
  id: string;
  groupId: string;
  fromMemberId: string;
  toMemberId: string;
  amount: number;
  settledAt: string;
}

export interface RecurringBill {
  id: string;
  userId: string;
  name: string;
  categoryId: string;
  cycleType: "MONTHLY" | "QUARTERLY" | "SEMI_ANNUAL" | "ANNUAL" | "CUSTOM_DAYS";
  cycleValue: number | null;
  estimatedAmount: number;
  isFixedAmount: boolean;
  dueDayOfCycle: number;
  nextDueDate: string;
  reminderDaysBefore: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BillPayment {
  id: string;
  recurringBillId: string;
  transactionId: string;
  actualAmount: number;
  paymentDate: string;
  createdAt: string;
}

export interface SavingsGoal {
  id: string;
  userId: string;
  name: string;
  targetAmount: number;
  targetDate: string;
  contributionFrequency: "DAILY" | "WEEKLY";
  currentSavedAmount: number;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  createdAt: string;
  updatedAt: string;
}

export interface SavingContribution {
  id: string;
  savingsGoalId: string;
  amount: number;
  note: string;
  contributedAt: string;
}

export interface NotificationLog {
  id: string;
  userId: string;
  type: string;
  channel: string;
  content: string;
  entityId: string;
  status: "SENT" | "FAILED" | "SKIPPED_NO_TELEGRAM";
  errorMessage: string | null;
  createdAt: string;
}

// UI-specific composite types
export interface TransactionWithCategory extends Transaction {
  category: Category;
}

export interface CategoryBudgetSummary {
  category: Category;
  allocation: CategoryAllocation;
  spent: number;
  remaining: number;
  percentage: number;
}

export interface GroupBalance {
  memberId: string;
  memberName: string;
  balance: number; // positive = is owed, negative = owes
}

export interface SimplifiedDebt {
  from: GroupMember;
  to: GroupMember;
  amount: number;
}

export interface DailySummary {
  date: string;
  totalExpense: number;
  totalIncome: number;
  transactionCount: number;
}

export interface MonthlySummary {
  month: string;
  totalExpense: number;
  totalIncome: number;
  categoryBreakdown: { categoryId: string; amount: number }[];
}
