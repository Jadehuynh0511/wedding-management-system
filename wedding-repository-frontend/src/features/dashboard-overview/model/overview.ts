export type HallAvailability = {
  total: number;
  available: number;
};

export type MonthlyStats = {
  totalWeddingsThisMonth: number;
  revenueThisMonth: number;
  pendingPaymentCount: number;
  hallAvailability: HallAvailability;
};

export type DailyRevenuePoint = {
  reportDate: string;
  revenue: number;
};

export type UpcomingWedding = {
  id: string;
  brideName: string;
  groomName: string;
  hallName: string;
  weddingDate: string;
  shiftName: string;
  status: "DA_DAT_COC" | "CHO_THANH_TOAN" | "DA_THANH_TOAN";
};

export const WEDDING_STATUS_CONFIG: Record<
  UpcomingWedding["status"],
  { label: string; className: string }
> = {
  DA_DAT_COC: { label: "Đã đặt cọc", className: "bg-amber-50 text-amber-700" },
  CHO_THANH_TOAN: { label: "Chờ thanh toán", className: "bg-blue-50 text-blue-700" },
  DA_THANH_TOAN: { label: "Đã thanh toán", className: "bg-green-50 text-green-700" },
};
