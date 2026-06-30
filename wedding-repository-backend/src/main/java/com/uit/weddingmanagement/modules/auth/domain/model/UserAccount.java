package com.uit.weddingmanagement.modules.auth.domain.model;

import java.util.Locale;

import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidCredentialsException;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        String fullName,
        String email,
        String phoneNumber,
        UserStatus status,
        UserGroup userGroup) {

    public UserAccount {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("User id must be greater than 0.");
        }

        username = normalizeUsername(username);
        passwordHash = requirePasswordHash(passwordHash);
        fullName = normalizeFullName(fullName);
        email = normalizeEmail(email);
        phoneNumber = normalizePhoneNumber(phoneNumber);
        status = requireStatus(status);
        userGroup = requireUserGroup(userGroup);
    }

    public static UserAccount create(
            String username,
            String passwordHash,
            String fullName,
            String email,
            String phoneNumber,
            UserStatus status,
            UserGroup userGroup) {
        return new UserAccount(null, username, passwordHash, fullName, email, phoneNumber, status, userGroup);
    }

    public UserAccount update(
            String username,
            String fullName,
            String email,
            String phoneNumber,
            UserStatus status,
            UserGroup userGroup) {
        if (id == null) {
            throw new IllegalStateException("Cannot update a user account without id.");
        }

        return new UserAccount(id, username, passwordHash, fullName, email, phoneNumber, status, userGroup);
    }

    public UserAccount deactivate() {
        if (id == null) {
            throw new IllegalStateException("Cannot deactivate a user account without id.");
        }

        return new UserAccount(id, username, passwordHash, fullName, email, phoneNumber, UserStatus.INACTIVE, userGroup);
    }

    public void ensureCanAuthenticate() {
        if (!status.canAuthenticate()) {
            throw new InvalidCredentialsException();
        }
    }

    public AuthenticatedUser toAuthenticatedUser() {
        return new AuthenticatedUser(
                id,
                username,
                fullName,
                userGroup.id(),
                userGroup.name(),
                userGroup.permissionCodes());
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        String normalizedUsername = username.trim();

        if (normalizedUsername.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Username must not contain whitespace.");
        }

        return normalizedUsername;
    }

    private static String requirePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required.");
        }

        return passwordHash;
    }

    private static String normalizeFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        return fullName.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        return phoneNumber.trim();
    }

    private static UserStatus requireStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("User status is required.");
        }

        return status;
    }

    private static UserGroup requireUserGroup(UserGroup userGroup) {
        if (userGroup == null) {
            throw new IllegalArgumentException("User group is required.");
        }

        return userGroup;
    }
}
