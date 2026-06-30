package com.uit.weddingmanagement.modules.auth.application.port.in;

import java.util.UUID;

public interface ValidateAuthenticatedSessionUseCase {

    void ensureSessionIsActive(Long userId, UUID sessionFamilyId);
}
