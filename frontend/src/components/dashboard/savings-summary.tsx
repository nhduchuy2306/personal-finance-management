"use client";

import Link from "next/link";
import {AlertTriangle, ArrowRight, Target} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getActiveSavingsGoals, getSavingsProgress} from "@/mock/savings";
import {cn} from "@/lib/utils";

export function SavingsSummary() {
  const goals = getActiveSavingsGoals();

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="flex-row items-center justify-between pb-2">
        <CardTitle className="text-base font-semibold">Mục tiêu tiết kiệm</CardTitle>
        <Link href="/savings">
          <Button variant="ghost" size="sm" className="text-xs text-primary gap-1 h-8 px-2">
            Xem tất cả <ArrowRight className="h-3 w-3"/>
          </Button>
        </Link>
      </CardHeader>
      <CardContent className="space-y-3">
        {goals.map((goal, index) => {
          const progress = getSavingsProgress(goal);

          return (
            <div
              key={goal.id}
              className="p-3 rounded-xl bg-muted/40 stagger-item"
              style={{animationDelay: `${index * 80}ms`}}
            >
              <div className="flex items-start justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div className="h-9 w-9 rounded-xl bg-emerald-500/15 flex items-center justify-center">
                    <Target className="h-4 w-4 text-emerald-600 dark:text-emerald-400"/>
                  </div>
                  <div>
                    <p className="text-sm font-medium leading-tight">{goal.name}</p>
                    <p className="text-[11px] text-muted-foreground">
                      {progress.daysLeft} ngày còn lại
                    </p>
                  </div>
                </div>
                {!progress.isOnTrack && (
                  <Badge variant="outline"
                         className="text-[10px] border-amber-500 text-amber-600 dark:text-amber-400 gap-0.5">
                    <AlertTriangle className="h-3 w-3"/> Chậm
                  </Badge>
                )}
              </div>

              {/* Progress ring */}
              <div className="flex items-center gap-3">
                <div className="relative h-12 w-12 shrink-0">
                  <svg className="h-12 w-12 -rotate-90" viewBox="0 0 48 48">
                    <circle
                      cx="24" cy="24" r="20"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="4"
                      className="text-muted"
                    />
                    <circle
                      cx="24" cy="24" r="20"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="4"
                      strokeDasharray={`${(progress.percentage / 100) * 125.6} 125.6`}
                      strokeLinecap="round"
                      className={cn(
                        "transition-all duration-1000",
                        progress.isOnTrack ? "text-emerald-500" : "text-amber-500"
                      )}
                    />
                  </svg>
                  <span
                    className="absolute inset-0 flex items-center justify-center text-[11px] font-bold tabular-nums">
                    {progress.percentage}%
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between text-xs text-muted-foreground mb-0.5">
                    <span>Đã tiết kiệm</span>
                    <span>Mục tiêu</span>
                  </div>
                  <div className="flex justify-between">
                    <CurrencyDisplay amount={goal.currentSavedAmount} compact size="sm"
                                     className="font-semibold text-foreground"/>
                    <CurrencyDisplay amount={goal.targetAmount} compact size="sm" className="text-muted-foreground"/>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </CardContent>
    </Card>
  );
}
