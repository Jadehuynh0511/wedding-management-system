import { NextResponse } from "next/server";

import type { LoginResponse } from "@/features/auth/model/auth-contracts";
import {
  AUTH_ACCESS_TOKEN_COOKIE_NAME,
  AUTH_REFRESH_TOKEN_COOKIE_NAME,
} from "@/features/auth/model/auth-session";

export type JsonPayload = Record<string, unknown> | null;
export type SessionTokenPayload = Pick<
  LoginResponse,
  "accessToken" | "expiresAt" | "refreshToken" | "refreshExpiresAt"
>;

export async function parseJsonPayload(response: Response): Promise<JsonPayload> {
  const rawBody = await response.text();

  if (!rawBody) {
    return null;
  }

  try {
    return JSON.parse(rawBody) as JsonPayload;
  } catch {
    return null;
  }
}

export function extractSessionTokenPayload(payload: JsonPayload): SessionTokenPayload | null {
  const data = payload?.data;

  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return null;
  }

  const sessionData = data as Record<string, unknown>;
  const accessToken = typeof sessionData.accessToken === "string" ? sessionData.accessToken : null;
  const expiresAt = typeof sessionData.expiresAt === "string" ? sessionData.expiresAt : null;
  const refreshToken =
    typeof sessionData.refreshToken === "string" ? sessionData.refreshToken : null;
  const refreshExpiresAt =
    typeof sessionData.refreshExpiresAt === "string" ? sessionData.refreshExpiresAt : null;

  if (!accessToken || !expiresAt || !refreshToken || !refreshExpiresAt) {
    return null;
  }

  return {
    accessToken,
    expiresAt,
    refreshToken,
    refreshExpiresAt,
  };
}

export function buildAuthForwardedHeaders(requestHeaders: Headers, includeJsonContentType = false) {
  const headers = new Headers();

  if (includeJsonContentType) {
    headers.set("Content-Type", "application/json");
  }

  headers.set("Accept", "application/json");

  const forwardedFor = requestHeaders.get("x-forwarded-for") || requestHeaders.get("x-real-ip");
  if (forwardedFor) {
    headers.set("X-Forwarded-For", forwardedFor);
  }

  const userAgent = requestHeaders.get("user-agent");
  if (userAgent) {
    headers.set("User-Agent", userAgent);
  }

  return headers;
}

export function setAuthSessionCookies(response: NextResponse, sessionTokens: SessionTokenPayload) {
  response.cookies.set(
    AUTH_ACCESS_TOKEN_COOKIE_NAME,
    sessionTokens.accessToken,
    getAuthCookieOptions(sessionTokens.expiresAt),
  );
  response.cookies.set(
    AUTH_REFRESH_TOKEN_COOKIE_NAME,
    sessionTokens.refreshToken,
    getAuthCookieOptions(sessionTokens.refreshExpiresAt),
  );
}

export function clearAuthSessionCookies(response: NextResponse) {
  response.cookies.delete(AUTH_ACCESS_TOKEN_COOKIE_NAME);
  response.cookies.delete(AUTH_REFRESH_TOKEN_COOKIE_NAME);
}

function getAuthCookieOptions(expiresAt: string) {
  const expires = new Date(expiresAt);
  const maxAgeInSeconds = Math.max(0, Math.floor((expires.getTime() - Date.now()) / 1000));

  return {
    expires: Number.isNaN(expires.getTime()) ? undefined : expires,
    maxAge: Number.isNaN(expires.getTime()) ? undefined : maxAgeInSeconds,
    httpOnly: true, // Cookie httpOnly để trình duyệt không bao giờ truy cập được token, chỉ gửi kèm trong request.
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    priority: "high" as const,
  };
}
