package com.digit.permission.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 角色变更响应对象
 * 
 * <p>用于封装角色升级/降级操作的响应数据。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleChangeResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 新角色代码
     */
    private String newRole;
    
    /**
     * 新角色名称
     */
    private String newRoleName;
} 