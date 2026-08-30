import {BudgetPeriod, CategoryAllocation, CategoryBudgetSummary, MonthlyBudget} from "@/lib/types";
import {mockCategories} from "./categories";
import {getTransactionsByMonth} from "./transactions";

export const mockBudgetPeriod: BudgetPeriod = {
  id: "bp-001",
  userId: "user-001",
  startMonth: "2026-08",
  endMonth: "2026-09",
  totalAmount: 15000000,
  status: "ACTIVE",
};

export const mockDraftBudgetPeriod: BudgetPeriod = {
  id: "bp-002",
  userId: "user-001",
  startMonth: "2026-10",
  endMonth: "2026-11",
  totalAmount: 15000000,
  status: "DRAFT",
};

export const mockMonthlyBudgets: MonthlyBudget[] = [
  {id: "mb-001", budgetPeriodId: "bp-001", month: "2026-08", allocatedAmount: 7500000},
  {id: "mb-002", budgetPeriodId: "bp-001", month: "2026-09", allocatedAmount: 7500000},
];

export const mockCategoryAllocations: CategoryAllocation[] = [
  // August allocations
  {id: "ca-001", monthlyBudgetId: "mb-001", categoryId: "cat-01", limitType: "DAILY", limitAmount: 120000},
  {id: "ca-002", monthlyBudgetId: "mb-001", categoryId: "cat-02", limitType: "DAILY", limitAmount: 60000},
  {id: "ca-003", monthlyBudgetId: "mb-001", categoryId: "cat-03", limitType: "MONTHLY", limitAmount: 500000},
  {id: "ca-004", monthlyBudgetId: "mb-001", categoryId: "cat-04", limitType: "MONTHLY", limitAmount: 1500000},
  {id: "ca-005", monthlyBudgetId: "mb-001", categoryId: "cat-05", limitType: "MONTHLY", limitAmount: 500000},
  {id: "ca-006", monthlyBudgetId: "mb-001", categoryId: "cat-06", limitType: "MONTHLY", limitAmount: 500000},
  {id: "ca-007", monthlyBudgetId: "mb-001", categoryId: "cat-07", limitType: "MONTHLY", limitAmount: 3000000},
  {id: "ca-008", monthlyBudgetId: "mb-001", categoryId: "cat-08", limitType: "MONTHLY", limitAmount: 500000},
  {id: "ca-009", monthlyBudgetId: "mb-001", categoryId: "cat-09", limitType: "MONTHLY", limitAmount: 1500000},
  {id: "ca-010", monthlyBudgetId: "mb-001", categoryId: "cat-10", limitType: "MONTHLY", limitAmount: 300000},
];

// Helper: get category budget summary for a month
export function getCategoryBudgetSummaries(month: string): CategoryBudgetSummary[] {
  const transactions = getTransactionsByMonth(month).filter((t) => t.type === "EXPENSE");
  const monthlyBudget = mockMonthlyBudgets.find((mb) => mb.month === month);
  if (!monthlyBudget) return [];

  const allocations = mockCategoryAllocations.filter(
    (ca) => ca.monthlyBudgetId === monthlyBudget.id
  );

  return allocations.map((allocation) => {
    const category = mockCategories.find((c) => c.id === allocation.categoryId)!;
    const spent = transactions
      .filter((t) => t.categoryId === allocation.categoryId)
      .reduce((sum, t) => sum + t.amount, 0);

    const limit = allocation.limitType === "DAILY"
      ? allocation.limitAmount * 30 // approximate monthly total
      : allocation.limitAmount;

    return {
      category,
      allocation,
      spent,
      remaining: limit - spent,
      percentage: limit > 0 ? Math.round((spent / limit) * 100) : 0,
    };
  }).sort((a, b) => b.percentage - a.percentage);
}

export function getTotalBudgetSpent(month: string): number {
  return getTransactionsByMonth(month)
    .filter((t) => t.type === "EXPENSE")
    .reduce((sum, t) => sum + t.amount, 0);
}

export function getDailyRemaining(categoryId: string, date: string): {
  limit: number;
  spent: number;
  remaining: number;
} {
  const allocation = mockCategoryAllocations.find(
    (ca) => ca.categoryId === categoryId && ca.limitType === "DAILY"
  );
  if (!allocation) return {limit: 0, spent: 0, remaining: 0};

  const {getTransactionsByDate} = require("./transactions");
  const dayTransactions = getTransactionsByDate(date);
  const spent = dayTransactions
    .filter((t: { categoryId: string; type: string }) => t.categoryId === categoryId && t.type === "EXPENSE")
    .reduce((sum: number, t: { amount: number }) => sum + t.amount, 0);

  return {
    limit: allocation.limitAmount,
    spent,
    remaining: allocation.limitAmount - spent,
  };
}
