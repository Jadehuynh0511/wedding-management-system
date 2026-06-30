package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class GetUserAccountServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Test
    void shouldReturnUserAccountDetailWhenUserExists() {
        when(userAccountQueryPort.findById(5L)).thenReturn(Optional.of(
                new UserAccount(
                        5L,
                        "staff.user",
                        "$2a$10$hash",
                        "Staff User",
                        "staff@local.dev",
                        "0900123456",
                        UserStatus.ACTIVE,
                        staffGroup())));

        GetUserAccountService getUserAccountService = new GetUserAccountService(userAccountQueryPort);

        var result = getUserAccountService.getUserAccount(5L);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.username()).isEqualTo("staff.user");
        assertThat(result.fullName()).isEqualTo("Staff User");
        assertThat(result.email()).isEqualTo("staff@local.dev");
        assertThat(result.phoneNumber()).isEqualTo("0900123456");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.groupId()).isEqualTo(2L);
        assertThat(result.groupName()).isEqualTo("STAFF");
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userAccountQueryPort.findById(404L)).thenReturn(Optional.empty());

        GetUserAccountService getUserAccountService = new GetUserAccountService(userAccountQueryPort);

        assertThatThrownBy(() -> getUserAccountService.getUserAccount(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User account not found with id: 404");
    }

    private UserGroup staffGroup() {
        return new UserGroup(2L, "STAFF", true, "Staff accounts", Set.of());
    }
}
