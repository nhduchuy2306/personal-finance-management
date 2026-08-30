"use client";

import Link from "next/link";
import {usePathname} from "next/navigation";
import {useState} from "react";
import {
  ArrowLeftRight,
  CalendarClock,
  LayoutDashboard,
  Menu,
  PiggyBank,
  PlusCircle,
  ScanLine,
  Settings,
  Users,
  Wallet,
} from "lucide-react";
import {cn} from "@/lib/utils";
import {Sheet, SheetContent, SheetTitle} from "@/components/ui/sheet";

const mainTabs = [
  {label: "Tổng quan", href: "/", icon: LayoutDashboard},
  {label: "Giao dịch", href: "/transactions", icon: ArrowLeftRight},
  {label: "Thêm", href: "#add", icon: PlusCircle, isAction: true},
  {label: "Ngân sách", href: "/budget", icon: Wallet},
  {label: "Khác", href: "#more", icon: Menu, isMore: true},
];

const moreItems = [
  {label: "Nhóm chi", href: "/groups", icon: Users},
  {label: "Hóa đơn", href: "/receipts", icon: ScanLine},
  {label: "Thanh toán", href: "/bills", icon: CalendarClock},
  {label: "Tiết kiệm", href: "/savings", icon: PiggyBank},
  {label: "Cài đặt", href: "/settings", icon: Settings},
];

export function MobileNav() {
  const pathname = usePathname();
  const [moreOpen, setMoreOpen] = useState(false);
  const [addOpen, setAddOpen] = useState(false);

  const isActiveInMore = moreItems.some((item) => pathname === item.href);

  return (
    <>
      {/* Bottom navigation bar */}
      <nav
        className="md:hidden fixed bottom-0 left-0 right-0 z-50 border-t border-border bg-background/80 backdrop-blur-xl safe-bottom">
        <div className="flex items-center justify-around px-2 py-1">
          {mainTabs.map((tab) => {
            if (tab.isAction) {
              return (
                <button
                  key="add"
                  onClick={() => setAddOpen(true)}
                  className="flex flex-col items-center justify-center py-1 px-3 -mt-4"
                >
                  <div
                    className="flex items-center justify-center h-12 w-12 rounded-2xl gradient-primary shadow-lg shadow-primary/30">
                    <PlusCircle className="h-6 w-6 text-white"/>
                  </div>
                </button>
              );
            }

            if (tab.isMore) {
              return (
                <button
                  key="more"
                  onClick={() => setMoreOpen(true)}
                  className={cn(
                    "flex flex-col items-center justify-center py-2 px-3 gap-0.5 min-w-[56px]",
                    isActiveInMore ? "text-primary" : "text-muted-foreground"
                  )}
                >
                  <Menu className="h-5 w-5"/>
                  <span className="text-[10px] font-medium">Khác</span>
                </button>
              );
            }

            const isActive = pathname === tab.href;
            const Icon = tab.icon;

            return (
              <Link
                key={tab.href}
                href={tab.href}
                className={cn(
                  "flex flex-col items-center justify-center py-2 px-3 gap-0.5 min-w-[56px] transition-colors",
                  isActive ? "text-primary" : "text-muted-foreground"
                )}
              >
                <Icon className="h-5 w-5"/>
                <span className="text-[10px] font-medium">{tab.label}</span>
                {isActive && (
                  <div className="absolute top-0 h-0.5 w-8 rounded-full bg-primary"/>
                )}
              </Link>
            );
          })}
        </div>
      </nav>

      {/* More menu sheet */}
      <Sheet open={moreOpen} onOpenChange={setMoreOpen}>
        <SheetContent side="bottom" className="rounded-t-3xl pb-8">
          <SheetTitle className="text-lg font-semibold mb-4">Khác</SheetTitle>
          <div className="grid grid-cols-4 gap-4">
            {moreItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMoreOpen(false)}
                  className="flex flex-col items-center gap-2 py-3"
                >
                  <div
                    className={cn(
                      "h-12 w-12 rounded-2xl flex items-center justify-center transition-colors",
                      isActive
                        ? "gradient-primary text-white"
                        : "bg-muted text-muted-foreground"
                    )}
                  >
                    <Icon className="h-5 w-5"/>
                  </div>
                  <span className={cn(
                    "text-xs font-medium",
                    isActive ? "text-primary" : "text-muted-foreground"
                  )}>
                    {item.label}
                  </span>
                </Link>
              );
            })}
          </div>
        </SheetContent>
      </Sheet>

      {/* Add transaction sheet */}
      <Sheet open={addOpen} onOpenChange={setAddOpen}>
        <SheetContent side="bottom" className="rounded-t-3xl pb-8">
          <SheetTitle className="text-lg font-semibold mb-4">Thêm mới</SheetTitle>
          <div className="grid grid-cols-3 gap-4">
            {[
              {
                label: "Chi tiêu",
                href: "/transactions?action=add&type=expense",
                icon: ArrowLeftRight,
                color: "bg-red-500/15 text-red-600 dark:text-red-400"
              },
              {
                label: "Thu nhập",
                href: "/transactions?action=add&type=income",
                icon: Wallet,
                color: "bg-green-500/15 text-green-600 dark:text-green-400"
              },
              {
                label: "Scan hóa đơn",
                href: "/receipts?action=upload",
                icon: ScanLine,
                color: "bg-blue-500/15 text-blue-600 dark:text-blue-400"
              },
            ].map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.label}
                  href={item.href}
                  onClick={() => setAddOpen(false)}
                  className="flex flex-col items-center gap-2 py-3"
                >
                  <div className={cn("h-14 w-14 rounded-2xl flex items-center justify-center", item.color)}>
                    <Icon className="h-6 w-6"/>
                  </div>
                  <span className="text-xs font-medium text-foreground">{item.label}</span>
                </Link>
              );
            })}
          </div>
        </SheetContent>
      </Sheet>
    </>
  );
}
