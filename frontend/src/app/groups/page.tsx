"use client";

import Link from "next/link";
import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {ArrowRight, Plus, Users} from "lucide-react";
import {getGroupBalances, mockGroups} from "@/mock/groups";

export default function GroupsPage() {
  return (
    <>
      <Header title="Nhóm chi tiêu" subtitle="Quản lý chi tiêu nhóm"/>

      <div className="p-4 md:p-6 space-y-4 animate-fade-in">
        {/* Create group button */}
        <Button className="w-full h-12 rounded-xl gradient-primary border-0 text-white gap-2 shadow-md">
          <Plus className="h-5 w-5"/> Tạo nhóm mới
        </Button>

        {/* Group list */}
        <div className="space-y-3">
          {mockGroups.map((group, index) => {
            const balances = getGroupBalances(group.id);
            const myBalance = balances.find((b) => b.memberName === "Đức Huỳnh");
            const totalDebt = balances
              .filter((b) => b.balance < 0)
              .reduce((sum, b) => sum + Math.abs(b.balance), 0);

            return (
              <Link key={group.id} href={`/groups/${group.id}`}>
                <Card className="border-0 shadow-sm card-hover cursor-pointer stagger-item mb-3"
                      style={{animationDelay: `${index * 80}ms`}}>
                  <CardContent className="p-4">
                    <div className="flex items-start gap-3">
                      <div className="h-12 w-12 rounded-2xl gradient-primary flex items-center justify-center shrink-0">
                        <Users className="h-6 w-6 text-white"/>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1">
                          <h3 className="text-base font-semibold truncate">{group.name}</h3>
                          <ArrowRight className="h-4 w-4 text-muted-foreground shrink-0"/>
                        </div>
                        <div className="flex items-center gap-2 mb-2">
                          <Badge variant="secondary" className="text-[10px] px-1.5 py-0 h-4">
                            {group.members.length} thành viên
                          </Badge>
                          {group.members.filter((m) => m.isGhost).length > 0 && (
                            <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4">
                              {group.members.filter((m) => m.isGhost).length} khách
                            </Badge>
                          )}
                        </div>

                        {/* Members avatars */}
                        <div className="flex items-center gap-1.5 mb-2">
                          {group.members.slice(0, 4).map((m, i) => (
                            <div
                              key={m.id}
                              className="h-7 w-7 rounded-full bg-muted flex items-center justify-center text-[10px] font-bold text-muted-foreground border-2 border-card"
                              style={{marginLeft: i > 0 ? "-6px" : "0"}}
                              title={m.displayName}
                            >
                              {m.displayName.charAt(0)}
                            </div>
                          ))}
                          {group.members.length > 4 && (
                            <span className="text-xs text-muted-foreground ml-1">
                              +{group.members.length - 4}
                            </span>
                          )}
                        </div>

                        {/* My balance */}
                        {myBalance && (
                          <div className="flex items-center gap-1.5">
                            <span className="text-xs text-muted-foreground">Bạn:</span>
                            <span
                              className={`text-sm font-semibold tabular-nums ${
                                myBalance.balance > 0
                                  ? "text-success"
                                  : myBalance.balance < 0
                                    ? "text-destructive"
                                    : "text-muted-foreground"
                              }`}
                            >
                              {myBalance.balance > 0
                                ? `được trả ${new Intl.NumberFormat("vi-VN").format(myBalance.balance)}₫`
                                : myBalance.balance < 0
                                  ? `nợ ${new Intl.NumberFormat("vi-VN").format(Math.abs(myBalance.balance))}₫`
                                  : "Hòa"}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      </div>
    </>
  );
}
