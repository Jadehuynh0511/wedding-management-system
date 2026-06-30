package com.uit.weddingmanagement.modules.auth.presentation.dto.request;

import com.uit.weddingmanagement.modules.auth.domain.model.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Dto để validate request khi tạo user thông qua API
public record CreateUserRequest(
                @NotBlank(message = "Username is required.") @Size(max = 100, message = "Username must not exceed 100 characters.") @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username must contain only letters, numbers, dot, underscore, or hyphen.") String username,
                @NotBlank(message = "Password is required.") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.") String password,
                @NotBlank(message = "Full name is required.") @Size(max = 150, message = "Full name must not exceed 150 characters.") String fullName,
                @Email(message = "Email must be a valid email address.") @Size(max = 255, message = "Email must not exceed 255 characters.") String email,
                @Size(max = 20, message = "Phone number must not exceed 20 characters.") String phoneNumber,
                @NotNull(message = "Group id is required.") @Positive(message = "Group id must be greater than 0.") Long groupId,
                UserStatus status) {
}
