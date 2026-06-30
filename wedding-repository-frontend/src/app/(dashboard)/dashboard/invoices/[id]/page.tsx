import { notFound, redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchInvoiceDetail } from "@/features/invoice/lib/invoice-api";
import { InvoiceDetailView } from "@/features/invoice/ui/invoice-detail-view";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";

type InvoiceDetailPageProps = {
  params: { id: string };
  searchParams?: {
    print?: string;
  };
};

export default async function InvoiceDetailPage({
  params,
  searchParams,
}: InvoiceDetailPageProps) {
  const invoiceId = Number(params.id);
  if (!Number.isFinite(invoiceId) || invoiceId <= 0) notFound();

  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/invoices");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  let detail;
  try {
    detail = await fetchInvoiceDetail(invoiceId, accessToken);
  } catch {
    notFound();
  }

  return <InvoiceDetailView detail={detail} autoPrint={searchParams?.print === "1"} />;
}
