package com.uit.weddingmanagement.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Bật auditing toàn cục:
    // - @CreatedDate  -> tự động set createdAt lúc INSERT
    // - @LastModifiedDate -> tự động cập nhật updatedAt lúc UPDATE
    // Giá trị này áp dụng cho các entity extends BaseEntity.
}
