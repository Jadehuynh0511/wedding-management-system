package com.uit.weddingmanagement.modules.auth.application.model.command;

// Command cho logout: backend sẽ revoke refresh-token family tương ứng.
public record LogoutCommand(String refreshToken) {
}
