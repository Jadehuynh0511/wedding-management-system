export type MenuItemStatus = "CON" | "HET";

export type MenuItem = {
  id: number;
  itemName: string;
  itemCategory: string;
  currentPrice: number;
  status: MenuItemStatus;
  available: boolean;
  description: string | null;
};

export type CreateMenuItemPayload = {
  itemName: string;
  itemCategory: string;
  currentPrice: number;
  status: MenuItemStatus;
  description?: string;
};

export type UpdateMenuItemPayload = CreateMenuItemPayload;

export const MENU_ITEM_STATUS_LABEL: Record<MenuItemStatus, { label: string; className: string }> = {
  CON: { label: "Còn", className: "bg-green-50 text-green-700" },
  HET: { label: "Hết", className: "bg-gray-100 text-gray-500" }
};
