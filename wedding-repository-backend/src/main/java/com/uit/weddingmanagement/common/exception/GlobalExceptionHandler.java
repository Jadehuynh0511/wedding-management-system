package com.uit.weddingmanagement.common.exception;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.auth.domain.exception.AdminPrivilegeRequiredException;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidCredentialsException;
import com.uit.weddingmanagement.modules.auth.domain.exception.InvalidRefreshTokenException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// @RestControllerAdvice dùng để thống nhất cách trả lỗi cho toàn bộ API.
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    return ResponseEntity.badRequest()
        .body(
            ApiResponse.error(
                "VALIDATION_ERROR",
                "Validation failed for request payload.",
                extractFieldErrors(exception.getBindingResult().getFieldErrors())));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
      BindException exception) {
    return ResponseEntity.badRequest()
        .body(
            ApiResponse.error(
                "BIND_ERROR",
                "Request parameters could not be bound.",
                extractFieldErrors(exception.getFieldErrors())));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
      ConstraintViolationException exception) {
    Map<String, String> violations = new LinkedHashMap<>();

    exception
        .getConstraintViolations()
        .forEach(
            violation ->
                violations.put(violation.getPropertyPath().toString(), violation.getMessage()));

    return ResponseEntity.badRequest()
        .body(
            ApiResponse.error(
                "CONSTRAINT_VIOLATION", "Validation failed for request parameters.", violations));
  }

  // Bắt lỗi khi request parameter có kiểu dữ liệu không hợp lệ, ví dụ id phải là Long nhưng client gửi lên chuỗi "abc".
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    return ResponseEntity.badRequest()
        .body(
            ApiResponse.error(
                "INVALID_REQUEST_PARAMETER",
                "Request parameter '"
                    + exception.getName()
                    + "' has invalid value '"
                    + exception.getValue()
                    + "'."));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("RESOURCE_NOT_FOUND", exception.getMessage()));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
      InvalidCredentialsException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("INVALID_CREDENTIALS", exception.getMessage()));
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(
      InvalidRefreshTokenException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("INVALID_REFRESH_TOKEN", exception.getMessage()));
  }

  @ExceptionHandler(AdminPrivilegeRequiredException.class)
  public ResponseEntity<ApiResponse<Void>> handleAdminPrivilegeRequired(
      AdminPrivilegeRequiredException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error("FORBIDDEN", exception.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ApiResponse.error("FORBIDDEN", "You do not have permission to access this resource."));
  }

  // Exception này được ném ra khi cố gắng tạo hoặc cập nhật một tài nguyên với dữ
  // liệu trùng lặp, ví dụ như tên loại sảnh đã tồn tại.
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
      DuplicateResourceException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("RESOURCE_CONFLICT", exception.getMessage()));
  }

  // Exception này được ném ra khi cố gắng xóa một tài nguyên đang được tham chiếu
  // bởi các tài nguyên khác, ví dụ như cố gắng xóa một loại sảnh đang có sảnh
  // tham chiếu đến nó.
  @ExceptionHandler(ResourceInUseException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceInUse(ResourceInUseException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("RESOURCE_IN_USE", exception.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
      IllegalArgumentException exception) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("INVALID_REQUEST", exception.getMessage()));
  }

  // Bắt tất cả các exception khác chưa được xử lý ở trên để tránh rò rỉ thông tin nhạy cảm và đảm bảo trả về
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred."));
  }

  private Map<String, String> extractFieldErrors(Iterable<FieldError> fieldErrors) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError fieldError : fieldErrors) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return errors;
  }
}
