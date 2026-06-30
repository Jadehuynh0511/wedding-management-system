package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.BookingServiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingServiceJpaRepository extends JpaRepository<BookingServiceJpaEntity, Long> {

  boolean existsByServiceItem_Id(Long serviceItemId);
}
