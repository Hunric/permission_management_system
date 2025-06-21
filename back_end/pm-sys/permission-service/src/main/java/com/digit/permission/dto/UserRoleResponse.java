package com.digit.permission.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 用户角色响应对象
 * 
 * <p>用于封装从权限服务返回的用户角色信息。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleResponse {
    
    /**
     * 角色代码
     * 如：user, admin, super_admin
     */
    private String roleCode;
    
    /**
     * 角色名称
     * 如：普通用户, 管理员, 超级管理员
     */
    private String roleName;
} 