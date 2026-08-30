"use client";

import {Header} from "@/components/layout/header";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {AlertTriangle, Camera, Check, Edit3, FileImage, Loader2, Upload, X} from "lucide-react";
import {CurrencyDisplay} from "@/components/shared/currency-display";
import {mockReceipts} from "@/mock/receipts";
import {getCategoryById, getExpenseCategories} from "@/mock/categories";
import {formatDate} from "@/lib/format";
import {cn} from "@/lib/utils";

const statusConfig: Record<string, { label: string; color: string; icon: React.ElementType }> = {
  PROCESSING: {label: "Đang xử lý", color: "bg-blue-500/15 text-blue-600", icon: Loader2},
  PARSED: {label: "Đã nhận dạng", color: "bg-amber-500/15 text-amber-600", icon: Edit3},
  CONFIRMED: {label: "Đã xác nhận", color: "bg-green-500/15 text-green-600", icon: Check},
  FAILED: {label: "Thất bại", color: "bg-red-500/15 text-red-600", icon: AlertTriangle},
  DISCARDED: {label: "Đã hủy", color: "bg-gray-500/15 text-gray-600", icon: X},
};

export default function ReceiptsPage() {
  const categories = getExpenseCategories();

  return (
    <>
      <Header title="Hóa đơn / Biên lai" subtitle="Scan & quản lý hóa đơn"/>

      <div className="p-4 md:p-6 space-y-4 md:space-y-6 animate-fade-in">
        {/* Upload zone */}
        <Card
          className="border-2 border-dashed border-primary/30 bg-primary/5 hover:bg-primary/10 transition-colors cursor-pointer">
          <CardContent className="p-8 text-center">
            <div className="mx-auto h-16 w-16 rounded-2xl gradient-primary flex items-center justify-center mb-4">
              <Upload className="h-8 w-8 text-white"/>
            </div>
            <h3 className="text-lg font-semibold mb-1">Tải lên hóa đơn</h3>
            <p className="text-sm text-muted-foreground mb-4">
              Kéo thả file hoặc nhấn để chọn ảnh hóa đơn
            </p>
            <div className="flex justify-center gap-3">
              <Button variant="outline" className="rounded-xl gap-2">
                <FileImage className="h-4 w-4"/> Chọn ảnh
              </Button>
              <Button variant="outline" className="rounded-xl gap-2 md:hidden">
                <Camera className="h-4 w-4"/> Chụp ảnh
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Receipt list */}
        <div className="space-y-4">
          {mockReceipts.map((receipt, index) => {
            const config = statusConfig[receipt.status];
            const StatusIcon = config.icon;
            const isParsed = receipt.status === "PARSED";

            return (
              <Card
                key={receipt.id}
                className={cn("border-0 shadow-sm stagger-item", isParsed && "ring-2 ring-amber-500/30")}
                style={{animationDelay: `${index * 80}ms`}}
              >
                <CardContent className="p-4">
                  {/* Header */}
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Badge className={cn("gap-1 text-[10px]", config.color)}>
                        <StatusIcon className={cn("h-3 w-3", receipt.status === "PROCESSING" && "animate-spin")}/>
                        {config.label}
                      </Badge>
                      {receipt.receiptDate && (
                        <span className="text-xs text-muted-foreground">
                          {formatDate(receipt.receiptDate)}
                        </span>
                      )}
                    </div>
                    {receipt.totalAmount && (
                      <CurrencyDisplay amount={receipt.totalAmount} size="lg"/>
                    )}
                  </div>

                  {/* Parsed items */}
                  {receipt.parsedData && (
                    <div className="space-y-2 mb-3">
                      {receipt.parsedData.items.map((item, i) => (
                        <div
                          key={i}
                          className="flex items-center gap-3 py-2 px-3 rounded-lg bg-muted/40"
                        >
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium truncate">{item.name}</p>
                            {isParsed && (
                              <Select defaultValue={item.categoryId || ""}>
                                <SelectTrigger
                                  className="h-7 w-[140px] text-[11px] mt-1 rounded-lg border-0 bg-background">
                                  <SelectValue placeholder="Chọn danh mục"/>
                                </SelectTrigger>
                                <SelectContent>
                                  {categories.map((c) => (
                                    <SelectItem key={c.id} value={c.id} className="text-xs">{c.name}</SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            )}
                            {item.categoryId && !isParsed && (
                              <span className="text-[11px] text-muted-foreground">
                                {getCategoryById(item.categoryId)?.name}
                              </span>
                            )}
                          </div>
                          <CurrencyDisplay amount={item.price} size="sm" className="font-semibold shrink-0"/>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Processing state */}
                  {receipt.status === "PROCESSING" && (
                    <div className="flex items-center justify-center py-6 gap-2 text-muted-foreground">
                      <Loader2 className="h-5 w-5 animate-spin"/>
                      <span className="text-sm">Đang nhận dạng hóa đơn...</span>
                    </div>
                  )}

                  {/* Actions for PARSED receipts */}
                  {isParsed && (
                    <div className="flex gap-2 pt-2">
                      <Button className="flex-1 rounded-xl gradient-primary border-0 text-white gap-1.5" size="sm">
                        <Check className="h-4 w-4"/> Xác nhận
                      </Button>
                      <Button variant="outline" className="rounded-xl gap-1.5" size="sm">
                        Chia nhóm
                      </Button>
                      <Button variant="ghost" size="sm" className="rounded-xl text-destructive">
                        <X className="h-4 w-4"/>
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>
    </>
  );
}
