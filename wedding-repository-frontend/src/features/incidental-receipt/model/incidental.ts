export type IncidentalServiceOption = {
  id: number;
  serviceName: string;
  unitName: string;
  currentPrice: number;
};

export type IncidentalReceiptLine = {
  serviceId: number;
  serviceName: string;
  unitName: string;
  quantity: number;
  unitPrice: number;
  notes: string;
};

export type IncidentalReceiptFormState = {
  bookingId: number;
  bookingCode: string;
  brideName: string;
  groomName: string;
  createdDate: string;
  creatorName: string;
  notes: string;
  lines: IncidentalReceiptLine[];
};

export const EMPTY_INCIDENTAL_FORM_STATE: IncidentalReceiptFormState = {
  bookingId: 0,
  bookingCode: "",
  brideName: "",
  groomName: "",
  createdDate: "",
  creatorName: "",
  notes: "",
  lines: []
};

export function calcIncidentalTotal(lines: IncidentalReceiptLine[]) {
  return lines.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0);
}

