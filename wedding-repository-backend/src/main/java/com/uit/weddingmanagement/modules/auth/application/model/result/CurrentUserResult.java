package com.uit.weddingmanagement.modules.auth.application.model.result;

import java.util.Set;

// Model này chỉ chứa thông tin user hiện tại để trả về cho API /me.
public record CurrentUserResult(Long id, String username, String groupName, Set<String> permissionCodes) {
}
