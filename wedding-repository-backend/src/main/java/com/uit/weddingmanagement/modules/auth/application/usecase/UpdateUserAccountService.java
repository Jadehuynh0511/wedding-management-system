package com.uit.weddingmanagement.modules.auth.application.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.model.command.UpdateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.UpdateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class UpdateUserAccountService implements UpdateUserAccountUseCase {

    private final UserAccountQueryPort userAccountQueryPort;
    private final UserAccountCommandPort userAccountCommandPort;
    private final GroupQueryPort groupQueryPort;

    public UpdateUserAccountService(
            UserAccountQueryPort userAccountQueryPort,
            UserAccountCommandPort userAccountCommandPort,
            GroupQueryPort groupQueryPort) {
        this.userAccountQueryPort = userAccountQueryPort;
        this.userAccountCommandPort = userAccountCommandPort;
        this.groupQueryPort = groupQueryPort;
    }

    @Override
    @AuditAction(
            action = "USER_UPDATE",
            module = "SYSTEM",
            targetType = "USER_ACCOUNT",
            targetIdExpression = "#userId",
            targetLabelExpression = "#result.username",
            successDescriptionExpression = "'Updated user ' + #result.username",
            failureDescriptionExpression = "'Failed to update user id ' + #userId",
            detailsExpression = "#command")
    public UserAccountResult updateUserAccount(Long userId, UpdateUserAccountCommand command) {
        UserAccount existingUserAccount = userAccountQueryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User account not found with id: " + userId));
        UserGroup userGroup = resolveUserGroup(command.groupId());

        UserAccount updatedUserAccount = existingUserAccount.update(
                command.username(),
                command.fullName(),
                command.email(),
                command.phoneNumber(),
                command.status() == null ? existingUserAccount.status() : command.status(),
                userGroup);

        ensureUniqueForUpdate(updatedUserAccount);

        try {
            return UserAccountResult.from(userAccountCommandPort.saveUserAccount(updatedUserAccount));
        } catch (DataIntegrityViolationException exception) {
            throw resolveUpdateConflict(updatedUserAccount);
        }
    }

    private UserGroup resolveUserGroup(Long groupId) {
        return groupQueryPort.findGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("User group not found with id: " + groupId));
    }

    private void ensureUniqueForUpdate(UserAccount userAccount) {
        if (userAccountQueryPort.existsByUsernameAndIdNot(userAccount.username(), userAccount.id())) {
            throw new DuplicateResourceException("Username already exists: " + userAccount.username());
        }

        if (userAccount.email() != null
                && userAccountQueryPort.existsByEmailAndIdNot(userAccount.email(), userAccount.id())) {
            throw new DuplicateResourceException("Email already exists: " + userAccount.email());
        }

        if (userAccount.phoneNumber() != null
                && userAccountQueryPort.existsByPhoneNumberAndIdNot(userAccount.phoneNumber(), userAccount.id())) {
            throw new DuplicateResourceException("Phone number already exists: " + userAccount.phoneNumber());
        }
    }

    private DuplicateResourceException resolveUpdateConflict(UserAccount userAccount) {
        if (userAccountQueryPort.existsByUsernameAndIdNot(userAccount.username(), userAccount.id())) {
            return new DuplicateResourceException("Username already exists: " + userAccount.username());
        }

        if (userAccount.email() != null
                && userAccountQueryPort.existsByEmailAndIdNot(userAccount.email(), userAccount.id())) {
            return new DuplicateResourceException("Email already exists: " + userAccount.email());
        }

        if (userAccount.phoneNumber() != null
                && userAccountQueryPort.existsByPhoneNumberAndIdNot(userAccount.phoneNumber(), userAccount.id())) {
            return new DuplicateResourceException("Phone number already exists: " + userAccount.phoneNumber());
        }

        return new DuplicateResourceException("User account contains duplicate unique fields.");
    }
}
