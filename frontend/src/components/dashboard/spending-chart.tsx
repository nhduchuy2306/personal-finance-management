"use client";

import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,} from "recharts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {getDailyExpenses, getExpenseByCategoryForMonth} from "@/mock/transactions";
import {getCategoryById} from "@/mock/categories";
import {chartCategoryColors} from "@/components/shared/category-icon";
import {formatCurrency} from "@/lib/format";

export function SpendingChart() {
  const categoryData = getExpenseByCategoryForMonth("2026-08");
  const dailyData = getDailyExpenses(7);

  const pieData = categoryData.map((item) => ({
    name: getCategoryById(item.categoryId)?.name || "Khác",
    value: item.amount,
    fill: chartCategoryColors[item.categoryId] || "#6b7280",
  }));

  const barData = dailyData.map((item) => ({
    date: new Date(item.date).toLocaleDateString("vi-VN", {day: "2-digit", month: "2-digit"}),
    amount: item.amount,
  }));

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="pb-2">
        <CardTitle className="text-base font-semibold">Chi tiêu tháng 8</CardTitle>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="category" className="w-full">
          <TabsList className="grid w-full grid-cols-2 mb-4">
            <TabsTrigger value="category">Theo danh mục</TabsTrigger>
            <TabsTrigger value="daily">7 ngày qua</TabsTrigger>
          </TabsList>

          <TabsContent value="category">
            <div className="flex flex-col md:flex-row items-center gap-4">
              <div className="w-full md:w-1/2 h-[200px]">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={55}
                      outerRadius={85}
                      paddingAngle={3}
                      dataKey="value"
                      strokeWidth={0}
                    >
                      {pieData.map((entry, i) => (
                        <Cell key={i} fill={entry.fill}/>
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value) => formatCurrency(Number(value))}
                      contentStyle={{
                        borderRadius: "12px",
                        border: "none",
                        boxShadow: "0 4px 20px rgba(0,0,0,0.1)",
                        fontSize: "13px",
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="w-full md:w-1/2 space-y-2">
                {pieData.slice(0, 5).map((item, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2">
                      <div
                        className="h-3 w-3 rounded-full"
                        style={{backgroundColor: item.fill}}
                      />
                      <span className="text-muted-foreground">{item.name}</span>
                    </div>
                    <span className="font-medium tabular-nums">
                      {formatCurrency(item.value, true)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </TabsContent>

          <TabsContent value="daily">
            <div className="h-[200px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={barData} barSize={24}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)"/>
                  <XAxis
                    dataKey="date"
                    fontSize={11}
                    tickLine={false}
                    axisLine={false}
                    tick={{fill: "var(--muted-foreground)"}}
                  />
                  <YAxis
                    fontSize={11}
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(v) => formatCurrency(v, true)}
                    tick={{fill: "var(--muted-foreground)"}}
                    width={50}
                  />
                  <Tooltip
                    formatter={(value) => [formatCurrency(Number(value)), "Chi tiêu"]}
                    contentStyle={{
                      borderRadius: "12px",
                      border: "none",
                      boxShadow: "0 4px 20px rgba(0,0,0,0.1)",
                      fontSize: "13px",
                    }}
                  />
                  <Bar dataKey="amount" fill="var(--primary)" radius={[6, 6, 0, 0]}/>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}
