package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.weddingmanagement.common.api.ApiResponse;

// Trả JSON 401 thống nhất khi user chưa đăng nhập, thay vì trang lỗi mặc định của Spring Security
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        // Set status 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Set content type
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Ghi response
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.error("UNAUTHORIZED", authException.getMessage()));
    }
}
