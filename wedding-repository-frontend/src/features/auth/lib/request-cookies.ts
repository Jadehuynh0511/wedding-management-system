import { cookies, headers } from "next/headers";

/**
 * Helper này để ưu tiên đọc cookie từ header của request hiện tại, vì trong một số trường hợp (như sau khi refresh token thành công trong middleware), giá trị cookie mới sẽ chỉ tồn tại trong header của request và chưa kịp đồng bộ vào đối tượng cookies() của Next.js.
 * Nếu không có cookie nào được tìm thấy trong header, helper này sẽ fallback về đọc từ cookies() như bình thường.
 */
export function getRequestCookieValue(name: string) {
  // Đọc cookie từ header của request hiện tại
  const cookieHeader = headers().get("cookie");

  if (cookieHeader) {
    const cookieValue = readCookieValueFromHeader(cookieHeader, name);
    if (cookieValue !== null) {
      return cookieValue;
    }
  }

  return cookies().get(name)?.value ?? null;
}

function readCookieValueFromHeader(cookieHeader: string, targetName: string) {
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

function safeDecodeURIComponent(value: string) {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}
