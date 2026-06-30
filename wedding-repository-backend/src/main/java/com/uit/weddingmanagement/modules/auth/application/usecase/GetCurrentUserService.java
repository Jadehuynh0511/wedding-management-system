package com.uit.weddingmanagement.modules.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.CurrentUserResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetCurrentUserUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Use case nhỏ này gom dữ liệu từ SecurityContext (cũng là user context đã 
// được xác thực xong) hiện tại để phục vụ API /api/auth/me.
@Service
@Transactional(readOnly = true)
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final CurrentUserPort currentUserPort;

    public GetCurrentUserService(CurrentUserPort currentUserPort) {
        this.currentUserPort = currentUserPort;
    }

    @Override
    public CurrentUserResult getCurrentUser() {
        // Gọi port để lấy user hiện tại. Port ở đây là interface, không phải
        // SecurityContext trực tiếp.
        AuthenticatedUser currentUser = currentUserPort.getCurrentUser();

        return new CurrentUserResult(
                currentUser.id(),
                currentUser.username(),
                currentUser.groupName(),
                currentUser.permissionCodes());
    }
}
