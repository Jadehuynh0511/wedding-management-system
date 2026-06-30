package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Map cấu hình token từ application.yml vào object typed để tránh rải string config trong code.
@ConfigurationProperties(prefix = "app.security.jwt")
public record AuthSecurityProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
}
