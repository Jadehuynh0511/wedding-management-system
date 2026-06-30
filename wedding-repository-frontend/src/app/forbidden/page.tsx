import Link from "next/link";
import { ShieldAlert } from "lucide-react";

import { Button } from "@/shared/ui/button";

export default function ForbiddenPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[linear-gradient(180deg,_#f8f7f4_0%,_#f4efe9_100%)] px-6 py-10">
      <section className="w-full max-w-xl rounded-[2rem] border border-slate-200 bg-white/90 p-8 text-center shadow-sm backdrop-blur">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-100 text-rose-600">
          <ShieldAlert className="h-6 w-6" />
        </div>
        <p className="mt-6 text-sm font-semibold uppercase tracking-[0.24em] text-rose-500">403 Access denied</p>
        <h1 className="mt-4 text-3xl font-semibold tracking-tight text-slate-950">
          This account is not allowed to open that page.
        </h1>
        <p className="mt-4 text-sm leading-7 text-slate-600">
          The session is valid, but the current backend RBAC data does not grant permission to view
          the requested screen.
        </p>
        <div className="mt-8 flex flex-wrap justify-center gap-3">
          <Button asChild>
            <Link href="/dashboard">Back to dashboard</Link>
          </Button>
          <Button asChild variant="outline">
            <Link href="/login">Sign in with another account</Link>
          </Button>
        </div>
      </section>
    </main>
  );
}
