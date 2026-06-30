package com.uit.weddingmanagement.modules.audit.infrastructure.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Định nghĩa Annotation @AuditAction để đánh dấu các phương thức cần ghi log
 * audit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
    // Tên hành động, ví dụ: "CREATE", "UPDATE", "DELETE"
    String action();

    // Tên module hoặc phạm vi của hành động, ví dụ: "UserManagement",
    // "OrderProcessing"
    String module();

    // Loại đối tượng bị tác động, ví dụ: "User", "Order"
    String targetType();

    // Biểu thức SpEL để lấy ID của đối tượng bị tác động, ví dụ: "#user.id",
    // "#order.id"
    String targetIdExpression() default "";

    // Biểu thức SpEL để lấy tên hoặc mô tả của đối tượng bị tác động, ví dụ:
    // "#user.username", "#order.description"
    String targetLabelExpression() default "";

    // Biểu thức SpEL để lấy mô tả chi tiết của hành động khi thành công, ví dụ:
    // "'Created user with ID ' + #user.id"
    String successDescriptionExpression() default "";

    // Biểu thức SpEL để lấy mô tả chi tiết của hành động khi thất bại, ví dụ:
    // "'Failed to create user: ' + #exception.message"
    String failureDescriptionExpression() default "";

    // Biểu thức SpEL để lấy thông tin chi tiết khác, ví dụ: "'Additional details: '
    // + #details"
    String detailsExpression() default "";
}
