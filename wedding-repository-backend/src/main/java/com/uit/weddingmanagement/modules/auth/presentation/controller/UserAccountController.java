package com.uit.weddingmanagement.modules.auth.presentation.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.auth.application.model.command.CreateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.model.command.UpdateUserAccountCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.CreateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.DeactivateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.ListUserAccountsUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.UpdateUserAccountUseCase;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.CreateUserRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.UpdateUserRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.UserAccountResponse;
import com.uit.weddingmanagement.modules.auth.presentation.mapper.UserAccountPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Accounts", description = "APIs for managing staff login accounts.")
public class UserAccountController {

    private static final String STAFF_ACCOUNT_MANAGE_PERMISSION =
            "@authorizationService.hasPermission('STAFF_ACCOUNT_MANAGE')";

    private final ListUserAccountsUseCase listUserAccountsUseCase;
    private final GetUserAccountUseCase getUserAccountUseCase;
    private final CreateUserAccountUseCase createUserAccountUseCase;
    private final UpdateUserAccountUseCase updateUserAccountUseCase;
    private final DeactivateUserAccountUseCase deactivateUserAccountUseCase;
    private final UserAccountPresentationMapper userAccountPresentationMapper;

    public UserAccountController(
            ListUserAccountsUseCase listUserAccountsUseCase,
            GetUserAccountUseCase getUserAccountUseCase,
            CreateUserAccountUseCase createUserAccountUseCase,
            UpdateUserAccountUseCase updateUserAccountUseCase,
            DeactivateUserAccountUseCase deactivateUserAccountUseCase,
            UserAccountPresentationMapper userAccountPresentationMapper) {
        this.listUserAccountsUseCase = listUserAccountsUseCase;
        this.getUserAccountUseCase = getUserAccountUseCase;
        this.createUserAccountUseCase = createUserAccountUseCase;
        this.updateUserAccountUseCase = updateUserAccountUseCase;
        this.deactivateUserAccountUseCase = deactivateUserAccountUseCase;
        this.userAccountPresentationMapper = userAccountPresentationMapper;
    }

    @GetMapping
    @PreAuthorize(STAFF_ACCOUNT_MANAGE_PERMISSION)
    @Operation(
            summary = "List user accounts",
            description = "Returns all user accounts available for staff account administration.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User accounts loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Bearer token is missing, invalid, or expired."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Current user does not have STAFF_ACCOUNT_MANAGE permission.")
    })
    public ApiResponse<List<UserAccountResponse>> listUserAccounts() {
        List<UserAccountResponse> userAccounts = listUserAccountsUseCase.listUserAccounts().stream()
                .map(userAccountPresentationMapper::toResponse)
                .toList();

        return ApiResponse.success("User accounts loaded successfully.", userAccounts);
    }

    @GetMapping("/{userId}")
    @PreAuthorize(STAFF_ACCOUNT_MANAGE_PERMISSION)
    @Operation(
            summary = "Get user account detail",
            description = "Returns detailed information for one user account by id.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User account loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User account does not exist.")
    })
    public ApiResponse<UserAccountResponse> getUserAccount(
            @PathVariable @Positive(message = "User id must be greater than 0.") Long userId) {
        return ApiResponse.success(
                "User account loaded successfully.",
                userAccountPresentationMapper.toResponse(getUserAccountUseCase.getUserAccount(userId)));
    }

    @PostMapping
    @PreAuthorize(STAFF_ACCOUNT_MANAGE_PERMISSION)
    @Operation(
            summary = "Create user account",
            description = "Creates a new user account and assigns it to an existing user group.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserAccountResponse> createUserAccount(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(
                "User account created successfully.",
                userAccountPresentationMapper.toResponse(
                        createUserAccountUseCase.createUserAccount(
                                new CreateUserAccountCommand(
                                        request.username(),
                                        request.password(),
                                        request.fullName(),
                                        request.email(),
                                        request.phoneNumber(),
                                        request.groupId(),
                                        request.status()))));
    }

    @PutMapping("/{userId}")
    @PreAuthorize(STAFF_ACCOUNT_MANAGE_PERMISSION)
    @Operation(
            summary = "Update user account",
            description = "Updates profile and group assignment for an existing user account.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserAccountResponse> updateUserAccount(
            @PathVariable @Positive(message = "User id must be greater than 0.") Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(
                "User account updated successfully.",
                userAccountPresentationMapper.toResponse(
                        updateUserAccountUseCase.updateUserAccount(
                                userId,
                                new UpdateUserAccountCommand(
                                        request.username(),
                                        request.fullName(),
                                        request.email(),
                                        request.phoneNumber(),
                                        request.groupId(),
                                        request.status()))));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize(STAFF_ACCOUNT_MANAGE_PERMISSION)
    @Operation(
            summary = "Deactivate user account",
            description = "Marks an existing user account as inactive instead of hard-deleting it.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deactivateUserAccount(
            @PathVariable @Positive(message = "User id must be greater than 0.") Long userId) {
        deactivateUserAccountUseCase.deactivateUserAccount(userId);
        return ApiResponse.success("User account deactivated successfully.");
    }
}
