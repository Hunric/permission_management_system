package com.digit.permission.service;

import com.digit.permission.dto.UserRoleResponse;

/**
 * 权限服务接口
 * 
 * <p>定义权限管理相关的业务方法，包括角色绑定、查询等功能。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
public interface PermissionService {
    
    /**
     * 为用户绑定默认角色
     * 
     * <p>在用户注册时调用，为新用户分配默认的"普通用户"角色。
     * 该方法参与Seata分布式事务，确保与用户创建操作的原子性。</p>
     * 
     * @param userId 用户ID
     * @throws RuntimeException 当绑定失败时抛出异常
     */
    void bindDefaultRole(Long userId);
    
    /**
     * 查询用户角色信息
     * 
     * @param userId 用户ID
     * @return 用户角色信息，如果用户不存在或未分配角色则返回null
     */
    UserRoleResponse getUserRole(Long userId);
    
    /**
     * 初始化角色数据
     * 
     * <p>系统启动时调用，确保基础角色数据存在。</p>
     */
    void initializeRoles();
} 