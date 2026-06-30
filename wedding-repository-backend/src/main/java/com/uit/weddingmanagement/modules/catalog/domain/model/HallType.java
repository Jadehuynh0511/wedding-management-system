package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.math.BigDecimal;

// Domain model của loại sảnh tự giữ invariant cốt lõi để mọi luồng tạo/cập nhật đều dùng chung
// một luật nghiệp vụ thống nhất.
public record HallType(
        Long id,
        String hallTypeName,
        BigDecimal minimumTablePrice,
        String description) {

    // Constructor chính, sẽ được gọi bởi cả factory method create và update, đảm bảo mọi
    // instance của HallType đều phải tuân theo cùng một logic validate và normalize.
    public HallType {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Hall type id must be greater than 0.");
        }

        hallTypeName = normalizeHallTypeName(hallTypeName);
        minimumTablePrice = requireMinimumTablePrice(minimumTablePrice);
        description = normalizeDescription(description);
    }

    public static HallType create(
            String hallTypeName,
            BigDecimal minimumTablePrice,
            String description) {
        return new HallType(null, hallTypeName, minimumTablePrice, description);
    }

    public HallType update(
            String hallTypeName,
            BigDecimal minimumTablePrice,
            String description) {
        if (id == null) {
            throw new IllegalStateException("Cannot update a hall type without id.");
        }

        return new HallType(id, hallTypeName, minimumTablePrice, description);
    }

    private static String normalizeHallTypeName(String hallTypeName) {
        if (hallTypeName == null || hallTypeName.isBlank()) {
            throw new IllegalArgumentException("Hall type name is required.");
        }

        return hallTypeName.trim().replaceAll("\\s+", " ");
    }

    private static BigDecimal requireMinimumTablePrice(BigDecimal minimumTablePrice) {
        if (minimumTablePrice == null) {
            throw new IllegalArgumentException("Minimum table price is required.");
        }

        if (minimumTablePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum table price must be greater than 0.");
        }

        return minimumTablePrice;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
