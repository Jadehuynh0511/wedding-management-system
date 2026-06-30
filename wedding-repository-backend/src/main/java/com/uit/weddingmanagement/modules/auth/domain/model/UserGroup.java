package com.uit.weddingmanagement.modules.auth.domain.model;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

// UserGroup là "vai trò động" lấy từ DB.
// Điểm quan trọng: group không hardcode quyền trong code, mà chỉ giữ danh sách quyền đã được load từ DB.
public record UserGroup(Long id, String name, boolean systemGroup, String description, Set<Permission> permissions) {

    public Set<String> permissionCodes() {
        // Sắp xếp permission codes cho response ổn định và tránh lỗi khi diff.
        return permissions.stream()
                .map(Permission::code)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
