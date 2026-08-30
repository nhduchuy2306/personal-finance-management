"use client";

import {Header} from "@/components/layout/header";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Separator} from "@/components/ui/separator";
import {Bell, Camera, Check, LogOut, MessageCircle, Palette, Unlink, User,} from "lucide-react";
import {mockUser} from "@/mock/users";
import {ThemeToggle} from "@/components/layout/theme-toggle";

export default function SettingsPage() {
  return (
    <>
      <Header title="Cài đặt"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in max-w-2xl">
        {/* Profile */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <User className="h-4 w-4 text-primary"/> Hồ sơ cá nhân
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Avatar */}
            <div className="flex items-center gap-4">
              <div className="relative">
                <div
                  className="h-16 w-16 rounded-2xl gradient-primary flex items-center justify-center text-white text-xl font-bold">
                  {mockUser.displayName.charAt(0)}
                </div>
                <button
                  className="absolute -bottom-1 -right-1 h-6 w-6 rounded-full bg-primary flex items-center justify-center text-primary-foreground shadow">
                  <Camera className="h-3 w-3"/>
                </button>
              </div>
              <div>
                <p className="text-lg font-semibold">{mockUser.displayName}</p>
                <p className="text-sm text-muted-foreground">{mockUser.email}</p>
              </div>
            </div>

            <Separator/>

            {/* Edit fields */}
            <div className="space-y-3">
              <div>
                <Label className="text-xs text-muted-foreground">Tên hiển thị</Label>
                <Input defaultValue={mockUser.displayName} className="mt-1 rounded-xl"/>
              </div>
              <div>
                <Label className="text-xs text-muted-foreground">Email</Label>
                <Input defaultValue={mockUser.email} className="mt-1 rounded-xl" disabled/>
              </div>
              <Button className="rounded-xl gradient-primary border-0 text-white">
                Lưu thay đổi
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Telegram */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <MessageCircle className="h-4 w-4 text-blue-500"/> Telegram
            </CardTitle>
          </CardHeader>
          <CardContent>
            {mockUser.telegramChatId ? (
              <div className="flex items-center justify-between p-3 rounded-xl bg-blue-500/10">
                <div className="flex items-center gap-2">
                  <Check className="h-4 w-4 text-blue-500"/>
                  <div>
                    <p className="text-sm font-medium">Đã kết nối</p>
                    <p className="text-xs text-muted-foreground">Chat ID: {mockUser.telegramChatId}</p>
                  </div>
                </div>
                <Button variant="ghost" size="sm" className="text-destructive rounded-xl gap-1">
                  <Unlink className="h-3.5 w-3.5"/> Ngắt kết nối
                </Button>
              </div>
            ) : (
              <div className="text-center py-4">
                <p className="text-sm text-muted-foreground mb-3">
                  Kết nối Telegram để nhận thông báo chi tiêu, hóa đơn, tiết kiệm
                </p>
                <Button className="rounded-xl bg-blue-500 hover:bg-blue-600 text-white gap-2">
                  <MessageCircle className="h-4 w-4"/> Kết nối Telegram
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Notifications */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Bell className="h-4 w-4 text-primary"/> Thông báo
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {[
              {label: "Cảnh báo ngân sách", desc: "Khi chi tiêu đạt 80% và 100%", defaultOn: true},
              {label: "Hóa đơn sắp hạn", desc: "Nhắc trước 3 ngày", defaultOn: true},
              {label: "Nhắc tiết kiệm", desc: "Nhắc đóng góp hàng ngày/tuần", defaultOn: true},
              {label: "Chia tiền nhóm", desc: "Khi có chi tiêu mới trong nhóm", defaultOn: false},
            ].map((item, i) => (
              <div key={i} className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">{item.label}</p>
                  <p className="text-xs text-muted-foreground">{item.desc}</p>
                </div>
                <div className="h-6 w-11 rounded-full bg-primary/20 relative cursor-pointer">
                  <div
                    className={`absolute top-0.5 h-5 w-5 rounded-full transition-all ${item.defaultOn ? "right-0.5 bg-primary" : "left-0.5 bg-muted-foreground"}`}/>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>

        {/* Appearance */}
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Palette className="h-4 w-4 text-primary"/> Giao diện
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium">Chế độ màu</p>
                <p className="text-xs text-muted-foreground">Sáng / Tối / Hệ thống</p>
              </div>
              <ThemeToggle/>
            </div>
          </CardContent>
        </Card>

        {/* Danger zone */}
        <Card className="border-0 shadow-sm">
          <CardContent className="p-4">
            <Button variant="ghost" className="w-full justify-start text-destructive gap-2 rounded-xl">
              <LogOut className="h-4 w-4"/> Đăng xuất
            </Button>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
