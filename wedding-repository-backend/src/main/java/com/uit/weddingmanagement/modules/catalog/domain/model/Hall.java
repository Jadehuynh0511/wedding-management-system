package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.math.BigDecimal;

// Domain model này tự giữ invariant cốt lõi của sảnh để mọi luồng tạo/cập nhật đều phải đi qua
// cùng một bộ luật nghiệp vụ.
public record Hall(
        Long id,
        HallType hallType,
        String hallName,
        Integer maxCapacity,
        BigDecimal tablePrice,
        HallStatus status,
        String description) {

    // Constructor chính, sẽ được gọi bởi cả factory method create và update, đảm bảo mọi
    // instance của Hall đều phải tuân theo cùng một logic validate và normalize.
    public Hall {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Hall id must be greater than 0.");
        }

        if (hallType == null) {
            throw new IllegalArgumentException("Hall type is required.");
        }

        hallName = normalizeHallName(hallName);
        maxCapacity = requireMaxCapacity(maxCapacity);
        tablePrice = requireTablePrice(tablePrice);
        status = requireStatus(status);
        description = normalizeDescription(description);

        // Đây là invariant quan trọng nhất của sảnh: giá bàn không bao giờ được thấp hơn ngưỡng
        // tối thiểu của loại sảnh đang gắn vào nó.
        ensureTablePriceMatchesHallType(tablePrice, hallType.minimumTablePrice());
    }

    // Factory method để tạo sảnh mới
    public static Hall create(
            HallType hallType,
            String hallName,
            Integer maxCapacity,
            BigDecimal tablePrice,
            HallStatus status,
            String description) {
        // Khi tạo mới mà client chưa chọn trạng thái, domain tự áp mặc định TRONG để tránh phải
        // lặp rule này ở từng use case.
        HallStatus normalizedStatus = status == null ? HallStatus.TRONG : status;

        return new Hall(null, hallType, hallName, maxCapacity, tablePrice, normalizedStatus, description);
    }

    // Factory method để cập nhật sảnh hiện có, sẽ trả về một instance mới với các trường được cập nhật theo input.
    public Hall update(
            HallType hallType,
            String hallName,
            Integer maxCapacity,
            BigDecimal tablePrice,
            HallStatus status,
            String description) {
        if (id == null) {
            throw new IllegalStateException("Cannot update a hall without id.");
        }

        return new Hall(id, hallType, hallName, maxCapacity, tablePrice, status, description);
    }

    // Các hàm validate và normalize riêng biệt giúp tách bạch rõ ràng các rule nghiệp vụ liên quan đến từng field,
    // đồng thời đảm bảo mọi luồng tạo/cập nhật sảnh đều phải tuân theo cùng một logic chuẩn hóa và kiểm tra.

    private static String normalizeHallName(String hallName) {
        if (hallName == null || hallName.isBlank()) {
            throw new IllegalArgumentException("Hall name is required.");
        }

        return hallName.trim().replaceAll("\\s+", " ");
    }

    private static Integer requireMaxCapacity(Integer maxCapacity) {
        if (maxCapacity == null) {
            throw new IllegalArgumentException("Max capacity is required.");
        }

        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than 0.");
        }

        return maxCapacity;
    }

    private static BigDecimal requireTablePrice(BigDecimal tablePrice) {
        if (tablePrice == null) {
            throw new IllegalArgumentException("Table price is required.");
        }

        if (tablePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Table price must be greater than 0.");
        }

        return tablePrice;
    }

    private static HallStatus requireStatus(HallStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Hall status is required.");
        }

        return status;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private static void ensureTablePriceMatchesHallType(
            BigDecimal tablePrice,
            BigDecimal minimumTablePrice) {
        if (minimumTablePrice == null) {
            throw new IllegalArgumentException("Minimum table price of the hall type is required.");
        }

        if (tablePrice.compareTo(minimumTablePrice) < 0) {
            throw new IllegalArgumentException(
                    "Table price must be greater than or equal to minimum table price of the hall type.");
        }
    }
}
