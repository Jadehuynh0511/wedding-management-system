package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserGroupResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListUserGroupsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.GroupQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.UserGroup;

// Use case này để lấy danh sách tất cả user group hiện có trong hệ thống
@Service
@Transactional(readOnly = true)
public class ListUserGroupsService implements ListUserGroupsUseCase {

    private final GroupQueryPort groupQueryPort;

    public ListUserGroupsService(GroupQueryPort groupQueryPort) {
        this.groupQueryPort = groupQueryPort;
    }

    @Override
    public List<UserGroupResult> listUserGroups() {
        return groupQueryPort.findAllGroups().stream()
                .map(this::toResult)
                .toList();
    }

    private UserGroupResult toResult(UserGroup userGroup) {
        return new UserGroupResult(
                userGroup.id(),
                userGroup.name(),
                userGroup.systemGroup(),
                userGroup.description());
    }
}
