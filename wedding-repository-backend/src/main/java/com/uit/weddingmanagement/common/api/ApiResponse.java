package com.uit.weddingmanagement.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

// Wrapper response chung cho toàn bộ API
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiResponse", description = "Standard API response envelope.")
public record ApiResponse<T>(
    @Schema(description = "Application-level status code.", example = "SUCCESS") String code,
    @Schema(description = "Human-readable response message.", example = "Request completed successfully.") String message,
    @Schema(description = "Payload returned for the request. Null for responses without a body.") T data) {

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>("SUCCESS", message, data);
  }

  public static ApiResponse<Void> success(String message) {
    return new ApiResponse<>("SUCCESS", message, null);
  }

  public static <T> ApiResponse<T> error(String code, String message, T data) {
    return new ApiResponse<>(code, message, data);
  }

  public static ApiResponse<Void> error(String code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}
