package com.uit.weddingmanagement.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting HTTP request metadata.
 * Supports reverse-proxy scenarios by honouring the X-Forwarded-For header.
 */
public final class HttpRequestUtils {

    private static final int FORWARDED_IP_INDEX = 0;

    private HttpRequestUtils() {
        // Utility class — không khởi tạo.
    }

    /**
     * Resolves the real client IP, preferring the first value in X-Forwarded-For
     * when available (e.g., behind Nginx).
     */
    public static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] forwardedIps = forwardedFor.split(",");
            if (forwardedIps.length > FORWARDED_IP_INDEX) {
                String clientIp = forwardedIps[FORWARDED_IP_INDEX].trim();
                if (!clientIp.isEmpty()) {
                    return clientIp;
                }
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns the User-Agent header value, or {@code null} if absent.
     */
    public static String resolveUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
