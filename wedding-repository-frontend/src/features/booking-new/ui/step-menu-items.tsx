"use client";

import { useMemo, useState } from "react";
import { Check, Plus, Search, Trash2, X } from "lucide-react";
import { cn } from "@/lib/utils";
import type { BookingFormState, BookingMenuItem } from "@/features/booking-new/model/booking";

type MenuItem = { id: number; itemName: string; itemCategory: string; currentPrice: number };

type StepMenuItemsProps = {
  state: BookingFormState;
  onChange: (patch: Partial<BookingFormState>) => void;
  availableMenuItems: MenuItem[];
  tableCount: number;
};

function fmt(n: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
}

export function StepMenuItems({
  state,
  onChange,
  availableMenuItems,
  tableCount,
}: StepMenuItemsProps) {
  const [pickerOpen, setPickerOpen] = useState(false);
  const [searchText, setSearchText] = useState("");

  function addItem(menuItem: MenuItem) {
    if (state.menuItems.find((m) => m.menuItemId === menuItem.id)) return;
    onChange({
      menuItems: [
        ...state.menuItems,
        {
          menuItemId: menuItem.id,
          menuItemName: menuItem.itemName,
          itemCategory: menuItem.itemCategory,
          pricePerUnit: menuItem.currentPrice,
          quantity: 1,
          notes: "",
        },
      ],
    });
  }

  function removeItem(menuItemId: number) {
    onChange({ menuItems: state.menuItems.filter((m) => m.menuItemId !== menuItemId) });
  }

  function updateItem(menuItemId: number, patch: Partial<BookingMenuItem>) {
    onChange({
      menuItems: state.menuItems.map((m) => (m.menuItemId === menuItemId ? { ...m, ...patch } : m)),
    });
  }

  const perTableTotal = state.menuItems.reduce((s, m) => s + m.pricePerUnit * m.quantity, 0);
  const grandMenuTotal = perTableTotal * tableCount;

  const selectedItemIds = useMemo(
    () => new Set(state.menuItems.map((item) => item.menuItemId)),
    [state.menuItems],
  );

  const filteredItems = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    if (!keyword) return availableMenuItems;

    return availableMenuItems.filter(
      (item) =>
        item.itemName.toLowerCase().includes(keyword) ||
        item.itemCategory.toLowerCase().includes(keyword),
    );
  }, [availableMenuItems, searchText]);

  const addableItems = availableMenuItems.filter((m) => !selectedItemIds.has(m.id));

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
            3
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Danh sách món ăn
          </h2>
        </div>

        <div className="relative">
          <button
            type="button"
            disabled={addableItems.length === 0}
            onClick={() => setPickerOpen((open) => !open)}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-1.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:cursor-not-allowed disabled:opacity-40"
          >
            <Plus className="h-3.5 w-3.5" />
            Thêm món
          </button>

          {pickerOpen && addableItems.length > 0 && (
            <div className="absolute right-0 top-10 z-30 w-[460px] rounded-xl border border-gray-100 bg-white shadow-xl">
              <div className="border-b border-gray-100 p-3">
                <div className="mb-2 flex items-center justify-between">
                  <p className="text-[12px] font-semibold uppercase tracking-wide text-gray-500">
                    Chọn món ăn
                  </p>
                  <button
                    type="button"
                    onClick={() => setPickerOpen(false)}
                    className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-gray-400" />
                  <input
                    value={searchText}
                    onChange={(e) => setSearchText(e.target.value)}
                    placeholder="Tìm theo tên món hoặc loại món..."
                    className="w-full rounded-lg border border-gray-200 py-2 pl-8 pr-3 text-[12px] text-gray-700 outline-none focus:border-rose-300"
                  />
                </div>
              </div>

              <div className="max-h-72 overflow-y-auto p-2">
                {filteredItems.length === 0 && (
                  <p className="py-6 text-center text-[12px] text-gray-400">
                    Không tìm thấy món nào phù hợp.
                  </p>
                )}

                {filteredItems.map((item) => {
                  const selected = selectedItemIds.has(item.id);

                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => {
                        if (selected) return;
                        addItem(item);
                      }}
                      className={cn(
                        "mb-1 flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left transition-colors",
                        selected
                          ? "cursor-default border-green-200 bg-green-50"
                          : "border-transparent hover:border-rose-200 hover:bg-rose-50",
                      )}
                    >
                      <div>
                        <p className="text-[13px] font-semibold text-gray-800">{item.itemName}</p>
                        <p className="text-[11px] text-gray-400">{item.itemCategory}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-[12px] font-medium text-gray-700">
                          {fmt(item.currentPrice)}
                        </span>
                        {selected && (
                          <span className="inline-flex items-center gap-1 rounded-full border border-green-200 bg-white px-2 py-0.5 text-[10px] font-semibold text-green-600">
                            <Check className="h-3 w-3" />
                            Đã chọn
                          </span>
                        )}
                      </div>
                    </button>
                  );
                })}
              </div>

              <div className="flex items-center justify-between border-t border-gray-100 bg-gray-50 px-3 py-2 text-[11px] text-gray-500">
                <span>Tổng món hiện có: {availableMenuItems.length}</span>
                <span>Đã chọn: {state.menuItems.length}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {state.menuItems.length === 0 ? (
        <p className="py-6 text-center text-[13px] text-gray-400">
          Chưa có món ăn nào. Nhấn "+ Thêm món" để thêm.
        </p>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-[13px]">
              <thead>
                <tr className="border-b border-gray-100 bg-gray-50">
                  <th className="w-16 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    STT
                  </th>
                  <th className="w-20 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Tên món ăn
                  </th>
                  <th className="w-24 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Loại
                  </th>
                  <th className="w-20 whitespace-nowrap px-3 py-2.5 text-center text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Số lượng
                  </th>
                  <th className="w-20 whitespace-nowrap px-3 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Đơn giá
                  </th>
                  <th className="w-24 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Ghi chú
                  </th>
                  <th className="w-8" />
                </tr>
              </thead>
              <tbody>
                {state.menuItems.map((item, i) => (
                  <tr key={item.menuItemId} className="border-b border-gray-50">
                    <td className="whitespace-nowrap px-3 py-2.5 text-gray-500">{i + 1}</td>
                    <td className="whitespace-nowrap px-3 py-2.5 font-medium text-gray-800">
                      {item.menuItemName}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-gray-500">
                      {item.itemCategory}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-center">
                      <input
                        type="number"
                        min={1}
                        value={item.quantity}
                        onChange={(e) => {
                          const parsed = parseInt(e.target.value, 10);
                          if (!Number.isFinite(parsed)) return;
                          updateItem(item.menuItemId, { quantity: Math.max(1, parsed) });
                        }}
                        className="w-16 rounded border border-gray-200 px-2 py-1 text-center text-[12px] text-gray-700 outline-none focus:border-rose-300"
                      />
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-right text-gray-700">
                      {fmt(item.pricePerUnit)}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5">
                      <input
                        value={item.notes}
                        onChange={(e) => updateItem(item.menuItemId, { notes: e.target.value })}
                        placeholder="—"
                        className="w-full rounded border border-transparent bg-transparent px-2 py-1 text-[12px] text-gray-600 outline-none focus:border-gray-200 focus:bg-white"
                      />
                    </td>
                    <td className="px-3 py-2.5">
                      <button
                        type="button"
                        onClick={() => removeItem(item.menuItemId)}
                        className="rounded p-1 text-gray-300 transition-colors hover:bg-rose-50 hover:text-rose-500"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-3 text-[12px]">
            <span className="text-gray-500">Tổng cộng / 1 bàn</span>
            <span className="font-semibold text-gray-700">
              {fmt(perTableTotal)}
              {tableCount > 0 && (
                <span className="ml-2 text-gray-400">
                  = {tableCount} bàn = {fmt(grandMenuTotal)}
                </span>
              )}
            </span>
          </div>
        </>
      )}
    </div>
  );
}
