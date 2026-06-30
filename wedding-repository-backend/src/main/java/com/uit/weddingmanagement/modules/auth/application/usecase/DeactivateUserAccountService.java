package com.uit.weddingmanagement.modules.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.DeactivateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class DeactivateUserAccountService implements DeactivateUserAccountUseCase {

    private final UserAccountQueryPort userAccountQueryPort;
    private final UserAccountCommandPort userAccountCommandPort;

    public DeactivateUserAccountService(
            UserAccountQueryPort userAccountQueryPort,
            UserAccountCommandPort userAccountCommandPort) {
        this.userAccountQueryPort = userAccountQueryPort;
        this.userAccountCommandPort = userAccountCommandPort;
    }

    @Override
    @AuditAction(
            action = "USER_UPDATE",
            module = "SYSTEM",
            targetType = "USER_ACCOUNT",
            targetIdExpression = "#userId",
            targetLabelExpression = "#result.username",
            successDescriptionExpression = "'Deactivated user ' + #result.username",
            failureDescriptionExpression = "'Failed to deactivate user id ' + #userId")
    public UserAccountResult deactivateUserAccount(Long userId) {
        UserAccount userAccount = userAccountQueryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User account not found with id: " + userId));

        if (userAccount.status() == UserStatus.INACTIVE) {
            return UserAccountResult.from(userAccount);
        }

        return UserAccountResult.from(userAccountCommandPort.saveUserAccount(userAccount.deactivate()));
    }
}
