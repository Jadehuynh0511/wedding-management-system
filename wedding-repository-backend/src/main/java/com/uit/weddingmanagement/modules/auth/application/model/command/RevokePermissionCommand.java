package com.uit.weddingmanagement.modules.auth.application.model.command;

// Command cho use case thu hoi quyen khoi group.
public record RevokePermissionCommand(Long groupId, String permissionCode) {}
