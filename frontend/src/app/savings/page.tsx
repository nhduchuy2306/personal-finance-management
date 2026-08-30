"use client";

import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {Progress} from "@/components/ui/progress";
import {
  AlertTriangle,
  Calendar,
  CheckCircle2,
  PiggyBank,
  Plane,
  Plus,
  ShieldCheck,
  Smartphone,
  Target,
  TrendingUp,
  Trophy,
} from "lucide-react";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getSavingsProgress, getTotalSaved, mockContributions, mockSavingsGoals} from "@/mock/savings";
import {formatCurrency, formatDate} from "@/lib/format";
import {cn} from "@/lib/utils";

const goalIcons: Record<string, React.ElementType> = {
  "sg-001": Smartphone,
  "sg-002": Plane,
  "sg-003": ShieldCheck,
  "sg-004": Target,
};

export default function SavingsPage() {
  const totalSaved = getTotalSaved();
  const activeGoals = mockSavingsGoals.filter((g) => g.status === "ACTIVE");
  const completedGoals = mockSavingsGoals.filter((g) => g.status === "COMPLETED");

  return (
    <>
      <Header title="Tiết kiệm" subtitle="Theo dõi mục tiêu tiết kiệm"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in">
        {/* Total saved summary */}
        <Card className="border-0 shadow-md overflow-hidden">
          <div className="gradient-success p-5 md:p-6 text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-white/80 mb-1">Tổng đã tiết kiệm</p>
                <CurrencyDisplay
                  amount={totalSaved}
                  size="xl"
                  className="text-white animate-count-in"
                />
              </div>
              <div className="h-12 w-12 rounded-2xl bg-white/20 flex items-center justify-center">
                <PiggyBank className="h-6 w-6 text-white"/>
              </div>
            </div>
            <p className="text-xs text-white/60 mt-2">
              {activeGoals.length} mục tiêu đang hoạt động
            </p>
          </div>
        </Card>

        {/* Add goal */}
        <Button className="w-full h-12 rounded-xl gradient-primary border-0 text-white gap-2 shadow-md">
          <Plus className="h-5 w-5"/> Tạo mục tiêu mới
        </Button>

        {/* Active goals */}
        <div>
          <h3 className="text-sm font-semibold text-foreground mb-3 px-1 flex items-center gap-2">
            <Target className="h-4 w-4 text-primary"/>
            Đang thực hiện
          </h3>
          <div className="space-y-3">
            {activeGoals.map((goal, index) => {
              const progress = getSavingsProgress(goal);
              const GoalIcon = goalIcons[goal.id] || Target;

              return (
                <Card
                  key={goal.id}
                  className="border-0 shadow-sm card-hover stagger-item"
                  style={{animationDelay: `${index * 100}ms`}}
                >
                  <CardContent className="p-4">
                    {/* Goal header */}
                    <div className="flex items-start gap-3 mb-4">
                      <div
                        className="h-12 w-12 rounded-2xl bg-emerald-500/15 flex items-center justify-center shrink-0">
                        <GoalIcon className="h-6 w-6 text-emerald-600 dark:text-emerald-400"/>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-0.5">
                          <h3 className="text-base font-semibold truncate">{goal.name}</h3>
                          {!progress.isOnTrack && (
                            <Badge variant="outline"
                                   className="text-[10px] border-amber-500 text-amber-600 dark:text-amber-400 gap-0.5 shrink-0 ml-2">
                              <AlertTriangle className="h-3 w-3"/> Chậm
                            </Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <Calendar className="h-3 w-3"/>
                          <span>Hạn: {formatDate(goal.targetDate)}</span>
                          <span>·</span>
                          <span>{progress.daysLeft} ngày còn lại</span>
                        </div>
                      </div>
                    </div>

                    {/* Progress bar */}
                    <div className="space-y-2 mb-4">
                      <div className="flex justify-between text-sm">
                        <CurrencyDisplay amount={goal.currentSavedAmount} size="sm"
                                         className="font-semibold text-foreground"/>
                        <CurrencyDisplay amount={goal.targetAmount} size="sm" className="text-muted-foreground"/>
                      </div>
                      <Progress
                        value={progress.percentage}
                        className={cn(
                          "h-3 rounded-full",
                          progress.isOnTrack
                            ? "[&>[data-slot=indicator]]:bg-emerald-500"
                            : "[&>[data-slot=indicator]]:bg-amber-500"
                        )}
                      />
                      <div className="flex justify-between text-xs text-muted-foreground">
                        <span>{progress.percentage}% hoàn thành</span>
                        <span>Còn {formatCurrency(progress.remainingAmount, true)}</span>
                      </div>
                    </div>

                    {/* Contribution info */}
                    <div className="flex items-center justify-between p-3 rounded-xl bg-muted/40">
                      <div>
                        <p className="text-xs text-muted-foreground">Cần tiết kiệm</p>
                        <p className="text-sm font-semibold tabular-nums">
                          {formatCurrency(progress.requiredPerPeriod)} / {goal.contributionFrequency === "DAILY" ? "ngày" : "tuần"}
                        </p>
                      </div>
                      <Button size="sm" className="rounded-xl gradient-primary border-0 text-white gap-1.5">
                        <Plus className="h-3.5 w-3.5"/> Nạp tiền
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Completed goals */}
        {completedGoals.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-foreground mb-3 px-1 flex items-center gap-2">
              <Trophy className="h-4 w-4 text-amber-500"/>
              Đã hoàn thành
            </h3>
            <div className="space-y-3">
              {completedGoals.map((goal) => {
                const GoalIcon = goalIcons[goal.id] || Target;
                return (
                  <Card key={goal.id} className="border-0 shadow-sm bg-muted/30">
                    <CardContent className="p-4">
                      <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-xl bg-amber-500/15 flex items-center justify-center">
                          <Trophy className="h-5 w-5 text-amber-500"/>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold truncate">{goal.name}</p>
                          <p className="text-xs text-muted-foreground">
                            Đã tiết kiệm {formatCurrency(goal.targetAmount, true)} ✓
                          </p>
                        </div>
                        <Badge className="bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 text-[10px]">
                          <CheckCircle2 className="h-3 w-3 mr-1"/> Hoàn thành
                        </Badge>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>
        )}

        {/* Recent contributions */}
        <div>
          <h3 className="text-sm font-semibold text-foreground mb-3 px-1 flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-primary"/>
            Đóng góp gần đây
          </h3>
          <Card className="border-0 shadow-sm">
            <CardContent className="p-0">
              {mockContributions.slice(0, 5).map((contrib, i) => {
                const goal = mockSavingsGoals.find((g) => g.id === contrib.savingsGoalId);
                return (
                  <div
                    key={contrib.id}
                    className={cn(
                      "flex items-center gap-3 px-4 py-3",
                      i < Math.min(mockContributions.length, 5) - 1 && "border-b border-border/50"
                    )}
                  >
                    <div className="h-8 w-8 rounded-lg bg-emerald-500/10 flex items-center justify-center">
                      <Plus className="h-4 w-4 text-emerald-600 dark:text-emerald-400"/>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{contrib.note}</p>
                      <p className="text-[11px] text-muted-foreground">
                        {goal?.name} · {formatDate(contrib.contributedAt, "relative")}
                      </p>
                    </div>
                    <CurrencyDisplay amount={contrib.amount} type="EXPENSE" showSign size="sm"
                                     className="font-semibold"/>
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
