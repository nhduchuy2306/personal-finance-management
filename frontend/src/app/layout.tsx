import type {Metadata} from "next";
import {Inter} from "next/font/google";
import "./globals.css";
import {ThemeProvider} from "@/components/providers/theme-provider";
import {TooltipProvider} from "@/components/ui/tooltip";
import {Sidebar} from "@/components/layout/sidebar";
import {MobileNav} from "@/components/layout/mobile-nav";

const inter = Inter({
  variable: "--font-sans",
  subsets: ["latin", "vietnamese"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "FinanceFlow — Quản lý tài chính cá nhân",
  description:
    "Hệ thống quản lý tài chính cá nhân thông minh: theo dõi chi tiêu, ngân sách, chia tiền nhóm, hóa đơn định kỳ và mục tiêu tiết kiệm.",
  keywords: [
    "quản lý tài chính",
    "personal finance",
    "ngân sách",
    "chi tiêu",
    "tiết kiệm",
  ],
};

export default function RootLayout({children}: { children: React.ReactNode }) {
  return (
    <html lang="vi" className={`${inter.variable} h-full antialiased`} suppressHydrationWarning>
    <body className="min-h-full bg-background">
    <ThemeProvider>
      <TooltipProvider>
        <div className="flex min-h-screen">
          {/* Desktop sidebar */}
          <Sidebar/>

          {/* Main content */}
          <main className="flex-1 min-w-0 pb-20 md:pb-0">
            {children}
          </main>

          {/* Mobile bottom nav */}
          <MobileNav/>
        </div>
      </TooltipProvider>
    </ThemeProvider>
    </body>
    </html>
  );
}
