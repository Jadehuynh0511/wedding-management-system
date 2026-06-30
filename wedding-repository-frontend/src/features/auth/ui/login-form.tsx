"use client";

import { FormEvent, useEffect, useState } from "react";
import { Eye, EyeOff, LoaderCircle } from "lucide-react";
import { useRouter } from "next/navigation";

import type { LoginMutationApiResponse } from "@/features/auth/model/auth-contracts";
import { AUTH_REMEMBERED_USERNAME_STORAGE_KEY } from "@/features/auth/model/auth-session";
import type { ApiResponse } from "@/shared/api/api-response";
import { Button } from "@/shared/ui/button";

type LoginFormProps = {
  redirectTo: string;
};

type LoginField = "username" | "password";
type LoginFieldErrors = Partial<Record<LoginField, string>>;
type LoginErrorResponse = ApiResponse<Record<string, string> | null>;

const inputBaseClassName =
  "w-full rounded-xl border border-[#efe4df] bg-[#f7f2ef] px-4 py-3 text-sm text-slate-800 outline-none transition placeholder:text-[#b7a29a] focus:border-[#cb7d89] focus:bg-white focus:ring-4 focus:ring-[#f8e3e7]";

export function LoginForm({ redirectTo }: LoginFormProps) {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberUsername, setRememberUsername] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<LoginFieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    const storedUsername = window.localStorage.getItem(AUTH_REMEMBERED_USERNAME_STORAGE_KEY);

    if (storedUsername) {
      setUsername(storedUsername);
      setRememberUsername(true);
    }
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextFieldErrors = validateForm(username, password);
    setFieldErrors(nextFieldErrors);
    setFormError(null);

    if (Object.keys(nextFieldErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          username: username.trim(),
          password
        }),
        cache: "no-store"
      });

      const payload = (await response.json()) as LoginMutationApiResponse | LoginErrorResponse;

      if (!response.ok) {
        const responseFieldErrors = extractFieldErrors(payload);

        if (Object.keys(responseFieldErrors).length > 0) {
          setFieldErrors(responseFieldErrors);
        } else {
          setFormError(payload.message || "Không thể đăng nhập với thông tin hiện tại.");
        }

        if (payload.code === "INVALID_CREDENTIALS") {
          setPassword("");
        }

        return;
      }

      persistRememberedUsername(rememberUsername, username.trim());
      setFieldErrors({});
      setFormError(null);
      router.push(redirectTo);
      router.refresh();
    } catch {
      setFormError("Không thể kết nối tới dịch vụ xác thực. Hãy thử lại sau ít phút.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit} noValidate>
      <div className="space-y-2">
        <label className="text-sm font-semibold text-slate-700" htmlFor="username">
          Tên đăng nhập
        </label>
        <input
          id="username"
          name="username"
          type="text"
          autoFocus
          autoComplete="username"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          value={username}
          onChange={(event) => {
            setUsername(event.target.value);
            clearFieldError("username", setFieldErrors);
          }}
          className={buildInputClassName(fieldErrors.username)}
          placeholder="Nhập tên đăng nhập"
          aria-invalid={Boolean(fieldErrors.username)}
          aria-describedby={fieldErrors.username ? "username-error" : undefined}
        />
        {fieldErrors.username ? (
          <p id="username-error" className="text-xs font-medium text-rose-600">
            {fieldErrors.username}
          </p>
        ) : null}
      </div>

      <div className="space-y-2">
        <label className="text-sm font-semibold text-slate-700" htmlFor="password">
          Mật khẩu
        </label>
        <div className="relative">
          <input
            id="password"
            name="password"
            type={isPasswordVisible ? "text" : "password"}
            autoComplete="current-password"
            value={password}
            onChange={(event) => {
              setPassword(event.target.value);
              clearFieldError("password", setFieldErrors);
            }}
            className={`${buildInputClassName(fieldErrors.password)} pr-12`}
            placeholder="Nhập mật khẩu"
            aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? "password-error" : undefined}
          />
          <button
            type="button"
            onClick={() => setIsPasswordVisible((previousValue) => !previousValue)}
            className="absolute inset-y-0 right-3 inline-flex items-center text-[#b89f98] transition hover:text-[#8e6f68]"
            aria-label={isPasswordVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
          >
            {isPasswordVisible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {fieldErrors.password ? (
          <p id="password-error" className="text-xs font-medium text-rose-600">
            {fieldErrors.password}
          </p>
        ) : null}
      </div>

      <div className="flex items-center justify-between gap-4">
        <label className="inline-flex items-center gap-2.5 text-sm text-[#7f6d67]">
          <input
            type="checkbox"
            checked={rememberUsername}
            onChange={(event) => setRememberUsername(event.target.checked)}
            className="h-4 w-4 rounded border-[#d8c2bb] text-[#cb7d89] focus:ring-[#f3cdd4]"
          />
          Ghi nhớ đăng nhập
        </label>
        <button
          type="button"
          disabled
          className="text-sm text-[#d1a5ad] transition disabled:cursor-not-allowed disabled:opacity-100"
          title="Tính năng này chưa được triển khai ở backend hiện tại."
        >
          Quên mật khẩu?
        </button>
      </div>

      {formError ? (
        <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {formError}
        </div>
      ) : null}

      <Button
        type="submit"
        size="lg"
        className="h-11 w-full rounded-xl bg-[#bf7a84] text-white shadow-[0_10px_20px_rgba(191,122,132,0.22)] hover:bg-[#b26e78]"
        disabled={isSubmitting}
      >
        {isSubmitting ? (
          <>
            <LoaderCircle className="mr-2 h-4 w-4 animate-spin" />
            Đang xác thực...
          </>
        ) : (
          "Đăng nhập"
        )}
      </Button>

      <div className="border-t border-[#f1e5df] pt-3 text-center text-xs text-[#baa49c]">
        Phiên bản 1.0.0 · support@tieccuoi.vn
      </div>
    </form>
  );
}

function buildInputClassName(errorMessage?: string) {
  return `${inputBaseClassName} ${errorMessage ? "border-rose-300 ring-4 ring-rose-100" : ""}`;
}

function validateForm(username: string, password: string): LoginFieldErrors {
  const errors: LoginFieldErrors = {};

  if (!username.trim()) {
    errors.username = "Vui lòng nhập tên đăng nhập.";
  }

  if (!password) {
    errors.password = "Vui lòng nhập mật khẩu.";
  }

  return errors;
}

function extractFieldErrors(payload: LoginMutationApiResponse | LoginErrorResponse): LoginFieldErrors {
  if (payload.code === "SUCCESS") {
    return {};
  }

  const data = payload.data as Record<string, unknown> | null;

  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return {};
  }

  const errors: LoginFieldErrors = {};

  if (typeof data.username === "string") {
    errors.username = data.username;
  }

  if (typeof data.password === "string") {
    errors.password = data.password;
  }

  return errors;
}

function clearFieldError(field: LoginField, setFieldErrors: React.Dispatch<React.SetStateAction<LoginFieldErrors>>) {
  setFieldErrors((previousErrors) => {
    if (!previousErrors[field]) {
      return previousErrors;
    }

    const nextErrors = { ...previousErrors };
    delete nextErrors[field];
    return nextErrors;
  });
}

function persistRememberedUsername(shouldRemember: boolean, username: string) {
  if (shouldRemember && username) {
    window.localStorage.setItem(AUTH_REMEMBERED_USERNAME_STORAGE_KEY, username);
    return;
  }

  window.localStorage.removeItem(AUTH_REMEMBERED_USERNAME_STORAGE_KEY);
}
