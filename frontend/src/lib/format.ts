// Currency formatting utilities for VNĐ
// All amounts stored as integers (BIGINT in VNĐ)

/**
 * Format a VNĐ amount to display string
 * @param amount - Amount in VNĐ (integer)
 * @param compact - If true, use compact notation (e.g., 1.5M)
 */
export function formatCurrency(amount: number, compact = false): string {
  if (compact) {
    if (Math.abs(amount) >= 1_000_000_000) {
      return `${(amount / 1_000_000_000).toFixed(1)}B`;
    }
    if (Math.abs(amount) >= 1_000_000) {
      return `${(amount / 1_000_000).toFixed(1)}M`;
    }
    if (Math.abs(amount) >= 1_000) {
      return `${(amount / 1_000).toFixed(0)}K`;
    }
  }

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Format a date string for display
 */
export function formatDate(
  dateStr: string,
  format: "short" | "long" | "relative" = "short"
): string {
  const date = new Date(dateStr);
  const now = new Date();

  if (format === "relative") {
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return "Hôm nay";
    if (diffDays === 1) return "Hôm qua";
    if (diffDays < 7) return `${diffDays} ngày trước`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} tuần trước`;
    return formatDate(dateStr, "short");
  }

  if (format === "long") {
    return date.toLocaleDateString("vi-VN", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
      timeZone: "Asia/Ho_Chi_Minh",
    });
  }

  return date.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    timeZone: "Asia/Ho_Chi_Minh",
  });
}

/**
 * Format a month string (YYYY-MM) for display
 */
export function formatMonth(monthStr: string): string {
  const [year, month] = monthStr.split("-").map(Number);
  const date = new Date(year, month - 1);
  return date.toLocaleDateString("vi-VN", {
    month: "long",
    year: "numeric",
    timeZone: "Asia/Ho_Chi_Minh",
  });
}

/**
 * Get today's date string in YYYY-MM-DD format
 */
export function getToday(): string {
  return new Date()
    .toLocaleDateString("en-CA", {timeZone: "Asia/Ho_Chi_Minh"});
}

/**
 * Get current month string in YYYY-MM format
 */
export function getCurrentMonth(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

/**
 * Calculate percentage
 */
export function calcPercentage(current: number, total: number): number {
  if (total === 0) return 0;
  return Math.round((current / total) * 100);
}

/**
 * Get budget status color based on percentage
 */
export function getBudgetStatusColor(percentage: number): string {
  if (percentage >= 100) return "text-destructive";
  if (percentage >= 80) return "text-warning";
  return "text-success";
}

/**
 * Get budget status bg color based on percentage
 */
export function getBudgetStatusBg(percentage: number): string {
  if (percentage >= 100) return "bg-destructive";
  if (percentage >= 80) return "bg-warning";
  return "bg-success";
}
