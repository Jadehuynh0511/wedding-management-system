package com.uit.weddingmanagement.modules.auth.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.weddingmanagement.modules.auth.application.model.result.PermissionCatalogResult;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListPermissionCatalogUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.PermissionQueryPort;
import com.uit.weddingmanagement.modules.auth.domain.model.Permission;

// Use case này chỉ là đọc toàn bộ permission catalog từ DB và map sang shape mà API cần.Không có business logic
// gì đặc biệt ở đây, nhưng vẫn nên giữ ở application layer để đảm bảo separation of concerns và dễ dàng tái sử dụng nếu cần.
@Service
@Transactional(readOnly = true)
public class ListPermissionCatalogService implements ListPermissionCatalogUseCase {

    private final PermissionQueryPort permissionQueryPort;

    public ListPermissionCatalogService(PermissionQueryPort permissionQueryPort) {
        this.permissionQueryPort = permissionQueryPort;
    }

    @Override
    public List<PermissionCatalogResult> listPermissionCatalog() {
        return permissionQueryPort.findAllPermissions().stream()
                .map(this::toResult)
                .toList();
    }

    private PermissionCatalogResult toResult(Permission permission) {
        return new PermissionCatalogResult(
                permission.id(),
                permission.code(),
                permission.displayName(),
                permission.moduleKey(),
                permission.functionalGroup(),
                permission.description());
    }
}
