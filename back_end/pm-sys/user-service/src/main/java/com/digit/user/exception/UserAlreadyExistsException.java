package com.digit.user.exception;

/**
 * 用户已存在异常
 * 
 * <p>当注册用户时，如果用户名已被占用则抛出此异常。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 根据用户名创建异常
     * 
     * @param username 已存在的用户名
     * @return 用户已存在异常实例
     */
    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException("用户名已存在: " + username);
    }
} 