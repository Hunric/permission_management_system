package com.digit.user.exception;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.HttpStatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle HTTP message not readable exception
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Request body parsing failed: {}", e.getMessage());
        
        String errorMessage = "Request body format error";
        
        Throwable cause = e.getCause();
        if (cause instanceof JsonProcessingException) {
            JsonProcessingException jsonException = (JsonProcessingException) cause;
            log.warn("JSON parsing error details: {}", jsonException.getOriginalMessage());
            errorMessage = "JSON format error: " + jsonException.getOriginalMessage();
        } else {
            log.warn("Request body parsing error details: {}", e.getLocalizedMessage());
            errorMessage = "Request body format error, unable to parse";
        }
        
        ApiResponse<String> response = ApiResponse.badRequest(errorMessage);
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST.getCode()).body(response);
    }

    /**
     * Handle method argument validation exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Parameter validation failed: {}", e.getMessage());
        
        Object target = e.getBindingResult().getTarget();
        if (target != null) {
            log.warn("Validation failed for request body: {}", target);
        }
        
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        log.warn("Detailed validation error information: {}", errorMessage);
        
        ApiResponse<String> response = ApiResponse.badRequest("Parameter validation failed: " + errorMessage);
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST.getCode()).body(response);
    }

    /**
     * Handle constraint violation exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Parameter constraint validation failed: {}", e.getMessage());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        
        log.warn("Detailed constraint violation information: {}", errorMessage);
        
        ApiResponse<String> response = ApiResponse.badRequest("Parameter validation failed: " + errorMessage);
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST.getCode()).body(response);
    }

    /**
     * Handle user not found exception
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.notFound(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.NOT_FOUND.getCode()).body(response);
    }
    
    /**
     * Handle authentication exception
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.unauthorized(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED.getCode()).body(response);
    }
    
    /**
     * Handle user already exists exception
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.warn("User already exists: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.conflict(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.CONFLICT.getCode()).body(response);
    }
    
    /**
     * Handle business exception
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.badRequest(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST.getCode()).body(response);
    }
    
    /**
     * Handle security exception (permission denied)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<String>> handleSecurityException(SecurityException e) {
        log.warn("Security exception: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.forbidden(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.FORBIDDEN.getCode()).body(response);
    }
    
    /**
     * Handle illegal argument exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        
        ApiResponse<String> response = ApiResponse.badRequest(e.getMessage());
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST.getCode()).body(response);
    }
    
    /**
     * Handle runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        
        ApiResponse<String> response = ApiResponse.internalServerError("系统异常，请稍后重试");
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()).body(response);
    }
    
    /**
     * Handle generic exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        
        ApiResponse<String> response = ApiResponse.internalServerError("Internal server error");
        return ResponseEntity.status(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()).body(response);
    }
}
