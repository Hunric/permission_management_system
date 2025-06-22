package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户修改密码请求数据传输对象
 * 
 * <p>封装用户自己修改密码时客户端提交的数据。包含旧密码验证和新密码设置。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDTO {
    
    /**
     * 旧密码
     * 
     * <p>用户当前的密码，用于身份验证。必须与数据库中存储的密码匹配。</p>
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    /**
     * 新密码
     * 
     * <p>用户希望设置的新密码。必须符合系统的密码强度要求。</p>
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6到20个字符之间")
    private String newPassword;
} 