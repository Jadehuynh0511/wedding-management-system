package com.uit.weddingmanagement.modules.auth.application.model.command;

// Command cho use case refresh session.
public record RefreshSessionCommand(String refreshToken, String ipAddress, String userAgent) {
}
