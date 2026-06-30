package com.uit.weddingmanagement.modules.catalog.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListHallTypesUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.CreateHallTypeRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateHallTypeRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.HallTypeResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.mapper.HallTypePresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller thực hiện các API quản lý loại sảnh cưới, bao gồm CRUD cho loại sảnh. Các API này yêu
// cầu quyền HALL_TYPE_MANAGE để đảm bảo chỉ người dùng có quyền mới có thể truy cập và
// thực hiện các thao tác quản lý loại sảnh cưới.
@Validated
@RestController
@RequestMapping("/api/hall-types")
@Tag(name = "Hall Types", description = "APIs for managing wedding hall types.")
public class HallTypeController {
    // Định nghĩa một constant cho biểu thức SpEL kiểm tra quyền HALL_TYPE_MANAGE,
    // giúp code dễ đọc và bảo trì hơn.
    private static final String HALL_TYPE_MANAGE_PERMISSION = "@authorizationService.hasPermission('HALL_TYPE_MANAGE')";

    // Đây là IoC (Inversion of Control) container pattern, nơi controller không
    // trực tiếp tạo ra các use case mà là nhận
    // chúng từ bên ngoài (thông qua constructor).
    private final ListHallTypesUseCase listHallTypesUseCase;
    private final GetHallTypeUseCase getHallTypeUseCase;
    private final CreateHallTypeUseCase createHallTypeUseCase;
    private final UpdateHallTypeUseCase updateHallTypeUseCase;
    private final DeleteHallTypeUseCase deleteHallTypeUseCase;
    private final HallTypePresentationMapper hallTypePresentationMapper;

    // Đây là Dependency Injection thông qua constructor, giúp Spring tự động
    // inject các use case cần thiết vào controller này khi khởi tạo.
    public HallTypeController(
            ListHallTypesUseCase listHallTypesUseCase,
            GetHallTypeUseCase getHallTypeUseCase,
            CreateHallTypeUseCase createHallTypeUseCase,
            UpdateHallTypeUseCase updateHallTypeUseCase,
            DeleteHallTypeUseCase deleteHallTypeUseCase,
            HallTypePresentationMapper hallTypePresentationMapper) {
        this.listHallTypesUseCase = listHallTypesUseCase;
        this.getHallTypeUseCase = getHallTypeUseCase;
        this.createHallTypeUseCase = createHallTypeUseCase;
        this.updateHallTypeUseCase = updateHallTypeUseCase;
        this.deleteHallTypeUseCase = deleteHallTypeUseCase;
        this.hallTypePresentationMapper = hallTypePresentationMapper;
    }

    // Các API trong controller này đều được bảo vệ bằng @PreAuthorize với biểu thức
    // SpEL sử dụng bean "authorizationService" để kiểm tra xem người dùng hiện tại
    // có quyền HALL_TYPE_MANAGE hay không

    // Lấy danh sách tất cả các loại sảnh cưới
    @GetMapping
    @PreAuthorize(HALL_TYPE_MANAGE_PERMISSION)
    @Operation(summary = "List hall types", description = "Returns all configured hall types for catalog management.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hall types loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, or expired."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Current user does not have HALL_TYPE_MANAGE permission.")
    })
    public ApiResponse<List<HallTypeResponse>> listHallTypes() {
        List<HallTypeResponse> hallTypes = listHallTypesUseCase.listHallTypes().stream()
                .map(hallTypePresentationMapper::toResponse)
                .toList();

        return ApiResponse.success("Hall types loaded successfully.", hallTypes);
    }

    // Lấy chi tiết một loại sảnh cưới theo ID
    @GetMapping("/{hallTypeId}")
    @PreAuthorize(HALL_TYPE_MANAGE_PERMISSION)
    @Operation(summary = "Get one hall type", description = "Returns details for the requested hall type.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hall type loaded successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hall type does not exist.")
    })
    public ApiResponse<HallTypeResponse> getHallType(
            @PathVariable @Positive(message = "Hall type id must be greater than 0.") Long hallTypeId) {
        return ApiResponse.success(
                "Hall type loaded successfully.",
                hallTypePresentationMapper.toResponse(getHallTypeUseCase.getHallType(hallTypeId)));
    }

    // Tạo mới một loại sảnh cưới, yêu cầu tên loại sảnh phải duy nhất và không được
    // trùng với các loại sảnh đã tồn tại.
    @PostMapping
    @PreAuthorize(HALL_TYPE_MANAGE_PERMISSION)
    @Operation(summary = "Create hall type", description = "Creates a new hall type after validating unique hall type name.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<HallTypeResponse> createHallType(
            @Valid @RequestBody CreateHallTypeRequest request) {
        return ApiResponse.success(
                "Hall type created successfully.",
                hallTypePresentationMapper.toResponse(
                        createHallTypeUseCase.createHallType(
                                new CreateHallTypeCommand(
                                        request.hallTypeName(), request.minimumTablePrice(), request.description()))));
    }

    // Cập nhật thông tin một loại sảnh cưới, yêu cầu tên loại sảnh phải duy nhất và
    // không được
    // trùng với các loại sảnh đã tồn tại.
    @PutMapping("/{hallTypeId}")
    @PreAuthorize(HALL_TYPE_MANAGE_PERMISSION)
    @Operation(summary = "Update hall type", description = "Updates hall type information after validating hall type uniqueness.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<HallTypeResponse> updateHallType(
            @PathVariable @Positive(message = "Hall type id must be greater than 0.") Long hallTypeId,
            @Valid @RequestBody UpdateHallTypeRequest request) {
        return ApiResponse.success(
                "Hall type updated successfully.",
                hallTypePresentationMapper.toResponse(
                        updateHallTypeUseCase.updateHallType(
                                hallTypeId,
                                new UpdateHallTypeCommand(
                                        request.hallTypeName(), request.minimumTablePrice(), request.description()))));
    }

    // Xóa một loại sảnh cưới, chỉ cho phép xóa khi không có sảnh nào đang tham
    // chiếu đến loại sảnh này để đảm bảo tính toàn vẹn dữ liệu.
    @DeleteMapping("/{hallTypeId}")
    @PreAuthorize(HALL_TYPE_MANAGE_PERMISSION)
    @Operation(summary = "Delete hall type", description = "Deletes a hall type when there are no halls referencing it.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteHallType(
            @PathVariable @Positive(message = "Hall type id must be greater than 0.") Long hallTypeId) {
        deleteHallTypeUseCase.deleteHallType(hallTypeId);
        return ApiResponse.success("Hall type deleted successfully.");
    }
}
