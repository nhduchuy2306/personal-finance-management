import {Transaction} from "@/lib/types";

export const mockTransactions: Transaction[] = [
  // Today - Aug 30
  {
    id: "txn-001", userId: "user-001", categoryId: "cat-01", amount: 45000,
    type: "EXPENSE", note: "Cà phê sáng Highland", transactionDate: "2026-08-30",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-30T07:30:00+07:00",
  },
  {
    id: "txn-002", userId: "user-001", categoryId: "cat-02", amount: 32000,
    type: "EXPENSE", note: "Grab đi làm", transactionDate: "2026-08-30",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-30T08:15:00+07:00",
  },
  {
    id: "txn-003", userId: "user-001", categoryId: "cat-01", amount: 65000,
    type: "EXPENSE", note: "Cơm trưa văn phòng", transactionDate: "2026-08-30",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-30T12:00:00+07:00",
  },
  // Yesterday - Aug 29
  {
    id: "txn-004", userId: "user-001", categoryId: "cat-01", amount: 85000,
    type: "EXPENSE", note: "Bún bò Huế + nước", transactionDate: "2026-08-29",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-29T12:00:00+07:00",
  },
  {
    id: "txn-005", userId: "user-001", categoryId: "cat-02", amount: 28000,
    type: "EXPENSE", note: "Xe ôm đi chợ", transactionDate: "2026-08-29",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-29T17:30:00+07:00",
  },
  {
    id: "txn-006", userId: "user-001", categoryId: "cat-04", amount: 350000,
    type: "EXPENSE", note: "Áo thun Uniqlo", transactionDate: "2026-08-29",
    source: "OCR", receiptId: "rcp-001", recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-29T19:00:00+07:00",
  },
  {
    id: "txn-007", userId: "user-001", categoryId: "cat-01", amount: 55000,
    type: "EXPENSE", note: "Phở tối", transactionDate: "2026-08-29",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-29T19:30:00+07:00",
  },
  // Aug 28
  {
    id: "txn-008", userId: "user-001", categoryId: "cat-03", amount: 120000,
    type: "EXPENSE", note: "Xem phim CGV", transactionDate: "2026-08-28",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-28T20:00:00+07:00",
  },
  {
    id: "txn-009", userId: "user-001", categoryId: "cat-01", amount: 150000,
    type: "EXPENSE", note: "Nhậu cuối tuần", transactionDate: "2026-08-28",
    source: "GROUP_SPLIT", receiptId: null, recurringBillId: null, groupExpenseId: "ge-001",
    noActiveBudget: false, createdAt: "2026-08-28T22:00:00+07:00",
  },
  // Aug 27
  {
    id: "txn-010", userId: "user-001", categoryId: "cat-05", amount: 200000,
    type: "EXPENSE", note: "Khám bác sĩ da liễu", transactionDate: "2026-08-27",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-27T10:00:00+07:00",
  },
  {
    id: "txn-011", userId: "user-001", categoryId: "cat-01", amount: 40000,
    type: "EXPENSE", note: "Cơm tấm sáng", transactionDate: "2026-08-27",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-27T07:00:00+07:00",
  },
  {
    id: "txn-012", userId: "user-001", categoryId: "cat-02", amount: 55000,
    type: "EXPENSE", note: "Grab về nhà", transactionDate: "2026-08-27",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-27T18:00:00+07:00",
  },
  // Aug 26
  {
    id: "txn-013", userId: "user-001", categoryId: "cat-06", amount: 500000,
    type: "EXPENSE", note: "Khóa học Udemy", transactionDate: "2026-08-26",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-26T21:00:00+07:00",
  },
  {
    id: "txn-014", userId: "user-001", categoryId: "cat-01", amount: 75000,
    type: "EXPENSE", note: "Pizza 4P's delivery", transactionDate: "2026-08-26",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-26T19:00:00+07:00",
  },
  // Aug 25
  {
    id: "txn-015", userId: "user-001", categoryId: "cat-09", amount: 850000,
    type: "EXPENSE", note: "Tiền điện tháng 8", transactionDate: "2026-08-25",
    source: "MANUAL", receiptId: null, recurringBillId: "bill-001", groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-25T09:00:00+07:00",
  },
  // Aug 24
  {
    id: "txn-016", userId: "user-001", categoryId: "cat-01", amount: 95000,
    type: "EXPENSE", note: "Lẩu Thái với bạn", transactionDate: "2026-08-24",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-24T19:30:00+07:00",
  },
  {
    id: "txn-017", userId: "user-001", categoryId: "cat-04", amount: 1200000,
    type: "EXPENSE", note: "Giày Nike Air Max", transactionDate: "2026-08-24",
    source: "OCR", receiptId: "rcp-002", recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-24T15:00:00+07:00",
  },
  // Income entries
  {
    id: "txn-018", userId: "user-001", categoryId: "cat-11", amount: 25000000,
    type: "INCOME", note: "Lương tháng 8", transactionDate: "2026-08-05",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-05T10:00:00+07:00",
  },
  {
    id: "txn-019", userId: "user-001", categoryId: "cat-12", amount: 3000000,
    type: "INCOME", note: "Freelance design", transactionDate: "2026-08-15",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-15T14:00:00+07:00",
  },
  // More Aug transactions
  {
    id: "txn-020", userId: "user-001", categoryId: "cat-01", amount: 38000,
    type: "EXPENSE", note: "Mì Quảng", transactionDate: "2026-08-23",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-23T12:00:00+07:00",
  },
  {
    id: "txn-021", userId: "user-001", categoryId: "cat-02", amount: 22000,
    type: "EXPENSE", note: "Bus đi chợ", transactionDate: "2026-08-23",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-23T10:00:00+07:00",
  },
  {
    id: "txn-022", userId: "user-001", categoryId: "cat-08", amount: 500000,
    type: "EXPENSE", note: "Tiết kiệm mua iPhone", transactionDate: "2026-08-20",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-20T08:00:00+07:00",
  },
  {
    id: "txn-023", userId: "user-001", categoryId: "cat-09", amount: 350000,
    type: "EXPENSE", note: "Tiền nước tháng 8", transactionDate: "2026-08-20",
    source: "MANUAL", receiptId: null, recurringBillId: "bill-002", groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-20T09:00:00+07:00",
  },
  {
    id: "txn-024", userId: "user-001", categoryId: "cat-09", amount: 200000,
    type: "EXPENSE", note: "Internet FPT tháng 8", transactionDate: "2026-08-18",
    source: "MANUAL", receiptId: null, recurringBillId: "bill-003", groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-18T10:00:00+07:00",
  },
  {
    id: "txn-025", userId: "user-001", categoryId: "cat-07", amount: 2500000,
    type: "EXPENSE", note: "Vé máy bay Đà Lạt", transactionDate: "2026-08-10",
    source: "MANUAL", receiptId: null, recurringBillId: null, groupExpenseId: null,
    noActiveBudget: false, createdAt: "2026-08-10T14:00:00+07:00",
  },
];

// Helper functions
export function getTransactionsByDate(date: string): Transaction[] {
  return mockTransactions.filter((t) => t.transactionDate === date);
}

export function getTransactionsByMonth(month: string): Transaction[] {
  return mockTransactions.filter((t) => t.transactionDate.startsWith(month));
}

export function getRecentTransactions(count: number): Transaction[] {
  return [...mockTransactions]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, count);
}

export function getTotalExpenseByMonth(month: string): number {
  return getTransactionsByMonth(month)
    .filter((t) => t.type === "EXPENSE")
    .reduce((sum, t) => sum + t.amount, 0);
}

export function getTotalIncomeByMonth(month: string): number {
  return getTransactionsByMonth(month)
    .filter((t) => t.type === "INCOME")
    .reduce((sum, t) => sum + t.amount, 0);
}

export function getExpenseByCategoryForMonth(month: string): { categoryId: string; amount: number }[] {
  const expenses = getTransactionsByMonth(month).filter((t) => t.type === "EXPENSE");
  const map = new Map<string, number>();
  expenses.forEach((t) => {
    map.set(t.categoryId, (map.get(t.categoryId) || 0) + t.amount);
  });
  return Array.from(map.entries())
    .map(([categoryId, amount]) => ({categoryId, amount}))
    .sort((a, b) => b.amount - a.amount);
}

export function getDailyExpenses(days: number): { date: string; amount: number }[] {
  const result: { date: string; amount: number }[] = [];
  const today = new Date("2026-08-30");
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().split("T")[0];
    const total = mockTransactions
      .filter((t) => t.transactionDate === dateStr && t.type === "EXPENSE")
      .reduce((sum, t) => sum + t.amount, 0);
    result.push({date: dateStr, amount: total});
  }
  return result;
}
