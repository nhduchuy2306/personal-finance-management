"use client";

import Link from "next/link";
import {ArrowRight} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {CategoryIcon} from "@/components/shared/category-icon";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getRecentTransactions} from "@/mock/transactions";
import {getCategoryById} from "@/mock/categories";
import {formatDate} from "@/lib/format";

export function RecentTransactions() {
  const transactions = getRecentTransactions(5);

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="flex-row items-center justify-between pb-2">
        <CardTitle className="text-base font-semibold">Giao dịch gần đây</CardTitle>
        <Link href="/transactions">
          <Button variant="ghost" size="sm" className="text-xs text-primary gap-1 h-8 px-2">
            Xem tất cả <ArrowRight className="h-3 w-3"/>
          </Button>
        </Link>
      </CardHeader>
      <CardContent className="px-3 md:px-6">
        <div className="space-y-1">
          {transactions.map((txn, index) => {
            const category = getCategoryById(txn.categoryId);
            return (
              <div
                key={txn.id}
                className="flex items-center gap-3 py-2.5 px-2 rounded-xl hover:bg-muted/50 transition-colors stagger-item"
                style={{animationDelay: `${index * 60}ms`}}
              >
                <CategoryIcon
                  iconName={category?.icon || "MoreHorizontal"}
                  categoryId={txn.categoryId}
                  size={16}
                />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">
                    {txn.note}
                  </p>
                  <p className="text-[11px] text-muted-foreground">
                    {category?.name} · {formatDate(txn.transactionDate, "relative")}
                  </p>
                </div>
                <CurrencyDisplay
                  amount={txn.amount}
                  type={txn.type}
                  showSign
                  size="sm"
                  className="font-semibold"
                />
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
