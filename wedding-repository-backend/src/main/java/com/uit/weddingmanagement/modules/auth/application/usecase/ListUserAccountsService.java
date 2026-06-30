package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListUserAccountsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;

@Service
@Transactional(readOnly = true)
public class ListUserAccountsService implements ListUserAccountsUseCase {

    private final UserAccountQueryPort userAccountQueryPort;

    public ListUserAccountsService(UserAccountQueryPort userAccountQueryPort) {
        this.userAccountQueryPort = userAccountQueryPort;
    }

    @Override
    public List<UserAccountResult> listUserAccounts() {
        return userAccountQueryPort.findAllUsers().stream()
                .map(UserAccountResult::from)
                .toList();
    }
}
