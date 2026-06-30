package com.uit.weddingmanagement.modules.auth.application.model.result;

// DTO nội bộ cho danh sách group, để presentation layer không cần dùng thằng domain
// model.
public record UserGroupResult(Long id, String name, boolean systemGroup, String description) {
}
