import {Group, GroupBalance, Settlement, SharedExpense, SimplifiedDebt} from "@/lib/types";

export const mockGroups: Group[] = [
  {
    id: "grp-001",
    name: "Nhóm đi ăn văn phòng",
    createdBy: "user-001",
    createdAt: "2026-06-01T10:00:00+07:00",
    members: [
      {
        id: "gm-001",
        groupId: "grp-001",
        userId: "user-001",
        displayName: "Đức Huỳnh",
        isGhost: false,
        createdAt: "2026-06-01T10:00:00+07:00"
      },
      {
        id: "gm-002",
        groupId: "grp-001",
        userId: "user-002",
        displayName: "Minh Trần",
        isGhost: false,
        createdAt: "2026-06-01T10:00:00+07:00"
      },
      {
        id: "gm-003",
        groupId: "grp-001",
        userId: null,
        displayName: "Hoa Nguyễn",
        isGhost: true,
        createdAt: "2026-06-01T10:00:00+07:00"
      },
      {
        id: "gm-004",
        groupId: "grp-001",
        userId: null,
        displayName: "Tuấn Lê",
        isGhost: true,
        createdAt: "2026-06-01T10:00:00+07:00"
      },
    ],
  },
  {
    id: "grp-002",
    name: "Đi Đà Lạt T9/2026",
    createdBy: "user-001",
    createdAt: "2026-08-01T10:00:00+07:00",
    members: [
      {
        id: "gm-005",
        groupId: "grp-002",
        userId: "user-001",
        displayName: "Đức Huỳnh",
        isGhost: false,
        createdAt: "2026-08-01T10:00:00+07:00"
      },
      {
        id: "gm-006",
        groupId: "grp-002",
        userId: "user-002",
        displayName: "Minh Trần",
        isGhost: false,
        createdAt: "2026-08-01T10:00:00+07:00"
      },
      {
        id: "gm-007",
        groupId: "grp-002",
        userId: null,
        displayName: "Lan Phạm",
        isGhost: true,
        createdAt: "2026-08-01T10:00:00+07:00"
      },
    ],
  },
  {
    id: "grp-003",
    name: "Tiền nhà ở ghép",
    createdBy: "user-001",
    createdAt: "2026-01-15T10:00:00+07:00",
    members: [
      {
        id: "gm-008",
        groupId: "grp-003",
        userId: "user-001",
        displayName: "Đức Huỳnh",
        isGhost: false,
        createdAt: "2026-01-15T10:00:00+07:00"
      },
      {
        id: "gm-009",
        groupId: "grp-003",
        userId: null,
        displayName: "Khoa Võ",
        isGhost: true,
        createdAt: "2026-01-15T10:00:00+07:00"
      },
    ],
  },
];

export const mockSharedExpenses: SharedExpense[] = [
  {
    id: "se-001", groupId: "grp-001", paidByMemberId: "gm-001",
    totalAmount: 600000, description: "Lẩu Haidilao", receiptId: null,
    splitMethod: "EQUAL", expenseDate: "2026-08-28",
    createdAt: "2026-08-28T20:00:00+07:00",
    splits: [
      {
        id: "es-001",
        sharedExpenseId: "se-001",
        memberId: "gm-001",
        amount: 150000,
        itemName: null,
        createdAt: "2026-08-28T20:00:00+07:00"
      },
      {
        id: "es-002",
        sharedExpenseId: "se-001",
        memberId: "gm-002",
        amount: 150000,
        itemName: null,
        createdAt: "2026-08-28T20:00:00+07:00"
      },
      {
        id: "es-003",
        sharedExpenseId: "se-001",
        memberId: "gm-003",
        amount: 150000,
        itemName: null,
        createdAt: "2026-08-28T20:00:00+07:00"
      },
      {
        id: "es-004",
        sharedExpenseId: "se-001",
        memberId: "gm-004",
        amount: 150000,
        itemName: null,
        createdAt: "2026-08-28T20:00:00+07:00"
      },
    ],
  },
  {
    id: "se-002", groupId: "grp-001", paidByMemberId: "gm-002",
    totalAmount: 240000, description: "Trà sữa Phúc Long", receiptId: null,
    splitMethod: "EQUAL", expenseDate: "2026-08-25",
    createdAt: "2026-08-25T15:00:00+07:00",
    splits: [
      {
        id: "es-005",
        sharedExpenseId: "se-002",
        memberId: "gm-001",
        amount: 60000,
        itemName: null,
        createdAt: "2026-08-25T15:00:00+07:00"
      },
      {
        id: "es-006",
        sharedExpenseId: "se-002",
        memberId: "gm-002",
        amount: 60000,
        itemName: null,
        createdAt: "2026-08-25T15:00:00+07:00"
      },
      {
        id: "es-007",
        sharedExpenseId: "se-002",
        memberId: "gm-003",
        amount: 60000,
        itemName: null,
        createdAt: "2026-08-25T15:00:00+07:00"
      },
      {
        id: "es-008",
        sharedExpenseId: "se-002",
        memberId: "gm-004",
        amount: 60000,
        itemName: null,
        createdAt: "2026-08-25T15:00:00+07:00"
      },
    ],
  },
  {
    id: "se-003", groupId: "grp-002", paidByMemberId: "gm-005",
    totalAmount: 7500000, description: "Vé máy bay Đà Lạt (3 người)", receiptId: null,
    splitMethod: "EQUAL", expenseDate: "2026-08-10",
    createdAt: "2026-08-10T14:00:00+07:00",
    splits: [
      {
        id: "es-009",
        sharedExpenseId: "se-003",
        memberId: "gm-005",
        amount: 2500000,
        itemName: null,
        createdAt: "2026-08-10T14:00:00+07:00"
      },
      {
        id: "es-010",
        sharedExpenseId: "se-003",
        memberId: "gm-006",
        amount: 2500000,
        itemName: null,
        createdAt: "2026-08-10T14:00:00+07:00"
      },
      {
        id: "es-011",
        sharedExpenseId: "se-003",
        memberId: "gm-007",
        amount: 2500000,
        itemName: null,
        createdAt: "2026-08-10T14:00:00+07:00"
      },
    ],
  },
];

export const mockSettlements: Settlement[] = [
  {
    id: "stl-001",
    groupId: "grp-001",
    fromMemberId: "gm-003",
    toMemberId: "gm-001",
    amount: 100000,
    settledAt: "2026-08-29T10:00:00+07:00"
  },
];

// Computed helpers
export function getGroupBalances(groupId: string): GroupBalance[] {
  const group = mockGroups.find((g) => g.id === groupId);
  if (!group) return [];

  const expenses = mockSharedExpenses.filter((e) => e.groupId === groupId);
  const settlements = mockSettlements.filter((s) => s.groupId === groupId);

  const balanceMap = new Map<string, number>();
  group.members.forEach((m) => balanceMap.set(m.id, 0));

  // Process expenses
  expenses.forEach((expense) => {
    const payer = expense.paidByMemberId;
    balanceMap.set(payer, (balanceMap.get(payer) || 0) + expense.totalAmount);
    expense.splits.forEach((split) => {
      balanceMap.set(split.memberId, (balanceMap.get(split.memberId) || 0) - split.amount);
    });
  });

  // Process settlements
  settlements.forEach((s) => {
    balanceMap.set(s.fromMemberId, (balanceMap.get(s.fromMemberId) || 0) + s.amount);
    balanceMap.set(s.toMemberId, (balanceMap.get(s.toMemberId) || 0) - s.amount);
  });

  return group.members.map((m) => ({
    memberId: m.id,
    memberName: m.displayName,
    balance: balanceMap.get(m.id) || 0,
  }));
}

export function getSimplifiedDebts(groupId: string): SimplifiedDebt[] {
  const group = mockGroups.find((g) => g.id === groupId);
  if (!group) return [];

  const balances = getGroupBalances(groupId);
  const debtors = balances.filter((b) => b.balance < 0).map((b) => ({...b}));
  const creditors = balances.filter((b) => b.balance > 0).map((b) => ({...b}));

  debtors.sort((a, b) => a.balance - b.balance);
  creditors.sort((a, b) => b.balance - a.balance);

  const debts: SimplifiedDebt[] = [];

  let i = 0, j = 0;
  while (i < debtors.length && j < creditors.length) {
    const amount = Math.min(-debtors[i].balance, creditors[j].balance);
    if (amount > 0) {
      debts.push({
        from: group.members.find((m) => m.id === debtors[i].memberId)!,
        to: group.members.find((m) => m.id === creditors[j].memberId)!,
        amount,
      });
    }
    debtors[i].balance += amount;
    creditors[j].balance -= amount;
    if (debtors[i].balance === 0) i++;
    if (creditors[j].balance === 0) j++;
  }

  return debts;
}
