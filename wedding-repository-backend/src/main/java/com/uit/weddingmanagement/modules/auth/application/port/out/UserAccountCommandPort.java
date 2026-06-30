package com.uit.weddingmanagement.modules.auth.application.port.out;

import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

public interface UserAccountCommandPort {

    UserAccount saveUserAccount(UserAccount userAccount);
}
