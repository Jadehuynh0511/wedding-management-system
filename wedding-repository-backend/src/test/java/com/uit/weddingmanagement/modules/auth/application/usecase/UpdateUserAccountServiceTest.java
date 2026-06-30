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
import com.uit.weddingmanagement.modules.auth.application.model.command.UpdateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
class UpdateUserAccountServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Mock
    private UserAccountCommandPort userAccountCommandPort;

    @Mock
    private GroupQueryPort groupQueryPort;

    @Captor
    private ArgumentCaptor<UserAccount> userAccountCaptor;

    @Test
    void shouldUpdateUserAccountAndPreservePasswordHash() {
        UserAccount existingUserAccount = new UserAccount(
                4L,
                "old.user",
                "$2a$10$existing-hash",
                "Old User",
                "old@local.dev",
                "0900111222",
                UserStatus.ACTIVE,
                adminGroup());
        UserGroup staffGroup = staffGroup();

        when(userAccountQueryPort.findById(4L)).thenReturn(Optional.of(existingUserAccount));
        when(groupQueryPort.findGroupById(2L)).thenReturn(Optional.of(staffGroup));
        when(userAccountQueryPort.existsByUsernameAndIdNot("edited.user", 4L)).thenReturn(false);
        when(userAccountQueryPort.existsByEmailAndIdNot("edited@local.dev", 4L)).thenReturn(false);
        when(userAccountQueryPort.existsByPhoneNumberAndIdNot("0900999888", 4L)).thenReturn(false);
        when(userAccountCommandPort.saveUserAccount(any(UserAccount.class)))
                .thenReturn(new UserAccount(
                        4L,
                        "edited.user",
                        "$2a$10$existing-hash",
                        "Edited User",
                        "edited@local.dev",
                        "0900999888",
                        UserStatus.LOCKED,
                        staffGroup));

        UpdateUserAccountService updateUserAccountService = new UpdateUserAccountService(
                userAccountQueryPort,
                userAccountCommandPort,
                groupQueryPort);

        var result = updateUserAccountService.updateUserAccount(
                4L,
                new UpdateUserAccountCommand(
                        " edited.user ",
                        "  Edited   User  ",
                        " Edited@Local.dev ",
                        " 0900999888 ",
                        2L,
                        UserStatus.LOCKED));

        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.username()).isEqualTo("edited.user");
        assertThat(result.fullName()).isEqualTo("Edited User");
        assertThat(result.email()).isEqualTo("edited@local.dev");
        assertThat(result.phoneNumber()).isEqualTo("0900999888");
        assertThat(result.status()).isEqualTo(UserStatus.LOCKED);
        assertThat(result.groupName()).isEqualTo("STAFF");

        verify(userAccountCommandPort).saveUserAccount(userAccountCaptor.capture());
        UserAccount savedUserAccount = userAccountCaptor.getValue();
        assertThat(savedUserAccount.passwordHash()).isEqualTo("$2a$10$existing-hash");
        assertThat(savedUserAccount.username()).isEqualTo("edited.user");
        assertThat(savedUserAccount.userGroup().id()).isEqualTo(2L);
        assertThat(savedUserAccount.status()).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void shouldRejectDuplicateEmailWhenUpdatingUserAccount() {
        UserAccount existingUserAccount = new UserAccount(
                4L,
                "old.user",
                "$2a$10$existing-hash",
                "Old User",
                "old@local.dev",
                "0900111222",
                UserStatus.ACTIVE,
                adminGroup());

        when(userAccountQueryPort.findById(4L)).thenReturn(Optional.of(existingUserAccount));
        when(groupQueryPort.findGroupById(2L)).thenReturn(Optional.of(staffGroup()));
        when(userAccountQueryPort.existsByUsernameAndIdNot("old.user", 4L)).thenReturn(false);
        when(userAccountQueryPort.existsByEmailAndIdNot("taken@local.dev", 4L)).thenReturn(true);

        UpdateUserAccountService updateUserAccountService = new UpdateUserAccountService(
                userAccountQueryPort,
                userAccountCommandPort,
                groupQueryPort);

        assertThatThrownBy(() -> updateUserAccountService.updateUserAccount(
                4L,
                new UpdateUserAccountCommand(
                        "old.user",
                        "Old User",
                        " Taken@Local.dev ",
                        "0900111222",
                        2L,
                        null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists: taken@local.dev");

        verify(userAccountCommandPort, never()).saveUserAccount(any(UserAccount.class));
    }

    private UserGroup adminGroup() {
        return new UserGroup(1L, "ADMIN", true, "System administrators", Set.of());
    }

    private UserGroup staffGroup() {
        return new UserGroup(2L, "STAFF", true, "Staff accounts", Set.of());
    }
}
