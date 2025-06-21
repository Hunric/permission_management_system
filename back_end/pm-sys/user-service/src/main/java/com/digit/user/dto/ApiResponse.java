package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Standard API Response Format
 * 
 * @author System
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * Response status code
     */
    private String code;
    
    /**
     * Response message
     */
    private String message;
    
    /**
     * Response data
     */
    private T data;
    
    // Constructor using HttpStatusCode enum
    public ApiResponse(HttpStatusCode statusCode, String message, T data) {
        this.code = String.valueOf(statusCode.getCode());
        this.message = message != null ? message : statusCode.getDescription();
        this.data = data;
    }
    
    // Success response methods
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatusCode.OK, null, data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(HttpStatusCode.OK, message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(HttpStatusCode.OK, message, null);
    }
    
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(HttpStatusCode.CREATED, null, data);
    }
    
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatusCode.CREATED, message, data);
    }
    
    public static <T> ApiResponse<T> accepted(String message) {
        return new ApiResponse<>(HttpStatusCode.ACCEPTED, message, null);
    }
    
    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(HttpStatusCode.NO_CONTENT, null, null);
    }
    
    // Client error response methods
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(HttpStatusCode.BAD_REQUEST, message, null);
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(HttpStatusCode.UNAUTHORIZED, message, null);
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(HttpStatusCode.FORBIDDEN, message, null);
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(HttpStatusCode.NOT_FOUND, message, null);
    }
    
    public static <T> ApiResponse<T> methodNotAllowed(String message) {
        return new ApiResponse<>(HttpStatusCode.METHOD_NOT_ALLOWED, message, null);
    }
    
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(HttpStatusCode.CONFLICT, message, null);
    }
    
    public static <T> ApiResponse<T> unprocessableEntity(String message) {
        return new ApiResponse<>(HttpStatusCode.UNPROCESSABLE_ENTITY, message, null);
    }
    
    public static <T> ApiResponse<T> tooManyRequests(String message) {
        return new ApiResponse<>(HttpStatusCode.TOO_MANY_REQUESTS, message, null);
    }
    
    // Server error response methods
    public static <T> ApiResponse<T> internalServerError(String message) {
        return new ApiResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR, message, null);
    }
    
    public static <T> ApiResponse<T> serviceUnavailable(String message) {
        return new ApiResponse<>(HttpStatusCode.SERVICE_UNAVAILABLE, message, null);
    }
    
    public static <T> ApiResponse<T> gatewayTimeout(String message) {
        return new ApiResponse<>(HttpStatusCode.GATEWAY_TIMEOUT, message, null);
    }
    
    // Generic error methods
    public static <T> ApiResponse<T> error(String message) {
        return internalServerError(message);
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
    
    public static <T> ApiResponse<T> error(HttpStatusCode statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }
    
    public static <T> ApiResponse<T> error(HttpStatusCode statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }
} 