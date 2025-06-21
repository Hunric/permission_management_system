package com.digit.user.exception;

/**
 * 业务异常基类
 * 
 * <p>所有业务相关异常的基类，用于统一处理业务逻辑错误。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 