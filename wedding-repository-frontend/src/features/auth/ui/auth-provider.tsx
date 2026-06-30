"use client";

import { createContext, startTransition, useContext, useState } from "react";
import { useRouter } from "next/navigation";

import type { AuthenticatedSession } from "@/entities/session/model/authenticated-session";
import type { CurrentUserApiResponse } from "@/features/auth/model/auth-contracts";
import { DEFAULT_POST_LOGIN_PATH } from "@/features/auth/model/auth-session";

type AuthProviderProps = {
  children: React.ReactNode;
  initialSession: AuthenticatedSession;
};

type AuthContextValue = {
  session: AuthenticatedSession | null;
  isRefreshingSession: boolean;
  isLoggingOut: boolean;
  refreshSession: () => Promise<AuthenticatedSession | null>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children, initialSession }: AuthProviderProps) {
  const router = useRouter();
  const [session, setSession] = useState<AuthenticatedSession | null>(initialSession);
  const [isRefreshingSession, setIsRefreshingSession] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  async function refreshSession() {
    setIsRefreshingSession(true);

    try {
      const response = await fetch("/api/auth/me", {
        cache: "no-store"
      });

      const payload = (await response.json()) as CurrentUserApiResponse;

      if (response.status === 401) {
        setSession(null);
        startTransition(() => {
          router.replace(buildClientLoginHref(getCurrentBrowserPath()));
          router.refresh();
        });
        return null;
      }

      if (!response.ok || payload.code !== "SUCCESS" || !payload.data) {
        throw new Error(payload.message || "Không thể đồng bộ phiên đăng nhập hiện tại.");
      }

      const nextSession = {
        id: payload.data.id,
        username: payload.data.username,
        groupName: payload.data.groupName,
        permissionCodes: payload.data.permissionCodes
      } satisfies AuthenticatedSession;

      setSession(nextSession);
      return nextSession;
    } finally {
      setIsRefreshingSession(false);
    }
  }

  async function logout() {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);

    try {
      await fetch("/api/auth/logout", {
        method: "POST",
        cache: "no-store"
      });
    } finally {
      setSession(null);
      startTransition(() => {
        router.replace("/login");
        router.refresh();
      });
      setIsLoggingOut(false);
    }
  }

  return (
    <AuthContext.Provider
      value={{
        session,
        isRefreshingSession,
        isLoggingOut,
        refreshSession,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider.");
  }

  return context;
}

function getCurrentBrowserPath() {
  if (typeof window === "undefined") {
    return DEFAULT_POST_LOGIN_PATH;
  }

  const pathname = window.location.pathname;
  const search = window.location.search;

  if (!pathname.startsWith("/") || pathname.startsWith("//")) {
    return DEFAULT_POST_LOGIN_PATH;
  }

  return `${pathname}${search}`;
}

function buildClientLoginHref(redirectTo?: string) {
  if (!redirectTo || !redirectTo.startsWith("/") || redirectTo.startsWith("//")) {
    return "/login";
  }

  const searchParams = new URLSearchParams({
    redirect: redirectTo
  });

  return `/login?${searchParams.toString()}`;
}
