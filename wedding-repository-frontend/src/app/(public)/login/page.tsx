import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { getOptionalAuthenticatedSession, isSafeRelativePath } from "@/features/auth/lib/server-session";
import { DEFAULT_POST_LOGIN_PATH } from "@/features/auth/model/auth-session";
import { LoginScreen } from "@/features/auth/ui/login-screen";

type LoginPageProps = {
  searchParams?: {
    redirect?: string | string[];
  };
};

export const metadata: Metadata = {
  title: "Đăng nhập | Wedding Management",
  description: "Đăng nhập hệ thống quản lý tiệc cưới."
};

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const redirectTo = resolveRedirectTarget(searchParams?.redirect);
  const session = await getOptionalAuthenticatedSession();

  if (session) {
    redirect(redirectTo);
  }

  return <LoginScreen redirectTo={redirectTo} />;
}

function resolveRedirectTarget(value?: string | string[]) {
  const target = Array.isArray(value) ? value[0] : value;

  if (!target || !isSafeRelativePath(target)) {
    return DEFAULT_POST_LOGIN_PATH;
  }

  return target;
}
