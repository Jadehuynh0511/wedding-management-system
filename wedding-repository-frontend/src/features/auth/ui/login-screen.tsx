import { Heart, ShieldCheck } from "lucide-react";

import { LoginForm } from "@/features/auth/ui/login-form";

type LoginScreenProps = {
  redirectTo: string;
};

export function LoginScreen({ redirectTo }: LoginScreenProps) {
  return (
    <main className="relative min-h-screen overflow-hidden bg-[#f4efeb]">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(204,127,138,0.14),_transparent_34%),radial-gradient(circle_at_bottom,_rgba(255,255,255,0.9),_transparent_42%)]" />

      <div className="relative flex min-h-screen items-center justify-center px-6 py-10">
        <div className="w-full max-w-md">
          <div className="rounded-[2rem] border border-[#eaded8] bg-white px-6 py-7 shadow-[0_24px_70px_rgba(127,85,92,0.12)] sm:px-8 sm:py-8">
            <div className="flex justify-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#cb7d89] text-white shadow-[0_12px_24px_rgba(203,125,137,0.32)]">
                <Heart className="h-5 w-5 fill-current" />
              </div>
            </div>

            <div className="mt-4 text-center">
              <h1 className="font-[family-name:var(--font-display)] text-xl font-semibold uppercase tracking-[0.08em] text-slate-800">
                Quản Lý Tiệc Cưới
              </h1>
              <div className="mt-2 flex items-center gap-3 text-[#b79b93]">
                <span className="h-px flex-1 bg-current/50" />
                <p className="text-[11px] font-medium tracking-[0.03em] text-[#a98c83]">
                  Trung tâm Tiệc cưới Hồng Phúc
                </p>
                <span className="h-px flex-1 bg-current/50" />
              </div>
            </div>

            <div className="mt-6 rounded-[1.4rem] border border-[#f0e4df] bg-[#fffdfc] p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-lg font-semibold uppercase tracking-[0.04em] text-slate-800">
                    Đăng nhập hệ thống
                  </p>
                </div>
                <div className="rounded-full border border-[#f1e3de] bg-white p-2 text-[#cb7d89]">
                  <ShieldCheck className="h-4 w-4" />
                </div>
              </div>

              <div className="mt-5">
                <LoginForm redirectTo={redirectTo} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
