"use client";

import {
  Banknote,
  Car,
  Gamepad2,
  Gift,
  GraduationCap,
  Heart,
  type LucideIcon,
  MoreHorizontal,
  PiggyBank,
  Plane,
  Receipt,
  ShoppingBag,
  UtensilsCrossed,
} from "lucide-react";

const iconMap: Record<string, LucideIcon> = {
  UtensilsCrossed,
  Car,
  Gamepad2,
  ShoppingBag,
  Heart,
  GraduationCap,
  Plane,
  PiggyBank,
  Receipt,
  MoreHorizontal,
  Banknote,
  Gift,
};

// Color palette for categories
const categoryColors: Record<string, string> = {
  "cat-01": "bg-orange-500/15 text-orange-600 dark:text-orange-400",
  "cat-02": "bg-blue-500/15 text-blue-600 dark:text-blue-400",
  "cat-03": "bg-purple-500/15 text-purple-600 dark:text-purple-400",
  "cat-04": "bg-pink-500/15 text-pink-600 dark:text-pink-400",
  "cat-05": "bg-red-500/15 text-red-600 dark:text-red-400",
  "cat-06": "bg-indigo-500/15 text-indigo-600 dark:text-indigo-400",
  "cat-07": "bg-cyan-500/15 text-cyan-600 dark:text-cyan-400",
  "cat-08": "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400",
  "cat-09": "bg-amber-500/15 text-amber-600 dark:text-amber-400",
  "cat-10": "bg-gray-500/15 text-gray-600 dark:text-gray-400",
  "cat-11": "bg-green-500/15 text-green-600 dark:text-green-400",
  "cat-12": "bg-yellow-500/15 text-yellow-600 dark:text-yellow-400",
};

interface CategoryIconProps {
  iconName: string;
  categoryId?: string;
  size?: number;
  className?: string;
}

export function CategoryIcon({iconName, categoryId, size = 18, className = ""}: CategoryIconProps) {
  const Icon = iconMap[iconName] || MoreHorizontal;
  const colorClass = categoryId ? (categoryColors[categoryId] || categoryColors["cat-10"]) : "";

  return (
    <div className={`flex items-center justify-center rounded-xl p-2.5 ${colorClass} ${className}`}>
      <Icon size={size}/>
    </div>
  );
}

export function getCategoryColor(categoryId: string): string {
  return categoryColors[categoryId] || categoryColors["cat-10"];
}

// Chart colors for categories
export const chartCategoryColors: Record<string, string> = {
  "cat-01": "#f97316",
  "cat-02": "#3b82f6",
  "cat-03": "#a855f7",
  "cat-04": "#ec4899",
  "cat-05": "#ef4444",
  "cat-06": "#6366f1",
  "cat-07": "#06b6d4",
  "cat-08": "#10b981",
  "cat-09": "#f59e0b",
  "cat-10": "#6b7280",
  "cat-11": "#22c55e",
  "cat-12": "#eab308",
};
