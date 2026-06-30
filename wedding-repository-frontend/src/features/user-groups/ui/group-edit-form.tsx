"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft, Info } from "lucide-react";
import type { UserGroup } from "@/features/user-groups/model/user-group";

type GroupEditFormProps = {
  group: UserGroup;
  groupIndex: number;
};

export function GroupEditForm({ group, groupIndex }: GroupEditFormProps) {
  const router = useRouter();

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">SỬA NHÓM NGƯỜI DÙNG</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Cập nhật thông tin nhóm NG{String(groupIndex).padStart(2, "0")} — {group.name}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/groups")}
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      <div className="rounded-xl border border-gray-100 bg-white p-6">
        <div className="mb-5 flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">1</span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Thông tin nhóm</h2>
        </div>

        <div className="space-y-4 max-w-xl">
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
              Tên nhóm <span className="text-rose-500">*</span>
            </label>
            <input
              defaultValue={group.name}
              disabled
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
            />
          </div>
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mô tả</label>
            <textarea
              defaultValue={group.description}
              disabled
              rows={3}
              className="w-full resize-none rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
            />
          </div>
        </div>

        {/* Notice: backend không hỗ trợ update nhóm */}
        <div className="mt-4 flex items-start gap-2 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 max-w-xl">
          <Info className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-blue-500" />
          <p className="text-[12px] text-blue-700">
            Thông tin nhóm hệ thống không thể chỉnh sửa trực tiếp. Để thay đổi quyền của nhóm, vui lòng sử dụng chức năng <span className="font-semibold">Phân quyền</span>.
          </p>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            onClick={() => router.push("/dashboard/groups")}
            className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            onClick={() => router.push(`/dashboard/groups/${group.id}/permissions`)}
            className="rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
          >
            Đến trang Phân quyền
          </button>
        </div>
      </div>
    </div>
  );
}