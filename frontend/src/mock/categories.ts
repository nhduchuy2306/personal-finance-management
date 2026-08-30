import {Category} from "@/lib/types";

export const mockCategories: Category[] = [
  {id: "cat-01", userId: null, name: "Ăn uống", icon: "UtensilsCrossed", isActive: true},
  {id: "cat-02", userId: null, name: "Di chuyển", icon: "Car", isActive: true},
  {id: "cat-03", userId: null, name: "Giải trí", icon: "Gamepad2", isActive: true},
  {id: "cat-04", userId: null, name: "Mua sắm", icon: "ShoppingBag", isActive: true},
  {id: "cat-05", userId: null, name: "Sức khỏe", icon: "Heart", isActive: true},
  {id: "cat-06", userId: null, name: "Giáo dục", icon: "GraduationCap", isActive: true},
  {id: "cat-07", userId: null, name: "Du lịch", icon: "Plane", isActive: true},
  {id: "cat-08", userId: null, name: "Tiết kiệm", icon: "PiggyBank", isActive: true},
  {id: "cat-09", userId: null, name: "Hóa đơn", icon: "Receipt", isActive: true},
  {id: "cat-10", userId: null, name: "Khác", icon: "MoreHorizontal", isActive: true},
  {id: "cat-11", userId: null, name: "Lương", icon: "Banknote", isActive: true},
  {id: "cat-12", userId: null, name: "Thưởng", icon: "Gift", isActive: true},
];

export function getCategoryById(id: string): Category | undefined {
  return mockCategories.find((c) => c.id === id);
}

export function getExpenseCategories(): Category[] {
  return mockCategories.filter(
    (c) => !["cat-11", "cat-12"].includes(c.id) && c.isActive
  );
}

export function getIncomeCategories(): Category[] {
  return mockCategories.filter(
    (c) => ["cat-11", "cat-12"].includes(c.id) && c.isActive
  );
}
