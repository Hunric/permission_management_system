package com.digit.user.exception;

/**
 * 身份验证异常
 * 
 * <p>当用户身份验证失败时抛出此异常，包括密码错误、账户被锁定等情况。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
public class AuthenticationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
} 