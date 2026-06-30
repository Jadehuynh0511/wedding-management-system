export type ServiceItemStatus = "HOAT_DONG" | "NGUNG_HOAT_DONG";

export type ServiceItem = {
  id: number;
  serviceName: string;
  serviceCategory: string;
  unitName: string;
  currentPrice: number;
  priceEffectiveFrom: string; // ISO instant
  status: ServiceItemStatus;
  active: boolean;
  description: string | null;
};

export type ServiceItemDetail = ServiceItem & {
  priceHistory: ServicePriceHistory[];
};

export type ServicePriceHistory = {
  id: number;
  serviceItemId: number;
  oldPrice: number;
  effectiveFrom: string;
  effectiveTo: string | null;
};

export type CreateServicePayload = {
  serviceName: string;
  serviceCategory: string;
  unitName: string;
  currentPrice: number;
  status?: ServiceItemStatus;
  description?: string;
};

export type UpdateServicePayload = {
  serviceName: string;
  serviceCategory: string;
  unitName: string;
  status: ServiceItemStatus;
  description?: string;
};

export type UpdateServicePricePayload = {
  newPrice: number;
};

export const SERVICE_STATUS_CONFIG: Record<ServiceItemStatus, { label: string; className: string }> = {
  HOAT_DONG: { label: "Hoạt động", className: "bg-green-50 text-green-700" },
  NGUNG_HOAT_DONG: { label: "Ngừng HĐ", className: "bg-gray-100 text-gray-500" }
};