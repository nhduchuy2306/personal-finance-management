"use client";

import Link from "next/link";
import {usePathname} from "next/navigation";
import {
  ArrowLeftRight,
  CalendarClock,
  LayoutDashboard,
  PiggyBank,
  ScanLine,
  Settings,
  TrendingUp,
  Users,
  Wallet,
} from "lucide-react";
import {cn} from "@/lib/utils";
import {NAV_ITEMS} from "@/lib/constants";
import {Separator} from "@/components/ui/separator";
import {ThemeToggle} from "./theme-toggle";

const iconComponents: Record<string, React.ElementType> = {
  LayoutDashboard, ArrowLeftRight, Wallet, Users, ScanLine,
  CalendarClock, PiggyBank, Settings,
};

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="hidden md:flex md:w-64 lg:w-72 flex-col border-r border-border bg-sidebar h-screen sticky top-0">
      {/* Logo */}
      <div className="flex items-center gap-3 px-6 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl gradient-primary">
          <TrendingUp className="h-5 w-5 text-white"/>
        </div>
        <div>
          <h1 className="text-lg font-bold tracking-tight text-sidebar-foreground">
            FinanceFlow
          </h1>
          <p className="text-[11px] text-muted-foreground -mt-0.5">Quản lý tài chính</p>
        </div>
      </div>

      <Separator className="mx-4"/>

      {/* Nav items */}
      <nav className="flex-1 px-3 py-4 space-y-1 custom-scrollbar overflow-y-auto">
        {NAV_ITEMS.map((item) => {
          const Icon = iconComponents[item.icon];
          const isActive = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200",
                isActive
                  ? "bg-sidebar-primary text-sidebar-primary-foreground shadow-sm"
                  : "text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
              )}
            >
              <Icon className="h-[18px] w-[18px] shrink-0"/>
              <span>{item.label}</span>
              {isActive && (
                <div className="ml-auto h-1.5 w-1.5 rounded-full bg-sidebar-primary-foreground/60"/>
              )}
            </Link>
          );
        })}
      </nav>

      {/* Bottom section */}
      <div className="px-4 py-4 border-t border-border">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div
              className="h-8 w-8 rounded-full gradient-primary flex items-center justify-center text-white text-xs font-bold">
              ĐH
            </div>
            <div className="text-sm">
              <p className="font-medium text-foreground leading-tight">Đức Huỳnh</p>
              <p className="text-[11px] text-muted-foreground">Premium</p>
            </div>
          </div>
          <ThemeToggle/>
        </div>
      </div>
    </aside>
  );
}
