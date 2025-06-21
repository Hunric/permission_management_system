package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录请求数据传输对象
 * 
 * <p>封装用户登录时客户端提交的认证信息。该DTO承载用户的登录凭证，
 * 用于身份验证和JWT令牌生成。</p>
 * 
 * <p><strong>安全特性：</strong></p>
 * <ul>
 *   <li>密码字段不会序列化到日志中</li>
 *   <li>支持参数校验防止恶意输入</li>
 *   <li>传输后及时清理敏感信息</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {
    
    /**
     * 用户名
     * 
     * <p>用户的登录标识符，必须是已注册的有效用户名。</p>
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    /**
     * 登录密码
     * 
     * <p>用户的认证凭据，将与数据库中存储的加密密码进行比对。</p>
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    /**
     * 重写toString方法，避免密码泄露到日志中
     */
    @Override
    public String toString() {
        return "UserLoginDTO{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
} 