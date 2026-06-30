package com.uit.weddingmanagement.modules.auth.application.model.result;

// Shape trung gian của 1 dòng permission catalog trả về từ application layer.
public record PermissionCatalogResult(
                Long id,
                String code,
                String name,
                String moduleKey,
                String functionalGroup,
                String description) {
}
