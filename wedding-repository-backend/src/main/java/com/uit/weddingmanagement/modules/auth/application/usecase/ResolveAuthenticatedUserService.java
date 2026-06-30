package com.uit.weddingmanagement.modules.auth.application.usecase;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.port.in.ResolveAuthenticatedUserUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

// Use case này là mắt xích quan trọng của RBAC dynamic:
// mỗi request có JWT sẽ reload lại user + group + permissions từ DB thay vì tin dữ liệu cũ trong token.
@Service
@Transactional(readOnly = true)
public class ResolveAuthenticatedUserService implements ResolveAuthenticatedUserUseCase {

    private final UserAccountQueryPort userAccountQueryPort;

    public ResolveAuthenticatedUserService(UserAccountQueryPort userAccountQueryPort) {
        this.userAccountQueryPort = userAccountQueryPort;
    }

    // output: AuthenticatedUser (id, username, groupName, permissionCodes)
    @Override
    public AuthenticatedUser resolveById(Long userId) {
        // Gọi port để lấy user hiện tại từ DB.
        UserAccount userAccount = userAccountQueryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user no longer exists."));

        // Kiểm tra lại trạng thái của user mỗi request để đảm bảo quyền và trạng thái
        // tài khoản
        // luôn được cập nhật, ngay cả khi JWT chưa hết hạn.
        userAccount.ensureCanAuthenticate();

        // Trả về AuthenticatedUser từ UserAccount.
        return userAccount.toAuthenticatedUser();
    }
}
