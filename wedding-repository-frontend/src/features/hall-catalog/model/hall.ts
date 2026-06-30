export type HallStatus = "TRONG" | "DANG_DUNG" | "BAO_TRI";

export type Hall = {
  id: number;
  hallTypeId: number;
  hallTypeName: string;
  minimumTablePrice: number;
  hallName: string;
  maxCapacity: number;
  tablePrice: number;
  status: HallStatus;
  description: string | null;
};

export type CreateHallPayload = {
  hallTypeId: number;
  hallName: string;
  maxCapacity: number;
  tablePrice: number;
  status: HallStatus;
  description?: string;
};

export type UpdateHallPayload = CreateHallPayload;

export const HALL_STATUS_CONFIG: Record<HallStatus, { label: string; className: string }> = {
  TRONG: { label: "Trống", className: "bg-green-50 text-green-700" },
  DANG_DUNG: { label: "Đang dùng", className: "bg-blue-50 text-blue-700" },
  BAO_TRI: { label: "Bảo trì", className: "bg-gray-100 text-gray-500" }
};

export const HALL_STATUS_OPTIONS: { value: HallStatus; label: string }[] = [
  { value: "TRONG", label: "Trống" },
  { value: "DANG_DUNG", label: "Đang dùng" },
  { value: "BAO_TRI", label: "Bảo trì" }
];