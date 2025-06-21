package com.digit.user.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.sql.Timestamp;

/**
 * 用户信息响应数据对象
 * 
 * <p>封装用户的基本信息，用于返回给客户端。
 * 不包含敏感信息如密码等。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoVO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 手机号码
     */
    private String phone;
    
    /**
     * 创建时间
     */
    private Timestamp gmtCreate;
    
    /**
     * 最后修改时间
     */
    private Timestamp gmtModified;
} 