import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { buildAuthForwardedHeaders, clearAuthSessionCookies } from "@/features/auth/lib/auth-proxy";
import { AUTH_REFRESH_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { getBackendApiBaseUrl } from "@/shared/config/api";

export async function POST(request: Request) {
  const refreshToken = cookies().get(AUTH_REFRESH_TOKEN_COOKIE_NAME)?.value;

  if (refreshToken) {
    try {
      await fetch(`${getBackendApiBaseUrl()}/api/auth/logout`, {
        method: "POST",
        headers: buildAuthForwardedHeaders(request.headers, true),
        body: JSON.stringify({
          refreshToken
        }),
        cache: "no-store"
      });
    } catch {
      // Frontend vẫn xóa local cookies để người dùng thoát khỏi máy hiện tại.
    }
  }

  const response = NextResponse.json({
    code: "SUCCESS",
    message: "Signed out successfully.",
    data: null
  });

  clearAuthSessionCookies(response);
  return response;
}
