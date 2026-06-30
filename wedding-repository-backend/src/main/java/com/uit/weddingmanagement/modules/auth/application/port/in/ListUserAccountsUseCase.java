package com.uit.weddingmanagement.modules.auth.application.port.in;

import java.util.List;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;

public interface ListUserAccountsUseCase {

    List<UserAccountResult> listUserAccounts();
}
