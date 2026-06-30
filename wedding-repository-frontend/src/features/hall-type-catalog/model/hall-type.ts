export type HallType = {
  id: number;
  hallTypeName: string;
  minimumTablePrice: number;
  description: string | null;
};

export type CreateHallTypePayload = {
  hallTypeName: string;
  minimumTablePrice: number;
  description?: string;
};

export type UpdateHallTypePayload = CreateHallTypePayload;

export type Hall = {
  id: number;
  hallTypeId: number;
  hallTypeName: string;
  minimumTablePrice: number;
  hallName: string;
  maxCapacity: number;
  tablePrice: number;
  status: "TRONG" | "DANG_DUNG" | "BAO_TRI";
  description: string | null;
};
