"use client";

import Link from "next/link";
import {ArrowRight} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Progress} from "@/components/ui/progress";
import {getCategoryBudgetSummaries} from "@/mock/budget";
import {CategoryIcon} from "@/components/shared/category-icon";
import {formatCurrency} from "@/lib/format";
import {cn} from "@/lib/utils";

export function BudgetProgress() {
  const summaries = getCategoryBudgetSummaries("2026-08").slice(0, 5);

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="flex-row items-center justify-between pb-2">
        <CardTitle className="text-base font-semibold">Ngân sách tháng này</CardTitle>
        <Link href="/budget">
          <Button variant="ghost" size="sm" className="text-xs text-primary gap-1 h-8 px-2">
            Chi tiết <ArrowRight className="h-3 w-3"/>
          </Button>
        </Link>
      </CardHeader>
      <CardContent className="space-y-4">
        {summaries.map((item, index) => {
          const isOver = item.percentage >= 100;
          const isWarning = item.percentage >= 80 && item.percentage < 100;

          return (
            <div
              key={item.category.id}
              className="stagger-item"
              style={{animationDelay: `${index * 80}ms`}}
            >
              <div className="flex items-center justify-between mb-1.5">
                <div className="flex items-center gap-2">
                  <CategoryIcon
                    iconName={item.category.icon}
                    categoryId={item.category.id}
                    size={14}
                    className="!p-1.5"
                  />
                  <span className="text-sm font-medium">{item.category.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted-foreground tabular-nums">
                    {formatCurrency(item.spent, true)} / {formatCurrency(
                    item.allocation.limitType === "DAILY"
                      ? item.allocation.limitAmount * 30
                      : item.allocation.limitAmount,
                    true
                  )}
                  </span>
                  <span
                    className={cn(
                      "text-xs font-bold tabular-nums",
                      isOver ? "text-destructive" : isWarning ? "text-warning" : "text-success"
                    )}
                  >
                    {item.percentage}%
                  </span>
                </div>
              </div>
              <div className="relative">
                <Progress
                  value={Math.min(item.percentage, 100)}
                  className={cn(
                    "h-2 rounded-full",
                    isOver
                      ? "[&>[data-slot=indicator]]:bg-destructive"
                      : isWarning
                        ? "[&>[data-slot=indicator]]:bg-warning"
                        : "[&>[data-slot=indicator]]:bg-success"
                  )}
                />
                {isOver && (
                  <div
                    className="absolute top-0 h-2 rounded-full bg-destructive/30 animate-pulse"
                    style={{width: `${Math.min((item.percentage - 100) * 2, 100)}%`, left: "100%", maxWidth: "20%"}}
                  />
                )}
              </div>
            </div>
          );
        })}
      </CardContent>
    </Card>
  );
}
