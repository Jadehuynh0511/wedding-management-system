import type { ApiResponse } from "@/shared/api/api-response";
import { getBackendApiBaseUrl } from "@/shared/config/api";

// Có đang chạy ở phía server (server component / route handler) hay không.
const isServer = typeof window === "undefined";

type BackendRequestInit = Omit<RequestInit, "body"> & {
  // Chỉ dùng khi gọi từ server component: token đọc từ cookie httpOnly và truyền vào.
  // Phía client luôn đi qua BFF proxy nên không cần (và không được phép cầm) token.
  accessToken?: string;
  // Payload JSON; helper tự stringify và set Content-Type.
  jsonBody?: unknown;
};

/**
 * Điểm vào duy nhất cho mọi lời gọi backend, hoạt động ở cả server lẫn client:
 *
 * - Client: gọi BFF proxy tương đối `/api/backend/...`. Cookie httpOnly tự đính kèm,
 *   proxy lo việc refresh access token + retry khi hết hạn => trong suốt với UI.
 *   Nếu proxy vẫn trả 401 nghĩa là refresh token đã hết hạn / bị thu hồi => điều hướng về login.
 * - Server component: gọi thẳng backend với access token truyền vào. Token này luôn còn hạn
 *   vì middleware đã refresh trước khi trang được render.
 */
export async function backendRequest<T>(
  path: string,
  init: BackendRequestInit = {},
  errorMessage = "Đã có lỗi xảy ra. Vui lòng thử lại.",
): Promise<T> {
  const { accessToken, jsonBody, headers, ...rest } = init;

  const requestHeaders = new Headers(headers);
  if (jsonBody !== undefined) {
    requestHeaders.set("Content-Type", "application/json");
  }

  const requestInit: RequestInit = {
    ...rest,
    headers: requestHeaders,
    body: jsonBody !== undefined ? JSON.stringify(jsonBody) : undefined,
    cache: "no-store",
  };

  let response: Response;

  if (isServer) {
    if (accessToken) {
      requestHeaders.set("Authorization", `Bearer ${accessToken}`);
    }
    // Khi được gọi từ server component
    response = await fetch(`${getBackendApiBaseUrl()}/api${path}`, requestInit);
  } else {
    // Khi được gọi từ client
    response = await fetch(`/api/backend${path}`, requestInit);

    // Proxy đã cố refresh nhưng vẫn 401 => phiên thật sự kết thúc => về trang đăng nhập.
    if (response.status === 401) {
      redirectToLogin();
      // Promise treo: chặn code phía sau chạy tiếp trong lúc trình duyệt đang chuyển trang.
      return new Promise<T>(() => {});
    }
  }

  const body = (await response.json().catch(() => null)) as ApiResponse<T> | null;

  if (!response.ok) {
    throw new Error(body?.message || errorMessage);
  }

  // Một số endpoint (vd: DELETE) có thể không trả data — trả về undefined là chấp nhận được.
  return body?.data as T;
}

// Điều hướng người dùng về trang đăng nhập, giữ lại đường dẫn hiện tại để quay lại sau khi đăng nhập.
function redirectToLogin() {
  if (typeof window === "undefined") return;
  if (window.location.pathname === "/login") return;

  const current = `${window.location.pathname}${window.location.search}`;
  window.location.assign(`/login?redirect=${encodeURIComponent(current)}`);
}
