package com.uit.weddingmanagement.modules.booking.presentation.mapper;

import com.uit.weddingmanagement.modules.booking.application.model.command.CreateCancellationReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateDepositReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptItemCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateInvoiceCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingMenuItemCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingServiceCommand;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateCancellationReceiptRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateIncidentalReceiptRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateInvoiceRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateWeddingBookingRequest;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi Request DTOs sang application-layer Command objects.
 * Được tách ra từ BookingController để tuân thủ SRP.
 * Command objects là input của domain UseCases (được sử dụng trong application
 * layer).
 * Command objects có ý nghĩa là dữ liệu cần cho 1 hành động nào đó.
 * Việc tách ra command để làm input cho usecase mà không lấy requestDto trực
 * tiếp giúp cho interface của usecase không bị phụ thuộc vào presentation
 * layer, và giúp cho presentation layer có thể thay đổi mà không ảnh hưởng đến
 * application layer
 */
@Component
public class BookingCommandMapper {

        public CreateWeddingBookingCommand toCommand(CreateWeddingBookingRequest request) {
                return new CreateWeddingBookingCommand(
                                request.hallId(),
                                request.shiftId(),
                                request.groomName(),
                                request.brideName(),
                                request.groomPhoneNumber(),
                                request.bridePhoneNumber(),
                                request.celebrationDate(),
                                request.tableCount(),
                                request.reservedTableCount(),
                                request.notes(),
                                request.menuItems().stream()
                                                .map(menuItem -> new CreateWeddingBookingMenuItemCommand(
                                                                menuItem.menuItemId(), menuItem.quantity(),
                                                                menuItem.notes()))
                                                .toList(),
                                request.services() == null
                                                ? List.of()
                                                : request.services().stream()
                                                                .map(service -> new CreateWeddingBookingServiceCommand(
                                                                                service.serviceId(), service.quantity(),
                                                                                service.notes()))
                                                                .toList(),
                                new CreateDepositReceiptCommand(
                                                request.depositReceipt().amount(),
                                                request.depositReceipt().paymentMethod(),
                                                request.depositReceipt().notes()));
        }

        public CreateIncidentalReceiptCommand toCommand(CreateIncidentalReceiptRequest request) {
                return new CreateIncidentalReceiptCommand(
                                request.notes(),
                                request.items().stream()
                                                .map(item -> new CreateIncidentalReceiptItemCommand(
                                                                item.serviceId(), item.quantity(), item.notes()))
                                                .toList());
        }

        public CreateCancellationReceiptCommand toCommand(CreateCancellationReceiptRequest request) {
                return new CreateCancellationReceiptCommand(request.reason());
        }

        public CreateInvoiceCommand toCommand(CreateInvoiceRequest request) {
                return new CreateInvoiceCommand(request == null ? null : request.notes());
        }
}
