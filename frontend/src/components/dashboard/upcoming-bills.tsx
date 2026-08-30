"use client";

import Link from "next/link";
import {AlertTriangle, ArrowRight, CalendarClock} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getUpcomingBills} from "@/mock/bills";
import {formatDate} from "@/lib/format";

export function UpcomingBills() {
  const bills = getUpcomingBills(30).slice(0, 3);

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="flex-row items-center justify-between pb-2">
        <CardTitle className="text-base font-semibold">Hóa đơn sắp đến hạn</CardTitle>
        <Link href="/bills">
          <Button variant="ghost" size="sm" className="text-xs text-primary gap-1 h-8 px-2">
            Xem tất cả <ArrowRight className="h-3 w-3"/>
          </Button>
        </Link>
      </CardHeader>
      <CardContent className="space-y-3">
        {bills.length === 0 ? (
          <p className="text-sm text-muted-foreground text-center py-4">
            Không có hóa đơn sắp đến hạn
          </p>
        ) : (
          bills.map((bill, index) => {
            const daysUntil = Math.ceil(
              (new Date(bill.nextDueDate).getTime() - new Date("2026-08-30").getTime()) /
              (1000 * 60 * 60 * 24)
            );
            const isUrgent = daysUntil <= 3;

            return (
              <div
                key={bill.id}
                className="flex items-center gap-3 p-3 rounded-xl bg-muted/40 stagger-item"
                style={{animationDelay: `${index * 80}ms`}}
              >
                <div className={`h-10 w-10 rounded-xl flex items-center justify-center ${
                  isUrgent ? "bg-destructive/15 text-destructive" : "bg-amber-500/15 text-amber-600 dark:text-amber-400"
                }`}>
                  {isUrgent ? (
                    <AlertTriangle className="h-5 w-5"/>
                  ) : (
                    <CalendarClock className="h-5 w-5"/>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{bill.name}</p>
                  <p className="text-[11px] text-muted-foreground">
                    {formatDate(bill.nextDueDate)} · {daysUntil > 0 ? `${daysUntil} ngày nữa` : "Hôm nay"}
                  </p>
                </div>
                <div className="text-right">
                  <CurrencyDisplay
                    amount={bill.estimatedAmount}
                    size="sm"
                    className="font-semibold"
                  />
                  {isUrgent && (
                    <Badge variant="destructive" className="text-[10px] mt-1 px-1.5 py-0">
                      Khẩn
                    </Badge>
                  )}
                </div>
              </div>
            );
          })
        )}
      </CardContent>
    </Card>
  );
}
