package com.digit.permission.service;

import com.digit.permission.dto.UserRoleResponse;
import java.util.List;

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
    
    /**
     * 升级用户角色为管理员
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @throws RuntimeException 当升级失败时抛出异常
     */
    void upgradeUserToAdmin(Long userId, Long operatorId);
    
    /**
     * 降级用户角色为普通用户
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @throws RuntimeException 当降级失败时抛出异常
     */
    void downgradeUserToUser(Long userId, Long operatorId);
    
    /**
     * 为超级管理员绑定特殊角色
     * 
     * <p>专门用于超级管理员账户初始化，直接绑定超级管理员角色。
     * 该方法会检查用户名是否为'super_admin'，只有超级管理员账户才能使用此方法。</p>
     * 
     * @param userId 用户ID
     * @param username 用户名，必须为'super_admin'
     * @throws RuntimeException 当绑定失败或用户名不是'super_admin'时抛出异常
     */
    void bindSuperAdminRole(Long userId, String username);
    
    /**
     * 根据角色代码查询用户ID列表
     * 
     * <p>用于权限过滤，查询具有指定角色的所有用户ID。
     * 主要用于分页查询时过滤掉不应该显示的用户。</p>
     * 
     * @param roleCodes 角色代码列表
     * @return 具有指定角色的用户ID列表
     */
    List<Long> getUserIdsByRoleCodes(List<String> roleCodes);
} 