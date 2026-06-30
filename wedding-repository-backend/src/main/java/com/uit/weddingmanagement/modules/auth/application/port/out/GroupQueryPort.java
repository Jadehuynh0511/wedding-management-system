package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.util.List;
import java.util.Optional;

import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;

// Port này dùng để truy vấn thông tin về group
public interface GroupQueryPort {

    List<UserGroup> findAllGroups();

    Optional<UserGroup> findGroupById(Long groupId);
}
