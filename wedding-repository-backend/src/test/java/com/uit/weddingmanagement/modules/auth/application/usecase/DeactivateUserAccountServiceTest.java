package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountCommandPort;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
class DeactivateUserAccountServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Mock
    private UserAccountCommandPort userAccountCommandPort;

    @Captor
    private ArgumentCaptor<UserAccount> userAccountCaptor;

    @Test
    void shouldDeactivateActiveUserAccount() {
        UserAccount activeUserAccount = new UserAccount(
                7L,
                "staff.user",
                "$2a$10$existing-hash",
                "Staff User",
                "staff@local.dev",
                "0900123456",
                UserStatus.ACTIVE,
                staffGroup());

        when(userAccountQueryPort.findById(7L)).thenReturn(Optional.of(activeUserAccount));
        when(userAccountCommandPort.saveUserAccount(any(UserAccount.class)))
                .thenReturn(new UserAccount(
                        7L,
                        "staff.user",
                        "$2a$10$existing-hash",
                        "Staff User",
                        "staff@local.dev",
                        "0900123456",
                        UserStatus.INACTIVE,
                        staffGroup()));

        DeactivateUserAccountService deactivateUserAccountService =
                new DeactivateUserAccountService(userAccountQueryPort, userAccountCommandPort);

        var result = deactivateUserAccountService.deactivateUserAccount(7L);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.username()).isEqualTo("staff.user");
        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);

        verify(userAccountCommandPort).saveUserAccount(userAccountCaptor.capture());
        assertThat(userAccountCaptor.getValue().status()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void shouldTreatAlreadyInactiveUserAsIdempotentRequest() {
        UserAccount inactiveUserAccount = new UserAccount(
                8L,
                "inactive.user",
                "$2a$10$existing-hash",
                "Inactive User",
                "inactive@local.dev",
                "0900999000",
                UserStatus.INACTIVE,
                staffGroup());

        when(userAccountQueryPort.findById(8L)).thenReturn(Optional.of(inactiveUserAccount));

        DeactivateUserAccountService deactivateUserAccountService =
                new DeactivateUserAccountService(userAccountQueryPort, userAccountCommandPort);

        var result = deactivateUserAccountService.deactivateUserAccount(8L);

        assertThat(result.id()).isEqualTo(8L);
        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
        verify(userAccountCommandPort, never()).saveUserAccount(any(UserAccount.class));
    }

    private UserGroup staffGroup() {
        return new UserGroup(2L, "STAFF", true, "Staff accounts", Set.of());
    }
}
