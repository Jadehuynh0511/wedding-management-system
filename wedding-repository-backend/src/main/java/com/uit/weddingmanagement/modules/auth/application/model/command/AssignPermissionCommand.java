package com.uit.weddingmanagement.modules.auth.application.model.command;

// Command này chỉ đơn giản là một DTO để truyền dữ liệu đầu vào cho use case cấp quyền cho group
public record AssignPermissionCommand(Long groupId, String permissionCode) {
}
