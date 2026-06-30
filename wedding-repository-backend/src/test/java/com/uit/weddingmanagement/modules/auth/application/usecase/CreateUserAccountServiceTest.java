package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.auth.application.model.command.CreateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.PasswordHashPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
class CreateUserAccountServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Mock
    private UserAccountCommandPort userAccountCommandPort;

    @Mock
    private GroupQueryPort groupQueryPort;

    @Mock
    private PasswordHashPort passwordHashPort;

    @Captor
    private ArgumentCaptor<UserAccount> userAccountCaptor;

    @Test
    void shouldCreateUserAccountWithNormalizedFieldsAndDefaultActiveStatus() {
        UserGroup staffGroup = staffGroup();

        when(groupQueryPort.findGroupById(2L)).thenReturn(Optional.of(staffGroup));
        when(passwordHashPort.hash("Secret123!")).thenReturn("$2a$10$new-hash");
        when(userAccountQueryPort.existsByUsername("staff.user")).thenReturn(false);
        when(userAccountQueryPort.existsByEmail("staff@local.dev")).thenReturn(false);
        when(userAccountQueryPort.existsByPhoneNumber("0900123456")).thenReturn(false);
        when(userAccountCommandPort.saveUserAccount(any(UserAccount.class)))
                .thenReturn(new UserAccount(
                        10L,
                        "staff.user",
                        "$2a$10$new-hash",
                        "Staff User",
                        "staff@local.dev",
                        "0900123456",
                        UserStatus.ACTIVE,
                        staffGroup));

        CreateUserAccountService createUserAccountService = new CreateUserAccountService(
                userAccountQueryPort,
                userAccountCommandPort,
                groupQueryPort,
                passwordHashPort);

        var result = createUserAccountService.createUserAccount(
                new CreateUserAccountCommand(
                        "  staff.user  ",
                        "Secret123!",
                        "  Staff   User  ",
                        "  Staff@Local.dev  ",
                        " 0900123456 ",
                        2L,
                        null));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.username()).isEqualTo("staff.user");
        assertThat(result.fullName()).isEqualTo("Staff User");
        assertThat(result.email()).isEqualTo("staff@local.dev");
        assertThat(result.phoneNumber()).isEqualTo("0900123456");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.groupId()).isEqualTo(2L);
        assertThat(result.groupName()).isEqualTo("STAFF");

        verify(userAccountCommandPort).saveUserAccount(userAccountCaptor.capture());
        UserAccount savedUserAccount = userAccountCaptor.getValue();
        assertThat(savedUserAccount.id()).isNull();
        assertThat(savedUserAccount.username()).isEqualTo("staff.user");
        assertThat(savedUserAccount.passwordHash()).isEqualTo("$2a$10$new-hash");
        assertThat(savedUserAccount.fullName()).isEqualTo("Staff User");
        assertThat(savedUserAccount.email()).isEqualTo("staff@local.dev");
        assertThat(savedUserAccount.phoneNumber()).isEqualTo("0900123456");
        assertThat(savedUserAccount.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUserAccount.userGroup().id()).isEqualTo(2L);
    }

    @Test
    void shouldRejectDuplicateUsernameWhenCreatingUserAccount() {
        when(groupQueryPort.findGroupById(2L)).thenReturn(Optional.of(staffGroup()));
        when(passwordHashPort.hash("Secret123!")).thenReturn("$2a$10$new-hash");
        when(userAccountQueryPort.existsByUsername("taken.user")).thenReturn(true);

        CreateUserAccountService createUserAccountService = new CreateUserAccountService(
                userAccountQueryPort,
                userAccountCommandPort,
                groupQueryPort,
                passwordHashPort);

        assertThatThrownBy(() -> createUserAccountService.createUserAccount(
                new CreateUserAccountCommand(
                        " taken.user ",
                        "Secret123!",
                        "Staff User",
                        "staff@local.dev",
                        "0900123456",
                        2L,
                        UserStatus.ACTIVE)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists: taken.user");

        verify(userAccountCommandPort, never()).saveUserAccount(any(UserAccount.class));
    }

    private UserGroup staffGroup() {
        return new UserGroup(2L, "STAFF", true, "Staff accounts", Set.of());
    }
}
