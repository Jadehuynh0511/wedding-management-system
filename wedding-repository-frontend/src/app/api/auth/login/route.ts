import { NextResponse } from "next/server";

import type { LoginRequest } from "@/features/auth/model/auth-contracts";
import { buildAuthForwardedHeaders, clearAuthSessionCookies, extractSessionTokenPayload, parseJsonPayload, setAuthSessionCookies } from "@/features/auth/lib/auth-proxy";
import { getBackendApiBaseUrl } from "@/shared/config/api";

export async function POST(request: Request) {
  let payload: LoginRequest;

  try {
    payload = (await request.json()) as LoginRequest;
  } catch {
    return NextResponse.json(
      {
        code: "INVALID_REQUEST",
        message: "Login payload must be valid JSON.",
        data: null
      },
      { status: 400 }
    );
  }

  try {
    const backendResponse = await fetch(`${getBackendApiBaseUrl()}/api/auth/login`, {
      method: "POST",
      headers: buildAuthForwardedHeaders(request.headers, true),
      body: JSON.stringify(payload),
      cache: "no-store"
    });

    const backendPayload = await parseJsonPayload(backendResponse);

    if (!backendPayload) {
      return NextResponse.json(
        {
          code: "UPSTREAM_ERROR",
          message: "Authentication service returned an unreadable response.",
          data: null
        },
        { status: 502 }
      );
    }

    if (!backendResponse.ok) {
      const errorResponse = NextResponse.json(backendPayload, {
        status: backendResponse.status
      });

      clearAuthSessionCookies(errorResponse);
      return errorResponse;
    }

    const sessionTokens = extractSessionTokenPayload(backendPayload);

    if (!sessionTokens) {
      return NextResponse.json(
        {
          code: "UPSTREAM_ERROR",
          message: "Authentication service returned an incomplete login payload.",
          data: null
        },
        { status: 502 }
      );
    }

    const response = NextResponse.json(
      {
        code: backendPayload.code,
        message: backendPayload.message,
        data: {
          expiresAt: sessionTokens.expiresAt,
          refreshExpiresAt: sessionTokens.refreshExpiresAt
        }
      },
      {
        status: backendResponse.status
      }
    );

    setAuthSessionCookies(response, sessionTokens);
    return response;
  } catch {
    return NextResponse.json(
      {
        code: "AUTH_SERVICE_UNAVAILABLE",
        message: "The authentication service is unavailable right now.",
        data: null
      },
      { status: 502 }
    );
  }
}
