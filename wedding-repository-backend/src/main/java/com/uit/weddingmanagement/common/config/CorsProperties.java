package com.uit.weddingmanagement.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Map giá trị từ application.yml
// app.cors.allowed-origins -> List<String> allowedOrigins
// Nhiều origin có thể thêm/sửa mà không cần sửa code Java.
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
