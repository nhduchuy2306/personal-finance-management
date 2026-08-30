import {SavingContribution, SavingsGoal} from "@/lib/types";

export const mockSavingsGoals: SavingsGoal[] = [
  {
    id: "sg-001", userId: "user-001",
    name: "iPhone 16 Pro Max",
    targetAmount: 35000000, targetDate: "2027-01-15",
    contributionFrequency: "WEEKLY", currentSavedAmount: 12500000,
    status: "ACTIVE",
    createdAt: "2026-06-01T08:00:00+07:00", updatedAt: "2026-08-28T08:00:00+07:00",
  },
  {
    id: "sg-002", userId: "user-001",
    name: "Du lịch Nhật Bản 🇯🇵",
    targetAmount: 50000000, targetDate: "2027-06-01",
    contributionFrequency: "WEEKLY", currentSavedAmount: 8000000,
    status: "ACTIVE",
    createdAt: "2026-04-01T08:00:00+07:00", updatedAt: "2026-08-25T08:00:00+07:00",
  },
  {
    id: "sg-003", userId: "user-001",
    name: "Quỹ khẩn cấp",
    targetAmount: 30000000, targetDate: "2027-03-01",
    contributionFrequency: "DAILY", currentSavedAmount: 22500000,
    status: "ACTIVE",
    createdAt: "2026-01-01T08:00:00+07:00", updatedAt: "2026-08-29T08:00:00+07:00",
  },
  {
    id: "sg-004", userId: "user-001",
    name: "MacBook Air M3",
    targetAmount: 28000000, targetDate: "2026-07-01",
    contributionFrequency: "WEEKLY", currentSavedAmount: 28000000,
    status: "COMPLETED",
    createdAt: "2026-01-15T08:00:00+07:00", updatedAt: "2026-06-28T08:00:00+07:00",
  },
];

export const mockContributions: SavingContribution[] = [
  {
    id: "sc-001",
    savingsGoalId: "sg-001",
    amount: 500000,
    note: "Tiết kiệm tuần 35",
    contributedAt: "2026-08-28T08:00:00+07:00"
  },
  {
    id: "sc-002",
    savingsGoalId: "sg-001",
    amount: 500000,
    note: "Tiết kiệm tuần 34",
    contributedAt: "2026-08-21T08:00:00+07:00"
  },
  {
    id: "sc-003",
    savingsGoalId: "sg-001",
    amount: 500000,
    note: "Tiết kiệm tuần 33",
    contributedAt: "2026-08-14T08:00:00+07:00"
  },
  {
    id: "sc-004",
    savingsGoalId: "sg-002",
    amount: 500000,
    note: "Japan fund tuần 35",
    contributedAt: "2026-08-25T08:00:00+07:00"
  },
  {
    id: "sc-005",
    savingsGoalId: "sg-003",
    amount: 100000,
    note: "Quỹ khẩn cấp hôm nay",
    contributedAt: "2026-08-29T08:00:00+07:00"
  },
  {
    id: "sc-006",
    savingsGoalId: "sg-003",
    amount: 100000,
    note: "Quỹ khẩn cấp hôm nay",
    contributedAt: "2026-08-28T08:00:00+07:00"
  },
];

export function getActiveSavingsGoals(): SavingsGoal[] {
  return mockSavingsGoals.filter((g) => g.status === "ACTIVE");
}

export function getSavingsProgress(goal: SavingsGoal): {
  percentage: number;
  remainingAmount: number;
  daysLeft: number;
  requiredPerPeriod: number;
  isOnTrack: boolean;
} {
  const percentage = Math.round((goal.currentSavedAmount / goal.targetAmount) * 100);
  const remainingAmount = goal.targetAmount - goal.currentSavedAmount;
  const today = new Date("2026-08-30");
  const target = new Date(goal.targetDate);
  const daysLeft = Math.max(0, Math.ceil((target.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)));

  const periodsLeft = goal.contributionFrequency === "DAILY"
    ? daysLeft
    : Math.ceil(daysLeft / 7);

  const requiredPerPeriod = periodsLeft > 0 ? Math.ceil(remainingAmount / periodsLeft) : remainingAmount;

  // Expected linear progress
  const totalDays = Math.ceil((target.getTime() - new Date(goal.createdAt).getTime()) / (1000 * 60 * 60 * 24));
  const daysElapsed = totalDays - daysLeft;
  const expectedProgress = totalDays > 0 ? (daysElapsed / totalDays) * goal.targetAmount : 0;
  const isOnTrack = goal.currentSavedAmount >= expectedProgress * 0.9; // 10% tolerance

  return {percentage, remainingAmount, daysLeft, requiredPerPeriod, isOnTrack};
}

export function getTotalSaved(): number {
  return getActiveSavingsGoals().reduce((sum, g) => sum + g.currentSavedAmount, 0);
}
