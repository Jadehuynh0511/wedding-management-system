package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import java.security.Principal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Principal là object mà Spring Security giữ trong SecurityContext cho request hiện tại.
// Nó bọc AuthenticatedUser domain model để tầng security và application cùng nhìn cùng 1 dữ liệu.
public class AuthenticatedUserPrincipal implements UserDetails, Principal {

    private final AuthenticatedUser authenticatedUser;
    // Danh sách các quyền của user
    private final Set<GrantedAuthority> authorities;

    public AuthenticatedUserPrincipal(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        this.authorities = buildAuthorities(authenticatedUser);
    }

    // Xây dựng danh sách các quyền từ user
    private Set<GrantedAuthority> buildAuthorities(AuthenticatedUser currentUser) {
        Set<GrantedAuthority> grantedAuthorities = currentUser.permissionCodes().stream()
                // Danh sách quyền này được sử dụng để kiểm tra quyền của user
                // Ví dụ: @PreAuthorize("hasAuthority('PERMISSION_VIEW_USER')")
                .map(permissionCode -> new SimpleGrantedAuthority("PERMISSION_" + permissionCode))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Thêm quyền nhóm vào danh sách quyền
        // Quyền nhóm được giữ riêng với quyền của user để kiểm tra ADMIN được rõ ràng.
        grantedAuthorities.add(new SimpleGrantedAuthority("GROUP_" + currentUser.groupName()));

        return grantedAuthorities;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    // Trả về danh sách các quyền của user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Override các method cần thiết của UserDetails để spring security nhận diện
     * user
     */
    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return authenticatedUser.username();
    }

    @Override
    public String getName() {
        return authenticatedUser.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
