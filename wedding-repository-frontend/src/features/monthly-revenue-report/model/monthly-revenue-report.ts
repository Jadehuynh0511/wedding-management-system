export type MonthlyRevenueReportItem = {
  reportDate: string;
  weddingBookingCount: number;
  revenue: number;
  revenueRatio: number;
};

export type MonthlyRevenueReport = {
  reportMonth: number;
  reportYear: number;
  generatedAt: string;
  totalRevenue: number;
  totalWeddingBookings: number;
  items: MonthlyRevenueReportItem[];
};
