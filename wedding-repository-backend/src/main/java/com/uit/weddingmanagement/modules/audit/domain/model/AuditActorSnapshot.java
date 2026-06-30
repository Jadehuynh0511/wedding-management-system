package com.uit.weddingmanagement.modules.audit.domain.model;

/**
 * Snapshot actor được lưu cùng audit log để giữ lại ngữ cảnh tại thời điểm thao
 * tác. Điều này rất quan trọng vì thông tin về người dùng (actor) có thể thay
 * đổi theo thời gian (ví dụ: username có thể được cập nhật, hoặc người dùng có
 * thể bị xóa). Bằng cách lưu một snapshot của actor tại thời điểm ghi log,
 * chúng ta đảm bảo rằng thông tin về ai đã thực hiện hành động vẫn được giữ
 * nguyên và chính xác trong audit log, ngay cả khi thông tin người dùng gốc
 * thay đổi sau đó.
 */
public record AuditActorSnapshot(
        Long userId,
        String username,
        String groupName) {

    // Factory method để tạo một snapshot đại diện cho hệ thống khi không có thông
    // tin người dùng nào có thể thu thập được, ví dụ: khi hành động được thực hiện
    // bởi một process nền hoặc khi có lỗi xảy ra trong việc lấy thông tin người
    // dùng hiện tại.
    public static AuditActorSnapshot system() {
        return new AuditActorSnapshot(null, "SYSTEM", "SYSTEM");
    }
}
