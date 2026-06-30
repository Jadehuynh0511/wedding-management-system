package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.BookingMenuItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingMenuItemJpaRepository extends JpaRepository<BookingMenuItemJpaEntity, Long> {

  boolean existsByMenuItem_Id(Long menuItemId);
}
