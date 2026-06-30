package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Lấy current user từ SecurityContext và chuyển về domain object.
@Component
public class CurrentUserSecurityContextAdapter implements CurrentUserPort {

    @Override
    public AuthenticatedUser getCurrentUser() {
        // Lấy Authentication từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem có Authentication và có phải là AuthenticatedUserPrincipal
        if (authentication == null
                || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found in the security context.");
        }

        // Trả về AuthenticatedUser từ AuthenticatedUserPrincipal
        return principal.getAuthenticatedUser();
    }
}
