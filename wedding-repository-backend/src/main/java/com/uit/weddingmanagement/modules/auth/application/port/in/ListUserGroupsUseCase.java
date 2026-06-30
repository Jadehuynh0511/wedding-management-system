package com.uit.weddingmanagement.modules.auth.application.port.in;

import java.util.List;

import com.uit.weddingmanagement.modules.auth.application.model.result.UserGroupResult;

public interface ListUserGroupsUseCase {

    List<UserGroupResult> listUserGroups();
}
