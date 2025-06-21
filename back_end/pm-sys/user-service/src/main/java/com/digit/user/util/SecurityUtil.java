package com.digit.user.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 
 * <p>提供与Spring Security相关的工具方法，用于获取当前登录用户信息。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Slf4j
public class SecurityUtil {
    
    /**
     * 获取当前登录用户的用户ID
     * 
     * @return 当前用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof Long) {
                    return (Long) principal;
                }
                
                log.warn("Principal类型不匹配，期望Long类型，实际类型: {}", principal.getClass().getSimpleName());
            }
            
            return null;
        } catch (Exception e) {
            log.warn("获取当前用户ID时发生异常: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查当前用户是否已认证
     * 
     * @return 如果用户已认证返回true，否则返回false
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   authentication.getPrincipal() != null;
        } catch (Exception e) {
            log.warn("检查用户认证状态时发生异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取当前认证对象
     * 
     * @return 当前认证对象，如果未认证则返回null
     */
    public static Authentication getCurrentAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            log.warn("获取当前认证对象时发生异常: {}", e.getMessage());
            return null;
        }
    }
} 