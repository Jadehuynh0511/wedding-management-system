package com.uit.weddingmanagement.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;
import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
class ListUserAccountsServiceTest {

    @Mock
    private UserAccountQueryPort userAccountQueryPort;

    @Test
    void shouldMapAllUserAccountsForAdministrationList() {
        when(userAccountQueryPort.findAllUsers()).thenReturn(List.of(
                new UserAccount(1L, "admin", "$2a$10$hash", "Local Admin", "admin@local.dev", "0900000000",
                        UserStatus.ACTIVE, adminGroup()),
                new UserAccount(2L, "staff.user", "$2a$10$hash", "Staff User", null, null,
                        UserStatus.INACTIVE, staffGroup())));

        ListUserAccountsService listUserAccountsService = new ListUserAccountsService(userAccountQueryPort);

        var result = listUserAccountsService.listUserAccounts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("admin");
        assertThat(result.get(0).groupName()).isEqualTo("ADMIN");
        assertThat(result.get(1).username()).isEqualTo("staff.user");
        assertThat(result.get(1).status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(result.get(1).groupId()).isEqualTo(2L);
    }

    private UserGroup adminGroup() {
        return new UserGroup(1L, "ADMIN", true, "System administrators", Set.of());
    }

    private UserGroup staffGroup() {
        return new UserGroup(2L, "STAFF", true, "Staff accounts", Set.of());
    }
}
