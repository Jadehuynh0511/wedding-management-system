package com.uit.weddingmanagement.modules.auth.domain.exception;

// Exception này được ném ra khi use case quản trị RBAC bị gọi bởi user không thuộc ADMIN. Rule "chỉ ADMIN được cấp/thu hồi quyền"
// cần sống ở application/domain, không chỉ ở controller. Điều này đảm bảo rằng dù có ai đó vô tình hay cố ý gọi thẳng service này mà không qua controller thì cũng sẽ bị chặn lại nếu không phải là admin.
public class AdminPrivilegeRequiredException extends RuntimeException {

    public AdminPrivilegeRequiredException() {
        super("Only ADMIN can assign or revoke permissions.");
    }
}
