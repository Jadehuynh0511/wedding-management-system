import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

const protectedPagePrefixes = ["/dashboard"];
const protectedPagePaths = new Set(["/rbac-foundation"]);

/**
 * Middleware này đóng vai trò là entry point cho mọi request.
 * Request đi vào, check xem có phải protected page kh, nếu có thì check access token,
 * nếu token kh hợp lệ thì redirect về route /api/auth/refresh?... để refresh token rồi
 * quay về trang này với token mới. Nếu lúc này token vẫn kh hợp lệ thì redirect về login page,
 * còn hợp lệ thì cho qua.
 */

export async function middleware(request: NextRequest) {
  const { pathname, search } = request.nextUrl;
  const requestHeaders = new Headers(request.headers);
  const accessToken = request.cookies.get("wm_access_token")?.value;
  const accessTokenState = getAccessTokenState(accessToken);
  const protectedPage = isProtectedPage(pathname);

  requestHeaders.set("x-request-path", pathname);
  requestHeaders.set("x-request-search", search);

  logMiddlewareEvent("request", {
    pathname,
    search,
    protectedPage,
    accessTokenState,
  });

  if (protectedPage && accessTokenState !== "valid") {
    const refreshUrl = new URL("/api/auth/refresh", request.url);
    refreshUrl.searchParams.set("redirect", `${pathname}${search}`);
    logMiddlewareEvent("redirect_to_refresh", {
      pathname,
      search,
      reason: `access_token_${accessTokenState}`,
      redirectTo: `${refreshUrl.pathname}${refreshUrl.search}`,
    });
    return NextResponse.redirect(refreshUrl);
  }

  logMiddlewareEvent("pass_through", {
    pathname,
    search,
    protectedPage,
  });

  return NextResponse.next({
    request: {
      headers: requestHeaders,
    },
  });
}

function isProtectedPage(pathname: string) {
  if (protectedPagePaths.has(pathname)) {
    return true;
  }

  return protectedPagePrefixes.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

function getAccessTokenState(accessToken?: string) {
  if (!accessToken) {
    return "missing" as const;
  }

  const expiresAt = readJwtExpiry(accessToken);

  if (!expiresAt) {
    return "invalid" as const;
  }

  if (expiresAt <= Date.now()) {
    return "invalid" as const;
  }

  return "valid" as const;
}

function readJwtExpiry(token: string) {
  const segments = token.split(".");
  if (segments.length < 2) {
    return null;
  }

  try {
    const payload = JSON.parse(decodeBase64Url(segments[1])) as { exp?: number };

    if (typeof payload.exp !== "number") {
      return null;
    }

    return payload.exp * 1000;
  } catch {
    return null;
  }
}

function decodeBase64Url(value: string) {
  const normalizedValue = value.replace(/-/g, "+").replace(/_/g, "/");
  const paddingLength = (4 - (normalizedValue.length % 4)) % 4;
  return atob(`${normalizedValue}${"=".repeat(paddingLength)}`);
}

function logMiddlewareEvent(event: string, details: Record<string, unknown>) {
  if (process.env.NODE_ENV === "production") {
    return;
  }

  console.info("[auth:middleware]", event, details);
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
};
