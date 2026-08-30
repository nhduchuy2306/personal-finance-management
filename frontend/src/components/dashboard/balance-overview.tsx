"use client";

import {Minus, TrendingDown, TrendingUp} from "lucide-react";
import {Card, CardContent} from "@/components/ui/card";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getTotalExpenseByMonth, getTotalIncomeByMonth} from "@/mock/transactions";

export function BalanceOverview() {
  const currentMonth = "2026-08";
  const prevMonth = "2026-07";

  const expense = getTotalExpenseByMonth(currentMonth);
  const income = getTotalIncomeByMonth(currentMonth);
  const balance = income - expense;

  // Mock previous month for comparison
  const prevExpense = 6800000;
  const expenseDiff = expense - prevExpense;
  const expenseDiffPercent = prevExpense > 0 ? Math.round((expenseDiff / prevExpense) * 100) : 0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 md:gap-4">
      {/* Total Balance Card */}
      <Card className="sm:col-span-1 overflow-hidden border-0 shadow-md card-hover">
        <div className="gradient-animated p-5 md:p-6 text-white">
          <p className="text-sm font-medium text-white/80 mb-1">Số dư tháng này</p>
          <CurrencyDisplay
            amount={balance}
            size="xl"
            className="text-white animate-count-in block"
          />
          <div className="flex items-center gap-1.5 mt-2 text-white/70 text-xs">
            <span>Thu nhập - Chi tiêu</span>
          </div>
        </div>
      </Card>

      {/* Income Card */}
      <Card className="border-0 shadow-sm card-hover bg-card">
        <CardContent className="p-5 md:p-6">
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm font-medium text-muted-foreground">Thu nhập</p>
            <div className="h-8 w-8 rounded-xl bg-success/15 flex items-center justify-center">
              <TrendingUp className="h-4 w-4 text-success"/>
            </div>
          </div>
          <CurrencyDisplay
            amount={income}
            type="INCOME"
            size="lg"
            className="block animate-count-in"
          />
          <p className="text-xs text-muted-foreground mt-1">
            Tháng 8/2026
          </p>
        </CardContent>
      </Card>

      {/* Expense Card */}
      <Card className="border-0 shadow-sm card-hover bg-card">
        <CardContent className="p-5 md:p-6">
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm font-medium text-muted-foreground">Chi tiêu</p>
            <div className="h-8 w-8 rounded-xl bg-destructive/15 flex items-center justify-center">
              <TrendingDown className="h-4 w-4 text-destructive"/>
            </div>
          </div>
          <CurrencyDisplay
            amount={expense}
            type="EXPENSE"
            size="lg"
            className="block animate-count-in"
          />
          <div className="flex items-center gap-1 mt-1">
            {expenseDiff > 0 ? (
              <TrendingUp className="h-3 w-3 text-destructive"/>
            ) : expenseDiff < 0 ? (
              <TrendingDown className="h-3 w-3 text-success"/>
            ) : (
              <Minus className="h-3 w-3 text-muted-foreground"/>
            )}
            <span className={`text-xs ${expenseDiff > 0 ? "text-destructive" : "text-success"}`}>
              {expenseDiffPercent > 0 ? "+" : ""}{expenseDiffPercent}% so với tháng trước
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
