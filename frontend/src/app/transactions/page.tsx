"use client";

import {useMemo, useState} from "react";
import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {ArrowUpDown, Plus, Search} from "lucide-react";
import {CategoryIcon} from "@/components/shared/category-icon";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {mockTransactions} from "@/mock/transactions";
import {getCategoryById, mockCategories} from "@/mock/categories";
import {formatCurrency, formatDate} from "@/lib/format";
import {cn} from "@/lib/utils";

export default function TransactionsPage() {
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<string>("all");
  const [typeFilter, setTypeFilter] = useState<string>("all");
  const [sortOrder, setSortOrder] = useState<"newest" | "oldest" | "highest" | "lowest">("newest");

  const filtered = useMemo(() => {
    let result = [...mockTransactions];

    if (search) {
      const q = search.toLowerCase();
      result = result.filter((t) => t.note.toLowerCase().includes(q));
    }
    if (categoryFilter !== "all") {
      result = result.filter((t) => t.categoryId === categoryFilter);
    }
    if (typeFilter !== "all") {
      result = result.filter((t) => t.type === typeFilter);
    }

    switch (sortOrder) {
      case "newest":
        result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        break;
      case "oldest":
        result.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
        break;
      case "highest":
        result.sort((a, b) => b.amount - a.amount);
        break;
      case "lowest":
        result.sort((a, b) => a.amount - b.amount);
        break;
    }

    return result;
  }, [search, categoryFilter, typeFilter, sortOrder]);

  // Group by date
  const grouped = useMemo(() => {
    const map = new Map<string, typeof filtered>();
    filtered.forEach((t) => {
      const date = t.transactionDate;
      if (!map.has(date)) map.set(date, []);
      map.get(date)!.push(t);
    });
    return Array.from(map.entries());
  }, [filtered]);

  const totalExpense = filtered.filter((t) => t.type === "EXPENSE").reduce((s, t) => s + t.amount, 0);
  const totalIncome = filtered.filter((t) => t.type === "INCOME").reduce((s, t) => s + t.amount, 0);

  return (
    <>
      <Header title="Giao dịch" subtitle={`${filtered.length} giao dịch`}/>

      <div className="p-4 md:p-6 space-y-4 animate-fade-in">
        {/* Summary cards */}
        <div className="grid grid-cols-2 gap-3">
          <Card className="border-0 shadow-sm">
            <CardContent className="p-4">
              <p className="text-xs text-muted-foreground mb-1">Tổng chi</p>
              <CurrencyDisplay amount={totalExpense} type="EXPENSE" size="lg"/>
            </CardContent>
          </Card>
          <Card className="border-0 shadow-sm">
            <CardContent className="p-4">
              <p className="text-xs text-muted-foreground mb-1">Tổng thu</p>
              <CurrencyDisplay amount={totalIncome} type="INCOME" size="lg"/>
            </CardContent>
          </Card>
        </div>

        {/* Filters */}
        <Card className="border-0 shadow-sm">
          <CardContent className="p-3 md:p-4">
            <div className="flex flex-col sm:flex-row gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
                <Input
                  placeholder="Tìm giao dịch..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="pl-9 h-9 bg-muted/50 border-0 rounded-xl"
                />
              </div>
              <div className="flex gap-2">
                <Select value={categoryFilter} onValueChange={(v) => v !== null && setCategoryFilter(v)}>
                  <SelectTrigger className="h-9 w-[130px] rounded-xl border-0 bg-muted/50">
                    <SelectValue placeholder="Danh mục"/>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Tất cả</SelectItem>
                    {mockCategories.map((c) => (
                      <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Select value={typeFilter} onValueChange={(v) => v !== null && setTypeFilter(v)}>
                  <SelectTrigger className="h-9 w-[110px] rounded-xl border-0 bg-muted/50">
                    <SelectValue placeholder="Loại"/>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Tất cả</SelectItem>
                    <SelectItem value="EXPENSE">Chi tiêu</SelectItem>
                    <SelectItem value="INCOME">Thu nhập</SelectItem>
                  </SelectContent>
                </Select>
                <Select value={sortOrder} onValueChange={(v) => v !== null && setSortOrder(v as typeof sortOrder)}>
                  <SelectTrigger className="h-9 w-[120px] rounded-xl border-0 bg-muted/50">
                    <ArrowUpDown className="h-3 w-3 mr-1"/>
                    <SelectValue/>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="newest">Mới nhất</SelectItem>
                    <SelectItem value="oldest">Cũ nhất</SelectItem>
                    <SelectItem value="highest">Cao nhất</SelectItem>
                    <SelectItem value="lowest">Thấp nhất</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Transaction list grouped by date */}
        <div className="space-y-4">
          {grouped.map(([date, transactions]) => {
            const dayTotal = transactions
              .filter((t) => t.type === "EXPENSE")
              .reduce((s, t) => s + t.amount, 0);

            return (
              <div key={date}>
                <div className="flex items-center justify-between px-1 mb-2">
                  <h3 className="text-sm font-semibold text-foreground">
                    {formatDate(date, "relative")}
                    <span className="text-xs font-normal text-muted-foreground ml-2">
                      {formatDate(date)}
                    </span>
                  </h3>
                  <span className="text-xs text-muted-foreground tabular-nums">
                    -{formatCurrency(dayTotal, true)}
                  </span>
                </div>
                <Card className="border-0 shadow-sm overflow-hidden">
                  <CardContent className="p-0">
                    {transactions.map((txn, i) => {
                      const category = getCategoryById(txn.categoryId);
                      return (
                        <div
                          key={txn.id}
                          className={cn(
                            "flex items-center gap-3 px-4 py-3 hover:bg-muted/50 transition-colors cursor-pointer",
                            i < transactions.length - 1 && "border-b border-border/50"
                          )}
                        >
                          <CategoryIcon
                            iconName={category?.icon || "MoreHorizontal"}
                            categoryId={txn.categoryId}
                            size={16}
                          />
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium truncate">{txn.note}</p>
                            <div className="flex items-center gap-1.5 mt-0.5">
                              <span className="text-[11px] text-muted-foreground">
                                {category?.name}
                              </span>
                              {txn.source !== "MANUAL" && (
                                <Badge variant="secondary" className="text-[9px] px-1 py-0 h-4">
                                  {txn.source === "OCR" ? "Scan" : "Nhóm"}
                                </Badge>
                              )}
                            </div>
                          </div>
                          <CurrencyDisplay
                            amount={txn.amount}
                            type={txn.type}
                            showSign
                            size="sm"
                            className="font-semibold shrink-0"
                          />
                        </div>
                      );
                    })}
                  </CardContent>
                </Card>
              </div>
            );
          })}
        </div>

        {/* Add transaction FAB (mobile) */}
        <div className="fixed bottom-24 right-4 md:hidden z-40">
          <Button size="lg" className="h-14 w-14 rounded-2xl shadow-lg gradient-primary border-0">
            <Plus className="h-6 w-6 text-white"/>
          </Button>
        </div>
      </div>
    </>
  );
}
