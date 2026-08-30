import {Receipt} from "@/lib/types";

export const mockReceipts: Receipt[] = [
  {
    id: "rcp-001", userId: "user-001",
    imagePath: "/receipts/receipt-001.jpg",
    status: "CONFIRMED",
    parsedData: {
      items: [
        {name: "Áo thun Uniqlo", price: 350000, categoryId: "cat-04"},
      ],
      total: 350000,
      date: "2026-08-29",
    },
    confirmedData: {
      items: [
        {name: "Áo thun Uniqlo", price: 350000, categoryId: "cat-04"},
      ],
      total: 350000,
      date: "2026-08-29",
    },
    totalAmount: 350000,
    receiptDate: "2026-08-29",
    createdAt: "2026-08-29T19:00:00+07:00",
    updatedAt: "2026-08-29T19:05:00+07:00",
  },
  {
    id: "rcp-002", userId: "user-001",
    imagePath: "/receipts/receipt-002.jpg",
    status: "CONFIRMED",
    parsedData: {
      items: [
        {name: "Giày Nike Air Max", price: 1200000, categoryId: "cat-04"},
      ],
      total: 1200000,
      date: "2026-08-24",
    },
    confirmedData: {
      items: [
        {name: "Giày Nike Air Max", price: 1200000, categoryId: "cat-04"},
      ],
      total: 1200000,
      date: "2026-08-24",
    },
    totalAmount: 1200000,
    receiptDate: "2026-08-24",
    createdAt: "2026-08-24T15:00:00+07:00",
    updatedAt: "2026-08-24T15:05:00+07:00",
  },
  {
    id: "rcp-003", userId: "user-001",
    imagePath: "/receipts/receipt-003.jpg",
    status: "PARSED",
    parsedData: {
      items: [
        {name: "Thịt bò Úc 500g", price: 195000, categoryId: null},
        {name: "Rau muống 2 bó", price: 20000, categoryId: null},
        {name: "Trứng gà 10 quả", price: 35000, categoryId: null},
        {name: "Nước mắm Nam Ngư", price: 28000, categoryId: null},
        {name: "Gạo ST25 5kg", price: 120000, categoryId: null},
      ],
      total: 398000,
      date: "2026-08-30",
    },
    confirmedData: null,
    totalAmount: 398000,
    receiptDate: "2026-08-30",
    createdAt: "2026-08-30T09:30:00+07:00",
    updatedAt: "2026-08-30T09:31:00+07:00",
  },
  {
    id: "rcp-004", userId: "user-001",
    imagePath: "/receipts/receipt-004.jpg",
    status: "PROCESSING",
    parsedData: null,
    confirmedData: null,
    totalAmount: null,
    receiptDate: null,
    createdAt: "2026-08-30T10:00:00+07:00",
    updatedAt: "2026-08-30T10:00:00+07:00",
  },
];

export function getPendingReceipts(): Receipt[] {
  return mockReceipts.filter((r) => r.status === "PARSED");
}

export function getProcessingReceipts(): Receipt[] {
  return mockReceipts.filter((r) => r.status === "PROCESSING");
}
