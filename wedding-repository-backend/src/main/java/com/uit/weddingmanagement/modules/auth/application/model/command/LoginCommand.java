package com.uit.weddingmanagement.modules.auth.application.model.command;

// DTO dùng ở layer application để nhận tham số login từ controller.
public record LoginCommand(String username, String password, String ipAddress, String userAgent) {
}
