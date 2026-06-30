package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import java.io.IOException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.uit.weddingmanagement.modules.auth.application.port.in.ResolveAuthenticatedUserUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.in.ValidateAuthenticatedSessionUseCase;
import com.uit.weddingmanagement.modules.auth.application.port.out.TokenProviderPort;
import com.uit.weddingmanagement.modules.auth.domain.exception.InactiveAuthenticatedSessionException;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Filter chạy 1 lần mỗi request để:
// 1. đọc Bearer access token
// 2. parse userId + sessionFamilyId từ JWT
// 3. xác minh session family vẫn active
// 4. reload user + permissions từ DB
// 5. đưa principal vào SecurityContext
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProviderPort tokenProviderPort;
    private final ValidateAuthenticatedSessionUseCase validateAuthenticatedSessionUseCase;
    private final ResolveAuthenticatedUserUseCase resolveAuthenticatedUserUseCase;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            TokenProviderPort tokenProviderPort,
            ValidateAuthenticatedSessionUseCase validateAuthenticatedSessionUseCase,
            ResolveAuthenticatedUserUseCase resolveAuthenticatedUserUseCase,
            AuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenProviderPort = tokenProviderPort;
        this.validateAuthenticatedSessionUseCase = validateAuthenticatedSessionUseCase;
        this.resolveAuthenticatedUserUseCase = resolveAuthenticatedUserUseCase;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            TokenProviderPort.TokenSubject tokenSubject = tokenProviderPort.parseAccessToken(token);

            validateAuthenticatedSessionUseCase.ensureSessionIsActive(
                    tokenSubject.userId(),
                    tokenSubject.sessionFamilyId());

            AuthenticatedUser authenticatedUser = resolveAuthenticatedUserUseCase.resolveById(tokenSubject.userId());
            Authentication authentication = buildAuthentication(authenticatedUser, request);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException
                | InactiveAuthenticatedSessionException
                | EntityNotFoundException
                | IllegalStateException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("Invalid or expired access token.", exception));
        }
    }

    private Authentication buildAuthentication(AuthenticatedUser authenticatedUser, HttpServletRequest request) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(authenticatedUser);
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authenticationToken =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }
}
