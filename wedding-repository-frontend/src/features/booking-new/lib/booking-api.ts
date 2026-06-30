import { backendRequest } from "@/shared/api/backend-client";
import type { AvailableHall, BookingFormState, PaymentMethod } from "@/features/booking-new/model/booking";

export async function fetchAvailableHalls(
  date: string,
  shiftId: number,
  accessToken?: string,
): Promise<AvailableHall[]> {
  return backendRequest<AvailableHall[]>(
    `/halls/availability?date=${encodeURIComponent(date)}&shiftId=${shiftId}`,
    { accessToken },
    "Không thể tải danh sách sảnh trống.",
  );
}

export async function createBooking(state: BookingFormState): Promise<{ id: number }> {
  const payload = {
    hallId: parseInt(state.hallId),
    shiftId: parseInt(state.shiftId),
    groomName: state.groomName.trim(),
    brideName: state.brideName.trim(),
    groomPhoneNumber: state.groomPhoneNumber.trim() || null,
    bridePhoneNumber: state.bridePhoneNumber.trim(),
    celebrationDate: state.celebrationDate,
    tableCount: parseInt(state.tableCount),
    reservedTableCount: parseInt(state.reservedTableCount) || 0,
    notes: state.notes.trim() || null,
    menuItems: state.menuItems.map((m) => ({
      menuItemId: m.menuItemId,
      quantity: m.quantity,
      notes: m.notes || null,
    })),
    services: state.services.map((s) => ({
      serviceId: s.serviceId,
      quantity: s.quantity,
      notes: s.notes || null,
    })),
    depositReceipt: {
      amount: parseFloat(state.depositAmount),
      paymentMethod: state.paymentMethod as PaymentMethod,
      notes: null,
    },
  };

  return backendRequest<{ id: number }>(
    "/bookings",
    { method: "POST", jsonBody: payload },
    "Không thể tạo đặt tiệc.",
  );
}
