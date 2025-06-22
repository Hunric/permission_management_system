package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 重置密码响应数据传输对象
 * 
 * <p>封装管理员重置用户密码后返回的响应数据。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 新的临时密码
     * 
     * <p>系统生成的临时密码，用户可以使用此密码登录并修改为自己的密码。</p>
     */
    private String newPassword;
    
    /**
     * 提示信息
     */
    private String message;
} 