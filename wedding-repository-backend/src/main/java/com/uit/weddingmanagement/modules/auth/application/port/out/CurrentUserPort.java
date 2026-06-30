package com.uit.weddingmanagement.modules.auth.application.port.out;

import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;

// Port này là cầu nối từ application layer sang SecurityContext hiện tại.
// Nhờ vậy use case lấy current user mà không phụ thuộc trực tiếp vào Spring Security.
public interface CurrentUserPort {

    AuthenticatedUser getCurrentUser();
}
