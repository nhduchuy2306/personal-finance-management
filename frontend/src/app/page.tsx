"use client";

import {Header} from "@/components/layout/header";
import {BalanceOverview} from "@/components/dashboard/balance-overview";
import {SpendingChart} from "@/components/dashboard/spending-chart";
import {RecentTransactions} from "@/components/dashboard/recent-transactions";
import {BudgetProgress} from "@/components/dashboard/budget-progress";
import {UpcomingBills} from "@/components/dashboard/upcoming-bills";
import {SavingsSummary} from "@/components/dashboard/savings-summary";

export default function DashboardPage() {
  return (
    <>
      <Header title="Xin chào, Đức 👋" subtitle="Tháng 8, 2026"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in">
        {/* Balance Overview */}
        <BalanceOverview/>

        {/* Charts + Recent Transactions */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6">
          <SpendingChart/>
          <RecentTransactions/>
        </div>

        {/* Budget + Bills + Savings */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-6">
          <BudgetProgress/>
          <UpcomingBills/>
          <SavingsSummary/>
        </div>
      </div>
    </>
  );
}
