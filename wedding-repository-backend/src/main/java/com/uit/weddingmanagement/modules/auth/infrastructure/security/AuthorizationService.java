package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import org.springframework.stereotype.Component;

import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;

// Bean này được expose ra cho method security/spEL dùng về sau.
// Ví dụ phase 4 có thể dùng @PreAuthorize("@authorizationService.hasPermission('USER_GROUP_MANAGE')").
@Component("authorizationService")
public class AuthorizationService {

    private final CurrentUserPort currentUserPort;

    public AuthorizationService(CurrentUserPort currentUserPort) {
        this.currentUserPort = currentUserPort;
    }

    public boolean hasPermission(String permissionCode) {
        return currentUserPort.getCurrentUser().hasPermission(permissionCode);
    }

    public boolean isAdmin() {
        return currentUserPort.getCurrentUser().isAdmin();
    }
}
