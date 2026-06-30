package com.uit.weddingmanagement.modules.auth.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.common.util.HttpRequestUtils;
import com.uit.weddingmanagement.modules.auth.application.model.command.LoginCommand;
import com.uit.weddingmanagement.modules.auth.application.model.command.LogoutCommand;
import com.uit.weddingmanagement.modules.auth.application.model.command.RefreshSessionCommand;
import com.uit.weddingmanagement.modules.auth.application.port.in.GetCurrentUserUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.LoginUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.LogoutUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.RefreshSessionUseCase;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.LoginRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.LogoutRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.request.RefreshTokenRequest;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.CurrentUserResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.LoginResponse;
import com.uit.weddingmanagement.modules.auth.presentation.dto.response.RefreshTokenResponse;
import com.uit.weddingmanagement.modules.auth.presentation.mapper.AuthPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controller chỉ làm nhiệm vụ HTTP: nhận request, gọi use case, rồi map sang response DTO.
// Business logic thật nằm ở application layer.
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication, refresh, logout, and current-user endpoints.")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final AuthPresentationMapper authPresentationMapper;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            LogoutUseCase logoutUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            AuthPresentationMapper authPresentationMapper) {
        this.loginUseCase = loginUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.authPresentationMapper = authPresentationMapper;
    }

    /**
     * Endpoint để đăng nhập, nhận username, password, IP và user agent, trả về
     * access token và refresh token nếu thành công. Cũng thu thập thông tin IP và
     * user agent để hỗ trợ phân tích và phát hiện lạm dụng refresh token sau này.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate with username and password", description = "Validates credentials and returns a JWT access token plus an opaque refresh token.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token pair issued successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request payload validation failed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credentials are invalid.")
    })
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(
                "Login successful.",
                authPresentationMapper.toResponse(
                        loginUseCase.login(new LoginCommand(
                                request.username(),
                                request.password(),
                                HttpRequestUtils.resolveClientIp(httpRequest),
                                HttpRequestUtils.resolveUserAgent(httpRequest)))));
    }

    // Endpoint để refresh token, nhận refresh token, IP và user agent, trả về token
    // pair mới nếu thành công. Cũng thu thập thông tin IP và user agent để hỗ trợ
    // phân tích và phát hiện lạm dụng refresh token (ví dụ: token bị đánh cắp và sử
    // dụng từ một thiết bị khác).
    @PostMapping("/refresh")
    @Operation(summary = "Rotate the current session", description = "Validates the supplied refresh token, rotates it, and returns a new token pair.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token pair refreshed successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request payload validation failed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or already reused.")
    })
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(
                "Session refreshed successfully.",
                authPresentationMapper.toResponse(
                        refreshSessionUseCase.refresh(new RefreshSessionCommand(
                                request.refreshToken(),
                                HttpRequestUtils.resolveClientIp(httpRequest),
                                HttpRequestUtils.resolveUserAgent(httpRequest)))));
    }

    // Endpoint để logout, nhận refresh token, revoke toàn bộ family của token đó để
    // đảm bảo các refresh token còn hoạt động khác trong cùng family cũng bị
    // revoke, đồng thời làm cho các access token còn hoạt động liên quan đến family
    // đó cũng trở nên vô hiệu (do kiểm tra familyId trong access token validation).
    @PostMapping("/logout")
    @Operation(summary = "Logout the current session", description = "Revokes the refresh-token family so future refresh attempts and active access checks fail.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout completed successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request payload validation failed.")
    })
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(new LogoutCommand(request.refreshToken()));
        return ApiResponse.success("Logout successful.");
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user", description = "Resolves the authenticated principal from the bearer token and returns its RBAC data.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or belongs to a revoked session.")
    })
    public ApiResponse<CurrentUserResponse> getCurrentUser() {
        return ApiResponse.success(
                "Current user loaded successfully.",
                authPresentationMapper.toResponse(getCurrentUserUseCase.getCurrentUser()));
    }
}
