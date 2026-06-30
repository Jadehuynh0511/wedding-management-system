import { cache } from "react";
import { headers } from "next/headers";
import { redirect } from "next/navigation";

import type { CurrentUserApiResponse } from "@/features/auth/model/auth-contracts";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME, DEFAULT_POST_LOGIN_PATH } from "@/features/auth/model/auth-session";
import type { AuthenticatedSession } from "@/entities/session/model/authenticated-session";
import { getBackendApiBaseUrl } from "@/shared/config/api";

type SessionLookupResult =
  | {
      status: "authenticated";
      session: AuthenticatedSession;
    }
  | {
      status: "missing" | "invalid";
      session: null;
    }
  | {
      status: "unavailable";
      session: null;
      message: string;
    };

const fetchCurrentUserByAccessToken = cache(async (accessToken: string): Promise<SessionLookupResult> => {
  try {
    const response = await fetch(`${getBackendApiBaseUrl()}/api/auth/me`, {
      headers: {
        Authorization: `Bearer ${accessToken}`
      },
      cache: "no-store"
    });

    if (response.status === 401) {
      return {
        status: "invalid",
        session: null
      };
    }

    if (!response.ok) {
      return {
        status: "unavailable",
        session: null,
        message: "Authentication service rejected the session lookup."
      };
    }

    const payload = (await response.json()) as CurrentUserApiResponse;

    if (payload.code !== "SUCCESS" || !payload.data) {
      return {
        status: "unavailable",
        session: null,
        message: "Authentication service returned an unexpected session payload."
      };
    }

    return {
      status: "authenticated",
      session: {
        id: payload.data.id,
        username: payload.data.username,
        groupName: payload.data.groupName,
        permissionCodes: payload.data.permissionCodes
      }
    };
  } catch {
    return {
      status: "unavailable",
      session: null,
      message: "Authentication service is unavailable."
    };
  }
});

export async function getSessionLookupResult(): Promise<SessionLookupResult> {
  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME);

  if (!accessToken) {
    return {
      status: "missing",
      session: null
    };
  }

  return fetchCurrentUserByAccessToken(accessToken);
}

export async function getOptionalAuthenticatedSession() {
  const result = await getSessionLookupResult();

  return result.status === "authenticated" ? result.session : null;
}

export async function requireAuthenticatedSession() {
  const result = await getSessionLookupResult();

  if (result.status === "authenticated") {
    return result.session;
  }

  if (result.status === "unavailable") {
    throw new Error(result.message);
  }

  redirect(buildLoginHref(getCurrentRequestPath()));
}

export function buildLoginHref(redirectTo?: string) {
  if (!redirectTo || !isSafeRelativePath(redirectTo)) {
    return "/login";
  }

  const searchParams = new URLSearchParams({
    redirect: redirectTo
  });

  return `/login?${searchParams.toString()}`;
}

export function getCurrentRequestPath() {
  const pathname = headers().get("x-request-path");
  const search = headers().get("x-request-search") ?? "";

  if (!pathname || !isSafeRelativePath(pathname)) {
    return DEFAULT_POST_LOGIN_PATH;
  }

  return `${pathname}${search}`;
}

export function isSafeRelativePath(pathname: string) {
  return pathname.startsWith("/") && !pathname.startsWith("//");
}
