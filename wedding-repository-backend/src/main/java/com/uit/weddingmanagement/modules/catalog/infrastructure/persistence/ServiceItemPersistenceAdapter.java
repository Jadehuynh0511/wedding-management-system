package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence;

import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryQueryPort;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.BookingServiceJpaRepository;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ServiceItemJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ServicePriceHistoryJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.IncidentalReceiptItemJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.ServiceItemJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.ServicePriceHistoryJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ServiceItemPersistenceAdapter
    implements ServiceItemQueryPort,
        ServiceItemCommandPort,
        ServiceItemReferenceQueryPort,
        ServicePriceHistoryQueryPort,
        ServicePriceHistoryCommandPort {

  private final ServiceItemJpaRepository serviceItemJpaRepository;
  private final ServicePriceHistoryJpaRepository servicePriceHistoryJpaRepository;
  private final BookingServiceJpaRepository bookingServiceJpaRepository;
  private final IncidentalReceiptItemJpaRepository incidentalReceiptItemJpaRepository;

  public ServiceItemPersistenceAdapter(
      ServiceItemJpaRepository serviceItemJpaRepository,
      ServicePriceHistoryJpaRepository servicePriceHistoryJpaRepository,
      BookingServiceJpaRepository bookingServiceJpaRepository,
      IncidentalReceiptItemJpaRepository incidentalReceiptItemJpaRepository) {
    this.serviceItemJpaRepository = serviceItemJpaRepository;
    this.servicePriceHistoryJpaRepository = servicePriceHistoryJpaRepository;
    this.bookingServiceJpaRepository = bookingServiceJpaRepository;
    this.incidentalReceiptItemJpaRepository = incidentalReceiptItemJpaRepository;
  }

  @Override
  public List<ServiceItem> findServiceItems(Boolean active) {
    ServiceItemStatus status = mapActivityToStatus(active);

    return serviceItemJpaRepository.findAllByOptionalStatus(status).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<ServiceItem> findServiceItemById(Long serviceItemId) {
    return serviceItemJpaRepository.findById(serviceItemId).map(this::toDomain);
  }

  @Override
  public boolean existsServiceItemByName(String serviceName) {
    return serviceItemJpaRepository.existsByServiceNameIgnoreCase(serviceName);
  }

  @Override
  public boolean existsServiceItemByNameAndIdNot(String serviceName, Long serviceItemId) {
    return serviceItemJpaRepository.existsByServiceNameIgnoreCaseAndIdNot(
        serviceName, serviceItemId);
  }

  @Override
  public ServiceItem saveServiceItem(ServiceItem serviceItem) {
    ServiceItemJpaEntity serviceItemJpaEntity = resolveEntityForSave(serviceItem);
    serviceItemJpaEntity.setServiceName(serviceItem.serviceName());
    serviceItemJpaEntity.setServiceCategory(serviceItem.serviceCategory());
    serviceItemJpaEntity.setUnitName(serviceItem.unitName());
    serviceItemJpaEntity.setCurrentPrice(serviceItem.currentPrice());
    serviceItemJpaEntity.setPriceEffectiveFrom(serviceItem.priceEffectiveFrom());
    serviceItemJpaEntity.setStatus(serviceItem.status());
    serviceItemJpaEntity.setDescription(serviceItem.description());

    return toDomain(serviceItemJpaRepository.save(serviceItemJpaEntity));
  }

  @Override
  public void deleteServiceItemById(Long serviceItemId) {
    serviceItemJpaRepository.deleteById(serviceItemId);
  }

  @Override
  public boolean existsAnyServiceReferenceByServiceItemId(Long serviceItemId) {
    return bookingServiceJpaRepository.existsByServiceItem_Id(serviceItemId)
        || incidentalReceiptItemJpaRepository.existsByServiceItem_Id(serviceItemId);
  }

  @Override
  public List<ServicePriceHistory> findServicePriceHistoriesByServiceItemId(Long serviceItemId) {
    return servicePriceHistoryJpaRepository
        .findByServiceItem_IdOrderByEffectiveToDescIdDesc(serviceItemId)
        .stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public ServicePriceHistory saveServicePriceHistory(ServicePriceHistory servicePriceHistory) {
    ServicePriceHistoryJpaEntity servicePriceHistoryJpaEntity = new ServicePriceHistoryJpaEntity();
    servicePriceHistoryJpaEntity.setServiceItem(
        resolveServiceEntity(servicePriceHistory.serviceItemId()));
    servicePriceHistoryJpaEntity.setOldPrice(servicePriceHistory.oldPrice());
    servicePriceHistoryJpaEntity.setEffectiveFrom(servicePriceHistory.effectiveFrom());
    servicePriceHistoryJpaEntity.setEffectiveTo(servicePriceHistory.effectiveTo());

    return toDomain(servicePriceHistoryJpaRepository.save(servicePriceHistoryJpaEntity));
  }

  private ServiceItemStatus mapActivityToStatus(Boolean active) {
    if (active == null) {
      return null;
    }

    return active ? ServiceItemStatus.HOAT_DONG : ServiceItemStatus.NGUNG_HOAT_DONG;
  }

  private ServiceItemJpaEntity resolveEntityForSave(ServiceItem serviceItem) {
    if (serviceItem.id() == null) {
      return new ServiceItemJpaEntity();
    }

    return resolveServiceEntity(serviceItem.id());
  }

  private ServiceItemJpaEntity resolveServiceEntity(Long serviceItemId) {
    return serviceItemJpaRepository
        .findById(serviceItemId)
        .orElseThrow(
            () -> new EntityNotFoundException("Service item not found with id: " + serviceItemId));
  }

  private ServiceItem toDomain(ServiceItemJpaEntity serviceItemJpaEntity) {
    return new ServiceItem(
        serviceItemJpaEntity.getId(),
        serviceItemJpaEntity.getServiceName(),
        serviceItemJpaEntity.getServiceCategory(),
        serviceItemJpaEntity.getUnitName(),
        serviceItemJpaEntity.getCurrentPrice(),
        serviceItemJpaEntity.getPriceEffectiveFrom(),
        serviceItemJpaEntity.getStatus(),
        serviceItemJpaEntity.getDescription());
  }

  private ServicePriceHistory toDomain(ServicePriceHistoryJpaEntity servicePriceHistoryJpaEntity) {
    return new ServicePriceHistory(
        servicePriceHistoryJpaEntity.getId(),
        servicePriceHistoryJpaEntity.getServiceItem().getId(),
        servicePriceHistoryJpaEntity.getOldPrice(),
        servicePriceHistoryJpaEntity.getEffectiveFrom(),
        servicePriceHistoryJpaEntity.getEffectiveTo());
  }
}
