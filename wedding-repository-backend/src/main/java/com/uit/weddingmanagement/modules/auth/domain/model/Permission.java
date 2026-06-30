package com.uit.weddingmanagement.modules.auth.domain.model;

// Domain model thuần cho 1 quyền trong hệ thống.
// Lớp này không biết gì về JPA, Spring Security hay HTTP.
public record Permission(
        Long id,
        String code,
        String displayName,
        String moduleKey,
        String functionalGroup,
        String description) {
}
