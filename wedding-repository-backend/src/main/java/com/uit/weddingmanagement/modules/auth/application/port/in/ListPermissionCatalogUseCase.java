package com.uit.weddingmanagement.modules.auth.application.port.in;

import java.util.List;

import com.uit.weddingmanagement.modules.auth.application.model.result.PermissionCatalogResult;

public interface ListPermissionCatalogUseCase {

    List<PermissionCatalogResult> listPermissionCatalog();
}
