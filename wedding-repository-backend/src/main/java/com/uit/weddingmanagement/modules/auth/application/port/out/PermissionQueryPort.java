package com.uit.weddingmanagement.modules.auth.application.port.out;

import java.util.List;
import java.util.Optional;

import com.uit.weddingmanagement.modules.auth.domain.model.Permission;

public interface PermissionQueryPort {

    List<Permission> findAllPermissions();

    Optional<Permission> findPermissionByCode(String permissionCode);
}
