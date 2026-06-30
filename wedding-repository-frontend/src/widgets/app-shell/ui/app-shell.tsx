import { ReactNode } from "react";

export type AppShellProps = {
  children: ReactNode;
  badge?: string;
  eyebrow?: string;
  heading: string;
  description: string;
  actions?: ReactNode;
};

export function AppShell({
  children,
  badge = "develop branch baseline",
  eyebrow = "Wedding Management",
  heading,
  description,
  actions
}: AppShellProps) {
  return (
    <main className="mx-auto min-h-screen max-w-7xl px-6 py-10 sm:px-8 lg:px-12">
      <header className="mb-10 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-sky-700">{eyebrow}</p>
          <h1 className="mt-3 max-w-3xl font-[family-name:var(--font-display)] text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
            {heading}
          </h1>
          <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-600 sm:text-base">{description}</p>
        </div>
        <div className="flex flex-col items-start gap-3 sm:flex-row sm:items-center">
          {actions}
          <div className="rounded-full border border-slate-200 bg-white/80 px-4 py-2 text-sm text-slate-600">
            {badge}
          </div>
        </div>
      </header>
      {children}
    </main>
  );
}
