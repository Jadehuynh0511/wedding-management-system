package com.uit.weddingmanagement.modules.auth.domain.exception;

// Dùng khi access token vẫn parse được nhưng session family đã bị logout/revoke.
public class InactiveAuthenticatedSessionException extends RuntimeException {

    public InactiveAuthenticatedSessionException() {
        super("Authenticated session is no longer active.");
    }
}
