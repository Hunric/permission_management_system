package com.digit.user.exception;

/**
 * 用户不存在异常
 * 
 * <p>当根据用户ID或用户名查找用户时，如果用户不存在则抛出此异常。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UserNotFoundException(Long userId) {
        super("用户不存在，用户ID: " + userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super("用户不存在，" + field + ": " + value);
    }
} 