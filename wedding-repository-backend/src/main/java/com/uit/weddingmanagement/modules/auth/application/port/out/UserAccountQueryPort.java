package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.util.List;
import java.util.Optional;

import com.uit.weddingmanagement.modules.auth.domain.model.UserAccount;

public interface UserAccountQueryPort {

    List<UserAccount> findAllUsers();

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findById(Long userId);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long userId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long userId);
}
