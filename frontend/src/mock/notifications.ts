import {NotificationLog} from "@/lib/types";

export const mockNotifications: NotificationLog[] = [
  {
    id: "notif-001", userId: "user-001", type: "BUDGET_WARNING",
    channel: "TELEGRAM", content: "⚠️ Chi tiêu Ăn uống hôm nay đã đạt 85% giới hạn (102,000₫ / 120,000₫)",
    entityId: "cat-01", status: "SENT", errorMessage: null,
    createdAt: "2026-08-30T12:30:00+07:00",
  },
  {
    id: "notif-002", userId: "user-001", type: "BILL_DUE_SOON",
    channel: "TELEGRAM", content: "📋 Tiền nhà sẽ đến hạn trong 6 ngày (05/09/2026) — ước tính 5,000,000₫",
    entityId: "bill-004", status: "SENT", errorMessage: null,
    createdAt: "2026-08-30T08:00:00+07:00",
  },
  {
    id: "notif-003", userId: "user-001", type: "SAVING_REMINDER",
    channel: "TELEGRAM", content: "💰 Đừng quên tiết kiệm hôm nay! Quỹ khẩn cấp — cần 100,000₫/ngày",
    entityId: "sg-003", status: "SENT", errorMessage: null,
    createdAt: "2026-08-30T08:00:00+07:00",
  },
  {
    id: "notif-004", userId: "user-001", type: "BUDGET_CRITICAL",
    channel: "TELEGRAM", content: "🚨 Chi tiêu Mua sắm đã vượt 103% giới hạn tháng (1,550,000₫ / 1,500,000₫)",
    entityId: "cat-04", status: "SENT", errorMessage: null,
    createdAt: "2026-08-29T19:00:00+07:00",
  },
  {
    id: "notif-005", userId: "user-001", type: "SAVING_BEHIND",
    channel: "TELEGRAM", content: "📉 Du lịch Nhật Bản đang chậm tiến độ! Hiện tại: 16% — Kỳ vọng: 22%",
    entityId: "sg-002", status: "SENT", errorMessage: null,
    createdAt: "2026-08-28T09:00:00+07:00",
  },
];

export function getRecentNotifications(count: number): NotificationLog[] {
  return [...mockNotifications]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, count);
}

export function getUnreadCount(): number {
  // Mock: count notifications from today
  const today = "2026-08-30";
  return mockNotifications.filter((n) => n.createdAt.startsWith(today)).length;
}
