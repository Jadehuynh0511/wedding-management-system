import { NextRequest, NextResponse } from "next/server";

import { clearAuthSessionCookies, setAuthSessionCookies } from "@/features/auth/lib/auth-proxy";
import {
  getCookieValueFromHeader,
  refreshSessionTokens,
  type RefreshSessionOutcome,
} from "@/features/auth/lib/session-refresh";
import {
  AUTH_REFRESH_TOKEN_COOKIE_NAME,
  DEFAULT_POST_LOGIN_PATH,
} from "@/features/auth/model/auth-session";

export async function GET(request: NextRequest) {
  const redirectTo = getSafeRedirectPath(request.nextUrl.searchParams.get("redirect"));
  const refreshResult = await refreshFromCookie(request);

  if (refreshResult.status === "success") {
    const response = NextResponse.redirect(new URL(redirectTo, request.url));
    setAuthSessionCookies(response, refreshResult.sessionTokens);
    return response;
  }

  const loginUrl = new URL("/login", request.url);
  loginUrl.searchParams.set("redirect", redirectTo);
  const response = NextResponse.redirect(loginUrl);
  clearAuthSessionCookies(response);
  return response;
}

export async function POST(request: Request) {
  const refreshResult = await refreshFromCookie(request);

  if (refreshResult.status === "missing") {
    const response = NextResponse.json(
      {
        code: "UNAUTHORIZED",
        message: "You need to sign in before refreshing the session.",
        data: null,
      },
      { status: 401 },
    );

    clearAuthSessionCookies(response);
    return response;
  }

  if (refreshResult.status === "unreadable") {
    return NextResponse.json(
      {
        code: "UPSTREAM_ERROR",
        message: "Authentication service returned an unreadable response.",
        data: null,
      },
      { status: 502 },
    );
  }

  if (refreshResult.status === "invalid") {
    const response = NextResponse.json(refreshResult.payload, {
      status: refreshResult.httpStatus,
    });
    clearAuthSessionCookies(response);
    return response;
  }

  if (refreshResult.status === "unavailable") {
    return NextResponse.json(refreshResult.payload, {
      status: refreshResult.httpStatus,
    });
  }

  if (refreshResult.status !== "success") {
    return NextResponse.json(
      {
        code: "UPSTREAM_ERROR",
        message: "Authentication service returned an unexpected refresh result.",
        data: null,
      },
      { status: 502 },
    );
  }

  const { payload, httpStatus, sessionTokens } = refreshResult;
  const responsePayload =
    payload && typeof payload === "object" && !Array.isArray(payload) ? payload : {};
  const response = NextResponse.json(
    {
      code: typeof responsePayload.code === "string" ? responsePayload.code : "SUCCESS",
      message:
        typeof responsePayload.message === "string"
          ? responsePayload.message
          : "Session refreshed successfully.",
      data: {
        expiresAt: sessionTokens.expiresAt,
        refreshExpiresAt: sessionTokens.refreshExpiresAt,
      },
    },
    {
      status: httpStatus,
    },
  );

  setAuthSessionCookies(response, sessionTokens);
  return response;
}

type RefreshResult = { status: "missing" } | RefreshSessionOutcome;

async function refreshFromCookie(request: Request): Promise<RefreshResult> {
  const refreshToken = getCookieValueFromHeader(
    request.headers.get("cookie"),
    AUTH_REFRESH_TOKEN_COOKIE_NAME,
  );

  if (!refreshToken) {
    return { status: "missing" };
  }

  return refreshSessionTokens(request.headers, refreshToken);
}

function getSafeRedirectPath(redirectTo: string | null) {
  if (!redirectTo || !redirectTo.startsWith("/") || redirectTo.startsWith("//")) {
    return DEFAULT_POST_LOGIN_PATH;
  }

  return redirectTo;
}
