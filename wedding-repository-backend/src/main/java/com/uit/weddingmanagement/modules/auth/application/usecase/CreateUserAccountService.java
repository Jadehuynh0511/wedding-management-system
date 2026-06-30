package com.uit.weddingmanagement.modules.auth.application.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.model.command.CreateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.CreateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.PasswordHashPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class CreateUserAccountService implements CreateUserAccountUseCase {

    private final UserAccountQueryPort userAccountQueryPort;
    private final UserAccountCommandPort userAccountCommandPort;
    private final GroupQueryPort groupQueryPort;
    private final PasswordHashPort passwordHashPort;

    public CreateUserAccountService(
            UserAccountQueryPort userAccountQueryPort,
            UserAccountCommandPort userAccountCommandPort,
            GroupQueryPort groupQueryPort,
            PasswordHashPort passwordHashPort) {
        this.userAccountQueryPort = userAccountQueryPort;
        this.userAccountCommandPort = userAccountCommandPort;
        this.groupQueryPort = groupQueryPort;
        this.passwordHashPort = passwordHashPort;
    }

    @Override
    @AuditAction(
            action = "USER_CREATE",
            module = "SYSTEM",
            targetType = "USER_ACCOUNT",
            targetIdExpression = "#result.id",
            targetLabelExpression = "#result.username",
            successDescriptionExpression = "'Created user ' + #result.username",
            failureDescriptionExpression = "'Failed to create user ' + #command.username",
            detailsExpression = "#command")
    public UserAccountResult createUserAccount(CreateUserAccountCommand command) {
        UserGroup userGroup = resolveUserGroup(command.groupId());
        UserStatus userStatus = command.status() == null ? UserStatus.ACTIVE : command.status();

        UserAccount userAccount = UserAccount.create(
                command.username(),
                passwordHashPort.hash(command.password()),
                command.fullName(),
                command.email(),
                command.phoneNumber(),
                userStatus,
                userGroup);

        ensureUniqueForCreate(userAccount);

        try {
            return UserAccountResult.from(userAccountCommandPort.saveUserAccount(userAccount));
        } catch (DataIntegrityViolationException exception) {
            throw resolveCreateConflict(userAccount);
        }
    }

    private UserGroup resolveUserGroup(Long groupId) {
        return groupQueryPort.findGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("User group not found with id: " + groupId));
    }

    private void ensureUniqueForCreate(UserAccount userAccount) {
        if (userAccountQueryPort.existsByUsername(userAccount.username())) {
            throw new DuplicateResourceException("Username already exists: " + userAccount.username());
        }

        if (userAccount.email() != null && userAccountQueryPort.existsByEmail(userAccount.email())) {
            throw new DuplicateResourceException("Email already exists: " + userAccount.email());
        }

        if (userAccount.phoneNumber() != null
                && userAccountQueryPort.existsByPhoneNumber(userAccount.phoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + userAccount.phoneNumber());
        }
    }

    private DuplicateResourceException resolveCreateConflict(UserAccount userAccount) {
        if (userAccountQueryPort.existsByUsername(userAccount.username())) {
            return new DuplicateResourceException("Username already exists: " + userAccount.username());
        }

        if (userAccount.email() != null && userAccountQueryPort.existsByEmail(userAccount.email())) {
            return new DuplicateResourceException("Email already exists: " + userAccount.email());
        }

        if (userAccount.phoneNumber() != null
                && userAccountQueryPort.existsByPhoneNumber(userAccount.phoneNumber())) {
            return new DuplicateResourceException("Phone number already exists: " + userAccount.phoneNumber());
        }

        return new DuplicateResourceException("User account contains duplicate unique fields.");
    }
}
