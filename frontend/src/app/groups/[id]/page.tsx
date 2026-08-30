"use client";

import {use} from "react";
import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {ArrowDownLeft, ArrowRight, ArrowUpRight, HandCoins, Plus} from "lucide-react";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getGroupBalances, getSimplifiedDebts, mockGroups, mockSharedExpenses,} from "@/mock/groups";
import {formatCurrency, formatDate} from "@/lib/format";
import {cn} from "@/lib/utils";

export default function GroupDetailPage({params}: { params: Promise<{ id: string }> }) {
  const {id} = use(params);
  const group = mockGroups.find((g) => g.id === id);
  if (!group) return <div className="p-6">Nhóm không tồn tại</div>;

  const balances = getGroupBalances(group.id);
  const debts = getSimplifiedDebts(group.id);
  const expenses = mockSharedExpenses.filter((e) => e.groupId === group.id);

  return (
    <>
      <Header title={group.name} subtitle={`${group.members.length} thành viên`}/>

      <div className="p-4 md:p-6 space-y-4 animate-fade-in">
        {/* Actions */}
        <div className="grid grid-cols-2 gap-3">
          <Button className="h-12 rounded-xl gradient-primary border-0 text-white gap-2">
            <Plus className="h-4 w-4"/> Thêm chi tiêu
          </Button>
          <Button variant="outline" className="h-12 rounded-xl gap-2">
            <HandCoins className="h-4 w-4"/> Thanh toán
          </Button>
        </div>

        <Tabs defaultValue="balances" className="w-full">
          <TabsList className="grid w-full grid-cols-3 mb-4">
            <TabsTrigger value="balances">Số dư</TabsTrigger>
            <TabsTrigger value="debts">Nợ đơn giản</TabsTrigger>
            <TabsTrigger value="expenses">Chi tiêu</TabsTrigger>
          </TabsList>

          {/* Balances tab */}
          <TabsContent value="balances">
            <Card className="border-0 shadow-sm">
              <CardContent className="p-0">
                {balances.map((b, i) => (
                  <div
                    key={b.memberId}
                    className={cn(
                      "flex items-center gap-3 px-4 py-3.5",
                      i < balances.length - 1 && "border-b border-border/50"
                    )}
                  >
                    <div className="h-9 w-9 rounded-full bg-muted flex items-center justify-center text-xs font-bold">
                      {b.memberName.charAt(0)}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{b.memberName}</p>
                    </div>
                    <div className="flex items-center gap-1.5">
                      {b.balance > 0 ? (
                        <ArrowDownLeft className="h-3.5 w-3.5 text-success"/>
                      ) : b.balance < 0 ? (
                        <ArrowUpRight className="h-3.5 w-3.5 text-destructive"/>
                      ) : null}
                      <span
                        className={cn(
                          "text-sm font-semibold tabular-nums",
                          b.balance > 0 ? "text-success" : b.balance < 0 ? "text-destructive" : "text-muted-foreground"
                        )}
                      >
                        {b.balance > 0
                          ? `+${formatCurrency(b.balance)}`
                          : b.balance < 0
                            ? formatCurrency(b.balance)
                            : "0₫"}
                      </span>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Simplified debts tab */}
          <TabsContent value="debts">
            <div className="space-y-3">
              {debts.length === 0 ? (
                <Card className="border-0 shadow-sm">
                  <CardContent className="p-6 text-center text-muted-foreground text-sm">
                    Không có khoản nợ nào
                  </CardContent>
                </Card>
              ) : (
                debts.map((debt, i) => (
                  <Card key={i} className="border-0 shadow-sm">
                    <CardContent className="p-4">
                      <div className="flex items-center gap-3">
                        <div
                          className="h-9 w-9 rounded-full bg-destructive/10 flex items-center justify-center text-xs font-bold text-destructive">
                          {debt.from.displayName.charAt(0)}
                        </div>
                        <div className="flex-1 text-center">
                          <ArrowRight className="h-4 w-4 text-muted-foreground mx-auto mb-1"/>
                          <CurrencyDisplay amount={debt.amount} size="lg" className="text-foreground"/>
                        </div>
                        <div
                          className="h-9 w-9 rounded-full bg-success/10 flex items-center justify-center text-xs font-bold text-success">
                          {debt.to.displayName.charAt(0)}
                        </div>
                      </div>
                      <div className="flex justify-between mt-2 text-xs text-muted-foreground">
                        <span>{debt.from.displayName}</span>
                        <span>trả cho</span>
                        <span>{debt.to.displayName}</span>
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>
          </TabsContent>

          {/* Expenses tab */}
          <TabsContent value="expenses">
            <Card className="border-0 shadow-sm">
              <CardContent className="p-0">
                {expenses.map((exp, i) => {
                  const payer = group.members.find((m) => m.id === exp.paidByMemberId);
                  return (
                    <div
                      key={exp.id}
                      className={cn(
                        "px-4 py-3.5",
                        i < expenses.length - 1 && "border-b border-border/50"
                      )}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <p className="text-sm font-medium">{exp.description}</p>
                        <CurrencyDisplay amount={exp.totalAmount} size="sm" className="font-semibold"/>
                      </div>
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <span>{payer?.displayName} trả</span>
                        <span>·</span>
                        <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4">
                          {exp.splitMethod === "EQUAL" ? "Chia đều" : exp.splitMethod}
                        </Badge>
                        <span>·</span>
                        <span>{formatDate(exp.expenseDate, "relative")}</span>
                      </div>
                    </div>
                  );
                })}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
