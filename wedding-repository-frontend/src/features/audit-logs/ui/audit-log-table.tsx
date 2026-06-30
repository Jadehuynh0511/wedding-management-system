"use client";

import { useState, useTransition } from "react";
import { Search, RefreshCw, ChevronLeft, ChevronRight, CheckCircle2, XCircle } from "lucide-react";

import { cn } from "@/lib/utils";
import { fetchAuditLogs } from "@/features/audit-logs/lib/audit-log-api";
import {
  RESULT_STATUS_CONFIG,
  type AuditLog,
  type AuditLogPage,
  type AuditResultStatus
} from "@/features/audit-logs/model/audit-log";

type AuditLogTableProps = {
  initialData: AuditLogPage;
};

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  });
}

const MODULE_KEY_LABEL: Record<string, string> = {
  AUTH: "Xác thực",
  CATALOG: "Danh mục",
  BOOKING: "Đặt tiệc",
  BILLING: "Thanh toán",
  REPORTING: "Báo cáo",
  SYSTEM: "Hệ thống"
};

const GROUP_LABEL: Record<string, string> = {
  ADMIN: "Quản trị viên",
  STAFF: "Nhân viên"
};

const ACTION_DESCRIPTION_VI: Record<string, string> = {
  // Auth
  LOGIN: "Đăng nhập hệ thống",
  LOGOUT: "Đăng xuất hệ thống",
  REFRESH_TOKEN: "Làm mới phiên đăng nhập",
  // RBAC
  PERMISSION_ASSIGN: "Gán quyền cho nhóm người dùng",
  PERMISSION_REVOKE: "Thu hồi quyền của nhóm người dùng",
  // Catalog
  HALL_TYPE_CREATE: "Tạo mới loại sảnh",
  HALL_TYPE_UPDATE: "Cập nhật loại sảnh",
  HALL_TYPE_DELETE: "Xóa loại sảnh",
  HALL_CREATE: "Tạo mới sảnh tiệc",
  HALL_UPDATE: "Cập nhật thông tin sảnh",
  HALL_DELETE: "Xóa sảnh tiệc",
  SHIFT_CREATE: "Tạo mới ca tiệc",
  SHIFT_UPDATE: "Cập nhật ca tiệc",
  SHIFT_DELETE: "Xóa ca tiệc",
  MENU_ITEM_CREATE: "Tạo mới món ăn",
  MENU_ITEM_UPDATE: "Cập nhật món ăn",
  MENU_ITEM_DELETE: "Xóa món ăn",
  SERVICE_CREATE: "Tạo mới dịch vụ",
  SERVICE_UPDATE: "Cập nhật dịch vụ",
  SERVICE_PRICE_UPDATE: "Cập nhật giá dịch vụ",
  SERVICE_DELETE: "Xóa dịch vụ",
  // Booking
  BOOKING_CREATE: "Tạo đặt tiệc mới",
  BOOKING_CANCEL: "Hủy tiệc cưới",
  DEPOSIT_CREATE: "Lập phiếu thu tiền cọc",
  INCIDENTAL_CREATE: "Lập phiếu dịch vụ phát sinh",
  INVOICE_CREATE: "Lập hóa đơn thanh toán",
  // System
  SYSTEM_RULE_UPDATE: "Cập nhật quy định hệ thống",
  USER_CREATE: "Tạo tài khoản nhân viên",
  USER_UPDATE: "Cập nhật tài khoản nhân viên",
  USER_DELETE: "Vô hiệu hóa tài khoản",
};

export function AuditLogTable({ initialData }: AuditLogTableProps) {
  const [data, setData] = useState<AuditLogPage>(initialData);
  const [username, setUsername] = useState("");
  const [actionCode, setActionCode] = useState("");
  const [resultStatus, setResultStatus] = useState<AuditResultStatus | "">("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [isPending, startTransition] = useTransition();

  async function load(page: number) {
    startTransition(async () => {
      const result = await fetchAuditLogs({
        username: username.trim() || undefined,
        actionCode: actionCode.trim() || undefined,
        resultStatus: resultStatus || undefined,
        from: from ? new Date(from).toISOString() : undefined,
        to: to ? new Date(to).toISOString() : undefined,
        page,
        size: 20
      });
      setData(result);
    });
  }

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    load(0);
  }

  function handleReset() {
    setUsername("");
    setActionCode("");
    setResultStatus("");
    setFrom("");
    setTo("");
    startTransition(async () => {
      const result = await fetchAuditLogs({ page: 0, size: 20 });
      setData(result);
    });
  }

  const startItem = data.totalElements === 0 ? 0 : data.page * data.size + 1;
  const endItem = Math.min((data.page + 1) * data.size, data.totalElements);

  return (
    <div>
      {/* Filter form */}
      <form onSubmit={handleSearch} className="mb-4 rounded-xl border border-gray-100 bg-white p-4">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <div>
            <label className="mb-1 block text-[11px] font-semibold text-gray-500">Người thực hiện</label>
            <div className="flex items-center gap-2 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2">
              <Search className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Tên đăng nhập..."
                className="flex-1 bg-transparent text-[13px] text-gray-700 placeholder:text-gray-400 outline-none"
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold text-gray-500">Mã hành động</label>
            <input
              value={actionCode}
              onChange={(e) => setActionCode(e.target.value)}
              placeholder="VD: PERMISSION_ASSIGN..."
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-700 placeholder:text-gray-400 outline-none focus:border-rose-300 transition-colors"
            />
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold text-gray-500">Kết quả</label>
            <select
              value={resultStatus}
              onChange={(e) => setResultStatus(e.target.value as AuditResultStatus | "")}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 pl-3 pr-8 py-2 text-[13px] text-gray-700 outline-none focus:border-rose-300 transition-colors"
            >
              <option value="">Tất cả</option>
              <option value="SUCCESS">Thành công</option>
              <option value="FAIL">Thất bại</option>
            </select>
          </div>
          <div className="flex items-end gap-2">
            <button
              type="submit"
              disabled={isPending}
              className="flex-1 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors disabled:opacity-50"
            >
              {isPending ? "Đang tải..." : "Tìm kiếm"}
            </button>
            <button
              type="button"
              onClick={handleReset}
              disabled={isPending}
              className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
              <RefreshCw className={cn("h-3.5 w-3.5", isPending && "animate-spin")} />
            </button>
          </div>
        </div>
        <div className="mt-3 grid grid-cols-2 gap-3">
          <div>
            <label className="mb-1 block text-[11px] font-semibold text-gray-500">Từ ngày</label>
            <input
              type="datetime-local"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-700 outline-none focus:border-rose-300 transition-colors"
            />
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold text-gray-500">Đến ngày</label>
            <input
              type="datetime-local"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-700 outline-none focus:border-rose-300 transition-colors"
            />
          </div>
        </div>
      </form>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-40 whitespace-nowrap">Thời gian</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-32 whitespace-nowrap">Người thực hiện</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28 whitespace-nowrap">Module</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Hành động</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mô tả</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-36 whitespace-nowrap">Kết quả</th>
            </tr>
          </thead>
          <tbody>
            {data.items.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-12 text-center text-[13px] text-gray-400">
                  Không có dữ liệu nhật ký
                </td>
              </tr>
            ) : (
              data.items.map((log) => (
                <LogRow
                  key={log.id}
                  log={log}
                  expanded={expandedId === log.id}
                  onToggle={() => setExpandedId(expandedId === log.id ? null : log.id)}
                />
              ))
            )}
          </tbody>
        </table>

        {/* Pagination */}
        <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
          <p className="text-[12px] text-gray-500">
            Hiển thị {startItem}–{endItem} trên {data.totalElements} bản ghi
          </p>
          <div className="flex items-center gap-1">
            <button
              onClick={() => load(data.page - 1)}
              disabled={data.page === 0 || isPending}
              className="rounded-lg border border-gray-200 p-1.5 text-gray-500 hover:bg-gray-50 transition-colors disabled:opacity-40"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span className="px-3 text-[12px] text-gray-600">
              Trang {data.page + 1} / {Math.max(1, data.totalPages)}
            </span>
            <button
              onClick={() => load(data.page + 1)}
              disabled={data.page >= data.totalPages - 1 || isPending}
              className="rounded-lg border border-gray-200 p-1.5 text-gray-500 hover:bg-gray-50 transition-colors disabled:opacity-40"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function LogRow({
  log,
  expanded,
  onToggle
}: {
  log: AuditLog;
  expanded: boolean;
  onToggle: () => void;
}) {
  const status = RESULT_STATUS_CONFIG[log.resultStatus];
  const moduleLabel = MODULE_KEY_LABEL[log.moduleKey] ?? log.moduleKey;
  const groupLabel = GROUP_LABEL[log.actorGroupName] ?? log.actorGroupName;

  return (
    <>
      <tr
        onClick={onToggle}
        className={cn(
          "border-b border-gray-50 cursor-pointer transition-colors",
          expanded ? "bg-rose-50/40" : "hover:bg-gray-50/60"
        )}
      >
        <td className="px-4 py-3 text-[12px] text-gray-600 whitespace-nowrap">
          {formatDateTime(log.occurredAt)}
        </td>
        <td className="px-4 py-3">
          <div>
            <p className="font-medium text-gray-800">{log.actorUsername}</p>
            <p className="text-[11px] text-gray-400">{groupLabel}</p>
          </div>
        </td>
        <td className="px-4 py-3">
          <span className="rounded-md bg-gray-100 px-2 py-0.5 text-[11px] font-semibold text-gray-600">
            {moduleLabel}
          </span>
        </td>
        <td className="px-4 py-3 font-mono text-[12px] text-gray-700">{log.actionCode}</td>
        <td className="px-4 py-3 max-w-xs truncate text-gray-600">
          {ACTION_DESCRIPTION_VI[log.actionCode] ?? log.description ?? "—"}
        </td>
        <td className="px-4 py-3">
          <span className={cn("flex w-fit items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-semibold", status.className)}>
            {log.resultStatus === "SUCCESS"
              ? <CheckCircle2 className="h-3 w-3" />
              : <XCircle className="h-3 w-3" />
            }
            {status.label}
          </span>
        </td>
      </tr>

      {/* Expanded detail row */}
      {expanded && (
        <tr className="border-b border-gray-100 bg-rose-50/20">
          <td colSpan={6} className="px-6 py-4">
            <div className="grid grid-cols-3 gap-4 text-[12px]">
              {log.targetType && (
                <DetailItem label="Loại đối tượng" value={log.targetType} />
              )}
              {log.targetId && (
                <DetailItem label="ID đối tượng" value={log.targetId} />
              )}
              {log.targetLabel && (
                <DetailItem label="Đối tượng" value={log.targetLabel} />
              )}
              {log.errorMessage && (
                <div className="col-span-3">
                  <p className="text-[11px] font-semibold text-rose-500 mb-1">Lỗi</p>
                  <p className="text-rose-700 bg-rose-50 rounded-lg px-3 py-2">{log.errorMessage}</p>
                </div>
              )}
              {log.details && Object.keys(log.details).length > 0 && (
                <div className="col-span-3">
                  <p className="text-[11px] font-semibold text-gray-500 mb-1">Chi tiết</p>
                  <pre className="rounded-lg bg-gray-50 px-3 py-2 text-[11px] text-gray-600 overflow-x-auto">
                    {JSON.stringify(log.details, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          </td>
        </tr>
      )}
    </>
  );
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-[11px] font-semibold text-gray-500 mb-0.5">{label}</p>
      <p className="text-gray-700">{value}</p>
    </div>
  );
}