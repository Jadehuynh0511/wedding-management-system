package com.uit.weddingmanagement.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cấu hình OpenAPI (Swagger) cho ứng dụng, để tạo tài liệu API dễ đọc và tương tác.
@Configuration
public class OpenApiConfig {

        // Tạo bean OpenAPI cho Swagger, cần tạo Bean vì Swagger sẽ tìm kiếm bean này để
        // cấu hình tài liệu API.
        @Bean
        OpenAPI weddingManagementOpenApi() {
                return new OpenAPI()
                                .info(
                                                new Info()
                                                                .title("Wedding Management API")
                                                                .version("v1")
                                                                .description(
                                                                                "Interactive OpenAPI documentation for the Wedding Management backend."))
                                .components(
                                                new Components()
                                                                // Định nghĩa SecurityScheme để Swagger hiểu cách xác
                                                                // thực JWT
                                                                .addSecuritySchemes(
                                                                                "bearerAuth",
                                                                                new SecurityScheme()
                                                                                                .name("Authorization")
                                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                                .scheme("bearer")
                                                                                                .bearerFormat("JWT")));
        }
}
