"use client";

import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {CalendarClock, CheckCircle2, Droplets, Dumbbell, Home, Music, Plus, Wifi, Zap,} from "lucide-react";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {getTotalUpcomingBillsAmount, getUpcomingBills, mockBills} from "@/mock/bills";
import {formatDate} from "@/lib/format";
import {cn} from "@/lib/utils";

const billIcons: Record<string, React.ElementType> = {
  "bill-001": Zap,
  "bill-002": Droplets,
  "bill-003": Wifi,
  "bill-004": Home,
  "bill-005": Music,
  "bill-006": Dumbbell,
};

const cycleLabels: Record<string, string> = {
  MONTHLY: "Hàng tháng",
  QUARTERLY: "Hàng quý",
  SEMI_ANNUAL: "6 tháng",
  ANNUAL: "Hàng năm",
  CUSTOM_DAYS: "Tùy chỉnh",
};

export default function BillsPage() {
  const upcoming = getUpcomingBills(60);
  const totalUpcoming = getTotalUpcomingBillsAmount(30);

  return (
    <>
      <Header title="Hóa đơn định kỳ" subtitle="Quản lý thanh toán định kỳ"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in">
        {/* Summary */}
        <Card className="border-0 shadow-md overflow-hidden">
          <div className="gradient-warning p-5 md:p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-amber-900/70 mb-1">Tổng hóa đơn sắp tới (30 ngày)</p>
                <CurrencyDisplay
                  amount={totalUpcoming}
                  size="xl"
                  className="text-amber-900 animate-count-in"
                />
              </div>
              <div className="h-12 w-12 rounded-2xl bg-white/30 flex items-center justify-center">
                <CalendarClock className="h-6 w-6 text-amber-900"/>
              </div>
            </div>
            <p className="text-xs text-amber-900/60 mt-2">
              {upcoming.length} hóa đơn cần thanh toán
            </p>
          </div>
        </Card>

        {/* Add bill button */}
        <Button className="w-full h-12 rounded-xl gradient-primary border-0 text-white gap-2 shadow-md">
          <Plus className="h-5 w-5"/> Thêm hóa đơn mới
        </Button>

        <Tabs defaultValue="upcoming" className="w-full">
          <TabsList className="grid w-full grid-cols-2 mb-4">
            <TabsTrigger value="upcoming">Sắp tới</TabsTrigger>
            <TabsTrigger value="all">Tất cả</TabsTrigger>
          </TabsList>

          <TabsContent value="upcoming">
            <div className="space-y-3">
              {upcoming.map((bill, index) => {
                const BillIcon = billIcons[bill.id] || CalendarClock;
                const daysUntil = Math.ceil(
                  (new Date(bill.nextDueDate).getTime() - new Date("2026-08-30").getTime()) / (1000 * 60 * 60 * 24)
                );
                const isUrgent = daysUntil <= 7;

                return (
                  <Card
                    key={bill.id}
                    className={cn("border-0 shadow-sm card-hover stagger-item", isUrgent && "ring-1 ring-amber-500/30")}
                    style={{animationDelay: `${index * 80}ms`}}
                  >
                    <CardContent className="p-4">
                      <div className="flex items-center gap-3">
                        <div className={cn(
                          "h-12 w-12 rounded-2xl flex items-center justify-center shrink-0",
                          isUrgent ? "bg-amber-500/15 text-amber-600 dark:text-amber-400" : "bg-muted text-muted-foreground"
                        )}>
                          <BillIcon className="h-6 w-6"/>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-0.5">
                            <h3 className="text-sm font-semibold truncate">{bill.name}</h3>
                            {bill.isFixedAmount && (
                              <Badge variant="secondary" className="text-[9px] px-1.5 py-0 h-4">Cố định</Badge>
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <span>{cycleLabels[bill.cycleType]}</span>
                            <span>·</span>
                            <span className={isUrgent ? "text-amber-600 dark:text-amber-400 font-medium" : ""}>
                              {daysUntil > 0 ? `${daysUntil} ngày nữa` : "Hôm nay"}
                            </span>
                          </div>
                          <p className="text-[11px] text-muted-foreground mt-0.5">
                            Hạn: {formatDate(bill.nextDueDate)}
                          </p>
                        </div>
                        <div className="text-right shrink-0">
                          <CurrencyDisplay amount={bill.estimatedAmount} size="sm" className="font-semibold"/>
                          <Button
                            size="sm"
                            variant="outline"
                            className="mt-1.5 h-7 text-[11px] rounded-lg px-2"
                          >
                            <CheckCircle2 className="h-3 w-3 mr-1"/> Thanh toán
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </TabsContent>

          <TabsContent value="all">
            <Card className="border-0 shadow-sm">
              <CardContent className="p-0">
                {mockBills.map((bill, i) => {
                  const BillIcon = billIcons[bill.id] || CalendarClock;
                  return (
                    <div
                      key={bill.id}
                      className={cn(
                        "flex items-center gap-3 px-4 py-3.5",
                        i < mockBills.length - 1 && "border-b border-border/50"
                      )}
                    >
                      <div className="h-10 w-10 rounded-xl bg-muted flex items-center justify-center">
                        <BillIcon className="h-5 w-5 text-muted-foreground"/>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{bill.name}</p>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <span>{cycleLabels[bill.cycleType]}</span>
                          <span>·</span>
                          <span>Ngày {bill.dueDayOfCycle}</span>
                          {!bill.isActive && <Badge variant="secondary" className="text-[9px]">Tắt</Badge>}
                        </div>
                      </div>
                      <CurrencyDisplay amount={bill.estimatedAmount} size="sm" className="font-semibold"/>
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
