export type Shift = {
  id: number;
  shiftName: string;
  startTime: string; // "HH:mm"
  endTime: string;   // "HH:mm"
  description: string | null;
};

export type CreateShiftPayload = {
  shiftName: string;
  startTime: string;
  endTime: string;
  description?: string;
};

export type UpdateShiftPayload = CreateShiftPayload;
