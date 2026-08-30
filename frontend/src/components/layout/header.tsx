"use client";

import {Bell, Search} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {ThemeToggle} from "./theme-toggle";
import {getRecentNotifications, getUnreadCount} from "@/mock/notifications";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from "@/components/ui/dropdown-menu";

interface HeaderProps {
  title: string;
  subtitle?: string;
}

export function Header({title, subtitle}: HeaderProps) {
  const unreadCount = getUnreadCount();
  const recentNotifs = getRecentNotifications(3);

  return (
    <header className="sticky top-0 z-40 bg-background/80 backdrop-blur-xl border-b border-border">
      <div className="flex items-center justify-between px-4 md:px-6 py-3">
        {/* Title section */}
        <div>
          <h1 className="text-xl md:text-2xl font-bold tracking-tight text-foreground">
            {title}
          </h1>
          {subtitle && (
            <p className="text-sm text-muted-foreground mt-0.5">{subtitle}</p>
          )}
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          {/* Search (desktop only) */}
          <div className="hidden lg:flex relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
            <Input
              placeholder="Tìm kiếm..."
              className="pl-9 h-9 w-64 bg-muted/50 border-0 rounded-xl"
            />
          </div>

          {/* Search icon (mobile) */}
          <Button variant="ghost" size="icon" className="h-9 w-9 rounded-xl lg:hidden">
            <Search className="h-4 w-4"/>
          </Button>

          {/* Notifications */}
          <DropdownMenu>
            <DropdownMenuTrigger
              className="inline-flex items-center justify-center h-9 w-9 rounded-xl hover:bg-accent transition-colors relative cursor-pointer">
              <Bell className="h-4 w-4"/>
              {unreadCount > 0 && (
                <span
                  className="absolute -top-0.5 -right-0.5 h-4 w-4 rounded-full bg-destructive text-[10px] font-bold text-white flex items-center justify-center">
                    {unreadCount}
                  </span>
              )}
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-80">
              <div className="px-3 py-2 border-b border-border">
                <p className="text-sm font-semibold">Thông báo</p>
              </div>
              {recentNotifs.map((notif) => (
                <DropdownMenuItem key={notif.id} className="flex flex-col items-start gap-1 py-3 cursor-pointer">
                  <p className="text-sm leading-snug">{notif.content}</p>
                  <p className="text-[11px] text-muted-foreground">
                    {new Date(notif.createdAt).toLocaleTimeString("vi-VN", {hour: "2-digit", minute: "2-digit"})}
                  </p>
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Theme toggle (mobile) */}
          <div className="md:hidden">
            <ThemeToggle/>
          </div>
        </div>
      </div>
    </header>
  );
}
