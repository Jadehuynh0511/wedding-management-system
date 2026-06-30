package com.uit.weddingmanagement.modules.catalog.application.model.command;

import java.math.BigDecimal;

// Việc tạo model ở tầng application là để đảm bảo rằng các lớp dữ liệu được sử dụng trong các use
// case có
// cấu trúc rõ ràng và tách biệt khỏi các lớp dữ liệu ở tầng presentation hoặc domain.
// Điều này giúp duy trì sự tách biệt giữa các tầng của ứng dụng và làm cho code dễ bảo trì hơn.
public record CreateHallTypeCommand(
    String hallTypeName, BigDecimal minimumTablePrice, String description) {}
