package com.uit.weddingmanagement.modules.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserAccountResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.UserAccountQueryPort;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class GetUserAccountService implements GetUserAccountUseCase {

    private final UserAccountQueryPort userAccountQueryPort;

    public GetUserAccountService(UserAccountQueryPort userAccountQueryPort) {
        this.userAccountQueryPort = userAccountQueryPort;
    }

    @Override
    public UserAccountResult getUserAccount(Long userId) {
        return userAccountQueryPort.findById(userId)
                .map(UserAccountResult::from)
                .orElseThrow(() -> new EntityNotFoundException("User account not found with id: " + userId));
    }
}
