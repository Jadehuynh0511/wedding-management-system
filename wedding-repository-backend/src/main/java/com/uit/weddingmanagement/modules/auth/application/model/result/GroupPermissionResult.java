package com.uit.weddingmanagement.modules.auth.application.model.result;

import java.util.Set;

// Kết quả của use case xem 1 group đang có các permission code nào.
public record GroupPermissionResult(Long groupId, Set<String> permissionCodes) {
}
