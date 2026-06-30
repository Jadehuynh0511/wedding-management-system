package com.uit.weddingmanagement.modules.auth.application.model.internal;

import java.time.Instant;

// Model này chỉ chứa thông tin access token để trả về API, không chứa domain user.
public record AccessToken(String value, String tokenType, Instant expiresAt) {
}
