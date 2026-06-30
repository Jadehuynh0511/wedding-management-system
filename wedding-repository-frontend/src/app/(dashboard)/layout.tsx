import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { AuthProvider } from "@/features/auth/ui/auth-provider";
import { DashboardShell } from "@/widgets/dashboard-shell/ui/dashboard-shell";

export default async function DashboardLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  const session = await requireAuthenticatedSession();

  return (
    <AuthProvider initialSession={session}>
      <DashboardShell>{children}</DashboardShell>
    </AuthProvider>
  );
}
