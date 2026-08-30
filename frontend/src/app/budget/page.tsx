"use client";

import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Progress} from "@/components/ui/progress";
import {AlertTriangle, CalendarDays, CheckCircle2, Clock, FileEdit} from "lucide-react";
import {CategoryIcon} from "@/components/shared/category-icon";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getCategoryBudgetSummaries, getTotalBudgetSpent, mockBudgetPeriod, mockDraftBudgetPeriod} from "@/mock/budget";
import {calcPercentage, formatCurrency, formatMonth} from "@/lib/format";
import {cn} from "@/lib/utils";

export default function BudgetPage() {
  const period = mockBudgetPeriod;
  const totalSpent = getTotalBudgetSpent("2026-08");
  const budgetUsed = calcPercentage(totalSpent, period.totalAmount);
  const summaries = getCategoryBudgetSummaries("2026-08");

  // Daily remaining for DAILY categories
  const dailyCategories = summaries.filter((s) => s.allocation.limitType === "DAILY");

  return (
    <>
      <Header title="Ngân sách" subtitle="Quản lý ngân sách chi tiêu"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in">
        {/* Draft banner */}
        {mockDraftBudgetPeriod && (
          <Card className="border-amber-500/30 bg-amber-500/5 shadow-sm">
            <CardContent className="p-4 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-amber-500/15 flex items-center justify-center shrink-0">
                <FileEdit className="h-5 w-5 text-amber-600 dark:text-amber-400"/>
              </div>
              <div className="flex-1">
                <p className="text-sm font-semibold text-foreground">Có bản nháp ngân sách mới</p>
                <p className="text-xs text-muted-foreground">
                  {formatMonth(mockDraftBudgetPeriod.startMonth)} — {formatMonth(mockDraftBudgetPeriod.endMonth)}
                </p>
              </div>
              <Button size="sm" className="rounded-xl gradient-primary border-0 text-white">
                Xem nháp
              </Button>
            </CardContent>
          </Card>
        )}

        {/* Active period overview */}
        <Card className="border-0 shadow-md overflow-hidden">
          <div className="gradient-primary p-5 md:p-6 text-white">
            <div className="flex items-center justify-between mb-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Badge className="bg-white/20 text-white border-0 text-[10px]">
                    <CheckCircle2 className="h-3 w-3 mr-1"/> ACTIVE
                  </Badge>
                </div>
                <h2 className="text-lg font-bold">
                  {formatMonth(period.startMonth)} — {formatMonth(period.endMonth)}
                </h2>
              </div>
              <div className="text-right">
                <p className="text-sm text-white/70">Tổng ngân sách</p>
                <CurrencyDisplay amount={period.totalAmount} className="text-white text-xl font-bold"/>
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between text-sm text-white/80">
                <span>Đã chi: {formatCurrency(totalSpent, true)}</span>
                <span>{budgetUsed}%</span>
              </div>
              <div className="h-3 bg-white/20 rounded-full overflow-hidden">
                <div
                  className={cn(
                    "h-full rounded-full transition-all duration-1000",
                    budgetUsed >= 100 ? "bg-red-400" : budgetUsed >= 80 ? "bg-amber-400" : "bg-white"
                  )}
                  style={{width: `${Math.min(budgetUsed, 100)}%`}}
                />
              </div>
              <p className="text-xs text-white/60">
                Còn lại: {formatCurrency(period.totalAmount - totalSpent, true)}
              </p>
            </div>
          </div>
        </Card>

        {/* Daily remaining */}
        {dailyCategories.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-foreground mb-3 px-1 flex items-center gap-2">
              <Clock className="h-4 w-4 text-primary"/>
              Hạn mức hôm nay
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {dailyCategories.map((item) => {
                // Simplified daily remaining mock
                const dailySpent = item.spent / 30;
                const dailyLimit = item.allocation.limitAmount;
                const dailyRemaining = dailyLimit - dailySpent;
                const dailyPercent = calcPercentage(dailySpent, dailyLimit);

                return (
                  <Card key={item.category.id} className="border-0 shadow-sm card-hover">
                    <CardContent className="p-4">
                      <div className="flex items-center gap-3 mb-3">
                        <CategoryIcon iconName={item.category.icon} categoryId={item.category.id} size={16}/>
                        <div className="flex-1">
                          <p className="text-sm font-medium">{item.category.name}</p>
                          <p className="text-xs text-muted-foreground">
                            Hạn mức: {formatCurrency(dailyLimit)}/ngày
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center justify-between mb-1.5">
                        <span className={cn(
                          "text-lg font-bold tabular-nums",
                          dailyRemaining < 0 ? "text-destructive" : "text-success"
                        )}>
                          {dailyRemaining >= 0 ? "Còn " : "Vượt "}{formatCurrency(Math.abs(dailyRemaining))}
                        </span>
                        <span className="text-xs text-muted-foreground">{dailyPercent}%</span>
                      </div>
                      <Progress
                        value={Math.min(dailyPercent, 100)}
                        className={cn(
                          "h-1.5",
                          dailyPercent >= 100
                            ? "[&>[data-slot=indicator]]:bg-destructive"
                            : dailyPercent >= 80
                              ? "[&>[data-slot=indicator]]:bg-warning"
                              : "[&>[data-slot=indicator]]:bg-success"
                        )}
                      />
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>
        )}

        {/* Category allocations */}
        <div>
          <h3 className="text-sm font-semibold text-foreground mb-3 px-1 flex items-center gap-2">
            <CalendarDays className="h-4 w-4 text-primary"/>
            Phân bổ theo danh mục — Tháng 8
          </h3>
          <Card className="border-0 shadow-sm">
            <CardContent className="p-0">
              {summaries.map((item, index) => {
                const isOver = item.percentage >= 100;
                const isWarning = item.percentage >= 80 && item.percentage < 100;
                const limit = item.allocation.limitType === "DAILY"
                  ? item.allocation.limitAmount * 30
                  : item.allocation.limitAmount;

                return (
                  <div
                    key={item.category.id}
                    className={cn(
                      "flex items-center gap-3 px-4 py-3.5",
                      index < summaries.length - 1 && "border-b border-border/50"
                    )}
                  >
                    <CategoryIcon iconName={item.category.icon} categoryId={item.category.id} size={16}/>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium">{item.category.name}</span>
                          <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4">
                            {item.allocation.limitType === "DAILY" ? "Ngày" : "Tháng"}
                          </Badge>
                        </div>
                        <span className={cn(
                          "text-xs font-bold tabular-nums",
                          isOver ? "text-destructive" : isWarning ? "text-warning" : "text-success"
                        )}>
                          {item.percentage}%
                        </span>
                      </div>
                      <Progress
                        value={Math.min(item.percentage, 100)}
                        className={cn(
                          "h-1.5 mb-1",
                          isOver
                            ? "[&>[data-slot=indicator]]:bg-destructive"
                            : isWarning
                              ? "[&>[data-slot=indicator]]:bg-warning"
                              : "[&>[data-slot=indicator]]:bg-success"
                        )}
                      />
                      <div className="flex justify-between text-[11px] text-muted-foreground">
                        <span>Đã chi: {formatCurrency(item.spent, true)}</span>
                        <span>Hạn mức: {formatCurrency(limit, true)}</span>
                      </div>
                    </div>
                    {isOver && (
                      <AlertTriangle className="h-4 w-4 text-destructive shrink-0"/>
                    )}
                  </div>
                );
              })}
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}
