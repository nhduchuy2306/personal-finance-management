"use client";

import {formatCurrency} from "@/lib/format";
import {cn} from "@/lib/utils";

interface CurrencyDisplayProps {
  amount: number;
  type?: "EXPENSE" | "INCOME" | "neutral";
  compact?: boolean;
  className?: string;
  showSign?: boolean;
  size?: "sm" | "md" | "lg" | "xl";
}

const sizeClasses = {
  sm: "text-sm",
  md: "text-base",
  lg: "text-lg font-semibold",
  xl: "text-2xl font-bold",
};

export function CurrencyDisplay({
                                  amount,
                                  type = "neutral",
                                  compact = false,
                                  className,
                                  showSign = false,
                                  size = "md",
                                }: CurrencyDisplayProps) {
  const typeColor =
    type === "EXPENSE"
      ? "text-destructive"
      : type === "INCOME"
        ? "text-success"
        : "";

  const sign = showSign
    ? type === "EXPENSE"
      ? "-"
      : type === "INCOME"
        ? "+"
        : ""
    : "";

  return (
    <span
      className={cn(
        "tabular-nums tracking-tight",
        sizeClasses[size],
        typeColor,
        className
      )}
    >
      {sign}{formatCurrency(amount, compact)}
    </span>
  );
}
