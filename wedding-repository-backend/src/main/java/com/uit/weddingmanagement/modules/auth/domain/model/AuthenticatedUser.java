package com.uit.weddingmanagement.modules.auth.domain.model;

import java.util.Set;

// Đây là user context đã được "xác thực xong" và sẵn sàng đưa vào SecurityContext.
// Nó là shape trung tâm mà use case, security filter và controller cùng dùng chung.
public record AuthenticatedUser(
        Long id,
        String username,
        String fullName,
        Long groupId,
        String groupName,
        Set<String> permissionCodes) {

    public boolean hasPermission(String permissionCode) {
        // Tất cả check permission về sau nên đi qua permission code thay vì hardcode role.
        return permissionCodes.contains(permissionCode);
    }

    public boolean isAdmin() {
        // ADMIN là rule quản trị hệ thống riêng của QD11, không phải permission thứ 17.
        return "ADMIN".equals(groupName);
    }
}
