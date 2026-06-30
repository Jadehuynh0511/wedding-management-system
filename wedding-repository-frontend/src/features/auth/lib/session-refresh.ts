import {
  buildAuthForwardedHeaders,
  extractSessionTokenPayload,
  parseJsonPayload,
  type JsonPayload,
  type SessionTokenPayload,
} from "@/features/auth/lib/auth-proxy";
import { getBackendApiBaseUrl } from "@/shared/config/api";

export type RefreshSessionOutcome =
  | {
      status: "success";
      payload: JsonPayload;
      httpStatus: number;
      sessionTokens: SessionTokenPayload;
    }
  | {
      status: "invalid" | "unavailable";
      payload: JsonPayload;
      httpStatus: number;
    }
  | {
      status: "unreadable";
    };

const inflightRefresh = new Map<string, Promise<RefreshSessionOutcome>>();
const recentRefresh = new Map<string, { outcome: RefreshSessionOutcome; at: number }>();
const RECENT_REFRESH_TTL_MS = 15_000;

export async function refreshSessionTokens(
  requestHeaders: Headers,
  refreshToken: string,
): Promise<RefreshSessionOutcome> {
  pruneRecentRefresh();

  const cached = recentRefresh.get(refreshToken);
  if (cached) {
    return cached.outcome;
  }

  const inflight = inflightRefresh.get(refreshToken);
  if (inflight) {
    return inflight;
  }

  const promise = exchangeRefreshToken(requestHeaders, refreshToken)
    .then((outcome) => {
      recentRefresh.set(refreshToken, {
        outcome,
        at: Date.now(),
      });
      return outcome;
    })
    .finally(() => {
      inflightRefresh.delete(refreshToken);
    });

  inflightRefresh.set(refreshToken, promise);
  return promise;
}

export function getCookieValueFromHeader(cookieHeader: string | null, targetName: string) {
  if (!cookieHeader) {
    return null;
  }

  for (const segment of cookieHeader.split(";")) {
    const trimmedSegment = segment.trim();
    if (!trimmedSegment) {
      continue;
    }

    const separatorIndex = trimmedSegment.indexOf("=");
    const rawName = separatorIndex >= 0 ? trimmedSegment.slice(0, separatorIndex) : trimmedSegment;
    const rawValue = separatorIndex >= 0 ? trimmedSegment.slice(separatorIndex + 1) : "";

    if (safeDecodeURIComponent(rawName) !== targetName) {
      continue;
    }

    return safeDecodeURIComponent(rawValue);
  }

  return null;
}

async function exchangeRefreshToken(
  requestHeaders: Headers,
  refreshToken: string,
): Promise<RefreshSessionOutcome> {
  try {
    const backendResponse = await fetch(`${getBackendApiBaseUrl()}/api/auth/refresh`, {
      method: "POST",
      headers: buildAuthForwardedHeaders(requestHeaders, true),
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });

    const payload = await parseJsonPayload(backendResponse);

    if (!payload) {
      return {
        status: "unreadable",
      };
    }

    if (backendResponse.status === 401) {
      return {
        status: "invalid",
        payload,
        httpStatus: backendResponse.status,
      };
    }

    if (!backendResponse.ok) {
      return {
        status: "unavailable",
        payload,
        httpStatus: backendResponse.status,
      };
    }

    const sessionTokens = extractSessionTokenPayload(payload);

    if (!sessionTokens) {
      return {
        status: "unreadable",
      };
    }

    return {
      status: "success",
      payload,
      httpStatus: backendResponse.status,
      sessionTokens,
    };
  } catch {
    return {
      status: "unavailable",
      payload: {
        code: "AUTH_SERVICE_UNAVAILABLE",
        message: "The authentication service is unavailable right now.",
        data: null,
      },
      httpStatus: 502,
    };
  }
}

function pruneRecentRefresh() {
  const now = Date.now();

  for (const [token, entry] of recentRefresh) {
    if (now - entry.at > RECENT_REFRESH_TTL_MS) {
      recentRefresh.delete(token);
    }
  }
}

function safeDecodeURIComponent(value: string) {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}
