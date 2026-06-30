package com.uit.weddingmanagement.modules.auth.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Cấu hình security tổng cho auth module:
// - access token stateless
// - login/refresh/logout public
// - mọi API business còn lại cần access token hợp lệ
@Configuration
@EnableMethodSecurity // Enable chế độ check quyền ở mức method bằng annotation @PreAuthorize, @PostAuthorize, @Secured, ...
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
        private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
                        JsonAccessDeniedHandler jsonAccessDeniedHandler) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
                this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
        }

        // Tạo bean SecurityFilterChain cho security
        // @Bean này được gắn lên một method để nói với Spring: "lấy object method này
        // về, lưu vào Spring context để chỗ khác dùng lại"
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                httpSecurity
                                .csrf(AbstractHttpConfigurer::disable) // Disable chống CSRF vì chúng ta dùng JWT, kh cần CSRF token (chỉ cần với session-based auth)
                                .cors(Customizer.withDefaults()) // Enable CORS với cấu hình mặc định (có thể tùy chỉnh nếu cần)
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessin management vì dùng JWT, kh lưu session trên server. Mỗi request tự cầm theo token để chứng minh mình là ai
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                                                .accessDeniedHandler(jsonAccessDeniedHandler)) // Khi auth fail (401) hoặc forbiden (403) -> trả JSON theo ApiResponse thay vì HTML mặc định của Spring Security
                                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/refresh",
                                                                "/api/auth/logout",
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/error",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**")
                                                .permitAll() // Cho phép public các endpoint này (không cần token)
                                                .anyRequest() // Các endpoint còn lại cần phải được xác thực (có token hợp lệ)
                                                .authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Chèn filter JwtAuthenticationFilter trước filter mặc định của Spring -> đọc token sớm

                return httpSecurity.build();
        }

        // Tạo bean PasswordEncoder để mã hóa password
        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
