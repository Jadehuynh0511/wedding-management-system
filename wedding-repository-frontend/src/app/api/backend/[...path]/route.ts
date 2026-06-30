import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

import {
  clearAuthSessionCookies,
  setAuthSessionCookies,
  type SessionTokenPayload,
} from "@/features/auth/lib/auth-proxy";
import { refreshSessionTokens } from "@/features/auth/lib/session-refresh";
import {
  AUTH_ACCESS_TOKEN_COOKIE_NAME,
  AUTH_REFRESH_TOKEN_COOKIE_NAME,
} from "@/features/auth/model/auth-session";
import { getBackendApiBaseUrl } from "@/shared/config/api";

/**
 * Request đi qua BFF proxy khi request xuất phát từ browser/client component gọi backendRequest() nhánh client để gọi dữ liệu nghiệp vụ từ BE.
 * Không đi qua BFF proxy khi:
 *  - Request xuất phát từ server component (do server component gọi trực tiếp đến BE thông qua backendRequest ở nhánh server)
 *  - Request là auth (public route) như login, refresh, me
 *  - Request chỉ đi qua middleware để chặn/redirect, kh forward business API
 *
 * - Trình duyệt không cầm access token; token nằm trong cookie httpOnly, chỉ được sử dụng bởi server khi cần đính kèm vào request.
 * - Proxy đọc cookie, gắn Bearer, rồi forward sang backend.
 * - Nếu backend trả 401 và còn refresh token:
 *   refresh -> retry 1 lần -> set cookie mới.
 * - Nếu refresh fail thật:
 *   trả 401 + xóa cookie để client quay về login.
 */

export async function GET(request: NextRequest, context: { params: { path: string[] } }) {
  return handleProxyRequest(request, context.params.path);
}

export async function POST(request: NextRequest, context: { params: { path: string[] } }) {
  return handleProxyRequest(request, context.params.path);
}

export async function PUT(request: NextRequest, context: { params: { path: string[] } }) {
  return handleProxyRequest(request, context.params.path);
}

export async function PATCH(request: NextRequest, context: { params: { path: string[] } }) {
  return handleProxyRequest(request, context.params.path);
}

export async function DELETE(request: NextRequest, context: { params: { path: string[] } }) {
  return handleProxyRequest(request, context.params.path);
}

async function handleProxyRequest(request: NextRequest, pathSegments: string[]) {
  const routePath = `/${pathSegments.join("/")}`;
  const accessToken = request.cookies.get(AUTH_ACCESS_TOKEN_COOKIE_NAME)?.value ?? null;
  const refreshToken = request.cookies.get(AUTH_REFRESH_TOKEN_COOKIE_NAME)?.value ?? null;

  logBackendProxyEvent("request", {
    method: request.method,
    routePath,
    search: request.nextUrl.search,
    hasAccessToken: Boolean(accessToken),
    hasRefreshToken: Boolean(refreshToken),
  });

  if (!accessToken && !refreshToken) {
    logBackendProxyEvent("reject_unauthenticated", {
      method: request.method,
      routePath,
      reason: "missing_access_and_refresh_token",
    });
    return buildUnauthorizedResponse("Bạn cần đăng nhập để tiếp tục.");
  }

  const targetUrl = `${getBackendApiBaseUrl()}/api/${pathSegments
    .map(encodeURIComponent)
    .join("/")}${request.nextUrl.search}`;

  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  const bodyText = hasBody ? await request.text() : undefined;

  logBackendProxyEvent("forward_initial", {
    method: request.method,
    routePath,
    targetUrl,
    usingAccessToken: Boolean(accessToken),
  });

  let backendResponse = await forwardToBackend(request, targetUrl, accessToken, bodyText);

  if (backendResponse.status === 401 && refreshToken) {
    logBackendProxyEvent("backend_401_refresh_needed", {
      method: request.method,
      routePath,
      targetUrl,
    });

    const refreshOutcome = await refreshSessionTokens(request.headers, refreshToken);

    if (refreshOutcome.status === "success") {
      logBackendProxyEvent("refresh_success_retry", {
        method: request.method,
        routePath,
        targetUrl,
      });

      backendResponse = await forwardToBackend(
        request,
        targetUrl,
        refreshOutcome.sessionTokens.accessToken,
        bodyText,
      );

      if (backendResponse.status === 401) {
        logBackendProxyEvent("retry_still_401_clear_session", {
          method: request.method,
          routePath,
          targetUrl,
        });
        return finalizeResponse(backendResponse, { clearCookies: true });
      }

      logBackendProxyEvent("retry_success_set_session", {
        method: request.method,
        routePath,
        targetUrl,
        responseStatus: backendResponse.status,
      });

      return finalizeResponse(backendResponse, {
        sessionTokens: refreshOutcome.sessionTokens,
      });
    }

    if (refreshOutcome.status === "invalid") {
      logBackendProxyEvent("refresh_invalid_clear_session", {
        method: request.method,
        routePath,
        targetUrl,
      });
      return buildUnauthorizedResponse("Phiên đăng nhập đã hết hạn.", {
        clearCookies: true,
      });
    }

    logBackendProxyEvent("refresh_unavailable_return_original_401", {
      method: request.method,
      routePath,
      targetUrl,
    });
    return finalizeResponse(backendResponse);
  }

  logBackendProxyEvent("forward_complete", {
    method: request.method,
    routePath,
    targetUrl,
    responseStatus: backendResponse.status,
  });

  return finalizeResponse(backendResponse);
}

async function forwardToBackend(
  request: NextRequest,
  targetUrl: string,
  accessToken: string | null,
  bodyText: string | undefined,
) {
  const headers = new Headers();
  headers.set("Accept", "application/json");

  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers.set("Content-Type", contentType);
  }

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const forwardedFor = request.headers.get("x-forwarded-for") || request.headers.get("x-real-ip");
  if (forwardedFor) {
    headers.set("X-Forwarded-For", forwardedFor);
  }

  const userAgent = request.headers.get("user-agent");
  if (userAgent) {
    headers.set("User-Agent", userAgent);
  }

  return fetch(targetUrl, {
    method: request.method,
    headers,
    body: bodyText,
    cache: "no-store",
  });
}

async function finalizeResponse(
  backendResponse: Response,
  options: { sessionTokens?: SessionTokenPayload; clearCookies?: boolean } = {},
) {
  const bodyText = await backendResponse.text();
  const response = new NextResponse(bodyText, {
    status: backendResponse.status,
    headers: {
      "Content-Type": backendResponse.headers.get("content-type") ?? "application/json",
    },
  });

  if (options.sessionTokens) {
    setAuthSessionCookies(response, options.sessionTokens);
  } else if (options.clearCookies) {
    clearAuthSessionCookies(response);
  }

  return response;
}

function buildUnauthorizedResponse(message: string, options: { clearCookies?: boolean } = {}) {
  const response = NextResponse.json(
    {
      code: "UNAUTHORIZED",
      message,
      data: null,
    },
    { status: 401 },
  );

  if (options.clearCookies) {
    clearAuthSessionCookies(response);
  }

  return response;
}

function logBackendProxyEvent(event: string, details: Record<string, unknown>) {
  if (process.env.NODE_ENV === "production") {
    return;
  }

  console.info("[auth:bff-proxy]", event, details);
}
