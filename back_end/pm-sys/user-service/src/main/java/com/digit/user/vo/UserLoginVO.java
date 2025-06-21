package com.digit.user.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 用户登录响应数据对象
 * 
 * <p>封装用户登录成功后返回给客户端的响应信息，
 * 包含JWT访问令牌和相关的元数据。</p>
 * 
 * <p><strong>响应内容：</strong></p>
 * <ul>
 *   <li>JWT访问令牌：用于后续API调用的身份验证</li>
 *   <li>令牌过期时间：客户端用于判断令牌有效性</li>
 *   <li>用户基本信息：便于前端显示</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginVO {
    
    /**
     * JWT访问令牌
     * 
     * <p>用于访问受保护资源的JSON Web Token。
     * 客户端需要在后续请求的Authorization头中携带此令牌。</p>
     */
    private String token;
    
    /**
     * 令牌过期时间（秒）
     * 
     * <p>从当前时间开始计算的令牌有效期，单位为秒。
     * 客户端可以根据此值判断是否需要刷新令牌。</p>
     */
    private Long expiresIn;
    
    /**
     * 用户ID
     * 
     * <p>登录用户的唯一标识符，便于前端进行用户相关操作。</p>
     */
    private Long userId;
    
    /**
     * 用户名
     * 
     * <p>登录成功的用户名，用于前端显示确认信息。</p>
     */
    private String username;
} 