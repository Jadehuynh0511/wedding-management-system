import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import {
  clearAuthSessionCookies,
  parseJsonPayload,
  setAuthSessionCookies,
  type JsonPayload,
  type SessionTokenPayload,
} from "@/features/auth/lib/auth-proxy";
import {
  refreshSessionTokens,
  type RefreshSessionOutcome,
} from "@/features/auth/lib/session-refresh";
import {
  AUTH_ACCESS_TOKEN_COOKIE_NAME,
  AUTH_REFRESH_TOKEN_COOKIE_NAME,
} from "@/features/auth/model/auth-session";
import { getBackendApiBaseUrl } from "@/shared/config/api";

type CurrentUserFetchResult =
  | {
      status: "success";
      payload: JsonPayload;
      httpStatus: number;
    }
  | {
      status: "unauthorized";
      payload: JsonPayload;
      httpStatus: number;
    }
  | {
      status: "error";
      payload: JsonPayload;
      httpStatus: number;
    }
  | {
      status: "unreadable";
    };

export async function GET(request: Request) {
  const accessToken = cookies().get(AUTH_ACCESS_TOKEN_COOKIE_NAME)?.value;
  const refreshToken = cookies().get(AUTH_REFRESH_TOKEN_COOKIE_NAME)?.value;

  if (!accessToken && !refreshToken) {
    const response = NextResponse.json(
      {
        code: "UNAUTHORIZED",
        message: "You need to sign in before loading the current user.",
        data: null,
      },
      { status: 401 },
    );

    clearAuthSessionCookies(response);
    return response;
  }

  let sessionTokens: SessionTokenPayload | null = null;
  let currentAccessToken = accessToken ?? null;

  if (!currentAccessToken && refreshToken) {
    const refreshResult = await refreshSessionTokens(request.headers, refreshToken);

    if (refreshResult.status !== "success") {
      return buildFailedRefreshResponse(refreshResult);
    }

    sessionTokens = refreshResult.sessionTokens;
    currentAccessToken = refreshResult.sessionTokens.accessToken;
  }

  const currentUserResult = await fetchCurrentUser(currentAccessToken!);

  if (currentUserResult.status === "success") {
    return buildCurrentUserResponse(currentUserResult.payload, currentUserResult.httpStatus, sessionTokens);
  }

  if (currentUserResult.status === "unauthorized" && refreshToken) {
    const refreshResult = await refreshSessionTokens(request.headers, refreshToken);

    if (refreshResult.status !== "success") {
      return buildFailedRefreshResponse(refreshResult);
    }

    const retriedCurrentUserResult = await fetchCurrentUser(refreshResult.sessionTokens.accessToken);

    if (retriedCurrentUserResult.status !== "success") {
      return buildFailedCurrentUserResponse(retriedCurrentUserResult);
    }

    return buildCurrentUserResponse(
      retriedCurrentUserResult.payload,
      retriedCurrentUserResult.httpStatus,
      refreshResult.sessionTokens,
    );
  }

  return buildFailedCurrentUserResponse(currentUserResult);
}

async function fetchCurrentUser(accessToken: string): Promise<CurrentUserFetchResult> {
  try {
    const backendResponse = await fetch(`${getBackendApiBaseUrl()}/api/auth/me`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        Accept: "application/json",
      },
      cache: "no-store",
    });

    const backendPayload = await parseJsonPayload(backendResponse);

    if (!backendPayload) {
      return {
        status: "unreadable",
      };
    }

    if (backendResponse.status === 401) {
      return {
        status: "unauthorized",
        payload: backendPayload,
        httpStatus: 401,
      };
    }

    if (!backendResponse.ok) {
      return {
        status: "error",
        payload: backendPayload,
        httpStatus: backendResponse.status,
      };
    }

    return {
      status: "success",
      payload: backendPayload,
      httpStatus: backendResponse.status,
    };
  } catch {
    return {
      status: "error",
      payload: {
        code: "AUTH_SERVICE_UNAVAILABLE",
        message: "The authentication service is unavailable right now.",
        data: null,
      },
      httpStatus: 502,
    };
  }
}

function buildCurrentUserResponse(
  payload: JsonPayload,
  status: number,
  sessionTokens: SessionTokenPayload | null,
) {
  const response = NextResponse.json(payload, {
    status,
  });

  if (sessionTokens) {
    setAuthSessionCookies(response, sessionTokens);
  }

  return response;
}

function buildFailedCurrentUserResponse(result: Exclude<CurrentUserFetchResult, { status: "success" }>) {
  if (result.status === "unreadable") {
    return NextResponse.json(
      {
        code: "UPSTREAM_ERROR",
        message: "Authentication service returned an unreadable response.",
        data: null,
      },
      { status: 502 },
    );
  }

  const response = NextResponse.json(result.payload, {
    status: result.httpStatus,
  });

  if (result.status === "unauthorized") {
    clearAuthSessionCookies(response);
  }

  return response;
}

function buildFailedRefreshResponse(result: Exclude<RefreshSessionOutcome, { status: "success" }>) {
  if (result.status === "unreadable") {
    return NextResponse.json(
      {
        code: "UPSTREAM_ERROR",
        message: "Authentication service returned an unreadable response.",
        data: null,
      },
      { status: 502 },
    );
  }

  const response = NextResponse.json(result.payload, {
    status: result.httpStatus,
  });

  if (result.status === "invalid") {
    clearAuthSessionCookies(response);
  }

  return response;
}
