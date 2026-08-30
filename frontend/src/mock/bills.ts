import {RecurringBill} from "@/lib/types";

export const mockBills: RecurringBill[] = [
  {
    id: "bill-001", userId: "user-001", name: "Tiền điện",
    categoryId: "cat-09", cycleType: "MONTHLY", cycleValue: null,
    estimatedAmount: 850000, isFixedAmount: false, dueDayOfCycle: 25,
    nextDueDate: "2026-09-25", reminderDaysBefore: 3,
    isActive: true, createdAt: "2026-01-01T00:00:00+07:00", updatedAt: "2026-08-25T09:00:00+07:00",
  },
  {
    id: "bill-002", userId: "user-001", name: "Tiền nước",
    categoryId: "cat-09", cycleType: "MONTHLY", cycleValue: null,
    estimatedAmount: 350000, isFixedAmount: false, dueDayOfCycle: 20,
    nextDueDate: "2026-09-20", reminderDaysBefore: 3,
    isActive: true, createdAt: "2026-01-01T00:00:00+07:00", updatedAt: "2026-08-20T09:00:00+07:00",
  },
  {
    id: "bill-003", userId: "user-001", name: "Internet FPT",
    categoryId: "cat-09", cycleType: "MONTHLY", cycleValue: null,
    estimatedAmount: 200000, isFixedAmount: true, dueDayOfCycle: 18,
    nextDueDate: "2026-09-18", reminderDaysBefore: 3,
    isActive: true, createdAt: "2026-01-01T00:00:00+07:00", updatedAt: "2026-08-18T10:00:00+07:00",
  },
  {
    id: "bill-004", userId: "user-001", name: "Tiền nhà",
    categoryId: "cat-09", cycleType: "MONTHLY", cycleValue: null,
    estimatedAmount: 5000000, isFixedAmount: true, dueDayOfCycle: 5,
    nextDueDate: "2026-09-05", reminderDaysBefore: 5,
    isActive: true, createdAt: "2026-01-01T00:00:00+07:00", updatedAt: "2026-08-05T08:00:00+07:00",
  },
  {
    id: "bill-005", userId: "user-001", name: "Spotify Premium",
    categoryId: "cat-03", cycleType: "MONTHLY", cycleValue: null,
    estimatedAmount: 59000, isFixedAmount: true, dueDayOfCycle: 15,
    nextDueDate: "2026-09-15", reminderDaysBefore: 2,
    isActive: true, createdAt: "2026-03-01T00:00:00+07:00", updatedAt: "2026-08-15T08:00:00+07:00",
  },
  {
    id: "bill-006", userId: "user-001", name: "Gym membership",
    categoryId: "cat-05", cycleType: "QUARTERLY", cycleValue: null,
    estimatedAmount: 2400000, isFixedAmount: true, dueDayOfCycle: 1,
    nextDueDate: "2026-10-01", reminderDaysBefore: 7,
    isActive: true, createdAt: "2026-01-01T00:00:00+07:00", updatedAt: "2026-07-01T08:00:00+07:00",
  },
];

export function getUpcomingBills(daysAhead: number = 30): RecurringBill[] {
  const today = new Date("2026-08-30");
  const cutoff = new Date(today);
  cutoff.setDate(cutoff.getDate() + daysAhead);

  return mockBills
    .filter((b) => {
      const dueDate = new Date(b.nextDueDate);
      return b.isActive && dueDate >= today && dueDate <= cutoff;
    })
    .sort((a, b) => new Date(a.nextDueDate).getTime() - new Date(b.nextDueDate).getTime());
}

export function getOverdueBills(): RecurringBill[] {
  const today = new Date("2026-08-30");
  return mockBills.filter((b) => {
    const dueDate = new Date(b.nextDueDate);
    return b.isActive && dueDate < today;
  });
}

export function getTotalUpcomingBillsAmount(daysAhead: number = 30): number {
  return getUpcomingBills(daysAhead).reduce((sum, b) => sum + b.estimatedAmount, 0);
}
