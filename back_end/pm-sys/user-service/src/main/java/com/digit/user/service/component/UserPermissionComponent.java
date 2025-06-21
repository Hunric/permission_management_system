package com.digit.user.service.component;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.UserRoleResponse;
import com.digit.user.rcp.PermissionFeignClient;
import com.digit.user.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用户权限组件
 * 
 * <p>负责处理用户权限验证相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>管理员权限验证</li>
 *   <li>用户角色查询</li>
 *   <li>权限级别检查</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPermissionComponent {
    
    private final PermissionFeignClient permissionFeignClient;
    
    // 管理员角色列表
    private static final List<String> ADMIN_ROLES = Arrays.asList("admin", "super_admin");
    
    /**
     * 验证管理员权限
     * 
     * <p>检查当前用户是否具有管理员或超级管理员权限</p>
     * 
     * @throws SecurityException 如果用户没有管理员权限
     */
    public void validateAdminPermission() {
        log.debug("验证管理员权限");
        
        // 获取当前用户ID
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("权限验证失败：用户未认证");
            throw new SecurityException("用户未认证");
        }
        
        log.debug("验证用户权限，用户ID: {}", currentUserId);
        
        try {
            // 调用权限服务查询用户角色
            ApiResponse<UserRoleResponse> response = permissionFeignClient.getUserRole(currentUserId);
            
            if (response == null || response.getData() == null) {
                log.warn("权限验证失败：用户未分配角色，用户ID: {}", currentUserId);
                throw new SecurityException("用户未分配角色");
            }
            
            UserRoleResponse userRole = response.getData();
            
            // 检查是否为管理员角色
            if (!ADMIN_ROLES.contains(userRole.getRoleCode())) {
                log.warn("权限验证失败：用户权限不足，用户ID: {}, 角色: {}", currentUserId, userRole.getRoleCode());
                throw new SecurityException("权限不足，需要管理员权限");
            }
            
            log.debug("权限验证通过，用户ID: {}, 角色: {}", currentUserId, userRole.getRoleCode());
            
        } catch (SecurityException e) {
            // 重新抛出安全异常
            throw e;
        } catch (Exception e) {
            log.error("权限验证失败，用户ID: {}, 错误: {}", currentUserId, e.getMessage());
            throw new SecurityException("权限验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户角色信息
     * 
     * @param userId 用户ID
     * @return 用户角色信息
     */
    public UserRoleResponse getUserRole(Long userId) {
        log.debug("查询用户角色，用户ID: {}", userId);
        
        try {
            ApiResponse<UserRoleResponse> response = permissionFeignClient.getUserRole(userId);
            
            if (response == null || response.getData() == null) {
                log.warn("用户未分配角色，用户ID: {}", userId);
                return null;
            }
            
            UserRoleResponse userRole = response.getData();
            log.debug("查询到用户角色，用户ID: {}, 角色: {}", userId, userRole.getRoleCode());
            return userRole;
            
        } catch (Exception e) {
            log.error("查询用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            throw new RuntimeException("查询用户角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查用户是否为管理员
     * 
     * @param userId 用户ID
     * @return 如果是管理员返回true，否则返回false
     */
    public boolean isAdmin(Long userId) {
        try {
            UserRoleResponse userRole = getUserRole(userId);
            return userRole != null && ADMIN_ROLES.contains(userRole.getRoleCode());
        } catch (Exception e) {
            log.warn("检查管理员权限时发生异常，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查用户是否为超级管理员
     * 
     * @param userId 用户ID
     * @return 如果是超级管理员返回true，否则返回false
     */
    public boolean isSuperAdmin(Long userId) {
        try {
            UserRoleResponse userRole = getUserRole(userId);
            return userRole != null && "super_admin".equals(userRole.getRoleCode());
        } catch (Exception e) {
            log.warn("检查超级管理员权限时发生异常，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取需要从用户列表中排除的用户ID列表
     * 
     * <p>根据当前用户的角色权限，确定哪些用户不应该在分页查询结果中显示：</p>
     * <ul>
     *   <li>管理员：只能查看普通用户，需要排除admin和super_admin角色的用户</li>
     *   <li>超级管理员：可以查看所有其他用户，只需要排除自己</li>
     * </ul>
     * 
     * @return 需要排除的用户ID列表
     */
    public List<Long> getExcludedUserIds() {
        log.debug("获取需要排除的用户ID列表");
        
        // 获取当前用户ID
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("获取排除用户列表失败：用户未认证");
            throw new SecurityException("用户未认证");
        }
        
        try {
            // 查询当前用户角色
            UserRoleResponse currentUserRole = getUserRole(currentUserId);
            if (currentUserRole == null) {
                log.warn("获取排除用户列表失败：用户未分配角色，用户ID: {}", currentUserId);
                throw new SecurityException("用户未分配角色");
            }
            
            List<Long> excludedUserIds = new ArrayList<>();
            
            if ("super_admin".equals(currentUserRole.getRoleCode())) {
                // 超级管理员：可以查看所有其他用户，只排除自己
                excludedUserIds.add(currentUserId);
                log.debug("超级管理员权限：排除自己，用户ID: {}", currentUserId);
                
            } else if ("admin".equals(currentUserRole.getRoleCode())) {
                // 管理员：只能查看普通用户，排除所有admin和super_admin角色的用户
                excludedUserIds.add(currentUserId); // 排除自己
                
                // 查询所有admin和super_admin角色的用户
                try {
                    ApiResponse<List<Long>> response = permissionFeignClient.getUserIdsByRoleCodes("admin,super_admin");
                    if (response != null && response.getData() != null) {
                        List<Long> adminAndSuperAdminIds = response.getData();
                        for (Long userId : adminAndSuperAdminIds) {
                            if (!excludedUserIds.contains(userId)) {
                                excludedUserIds.add(userId);
                            }
                        }
                    } else {
                        log.warn("权限服务返回空数据，无法获取admin和super_admin用户列表");
                    }
                } catch (Exception e) {
                    log.warn("查询admin和super_admin用户列表失败: {}, 仅排除当前用户", e.getMessage());
                    // 发生异常时，仅排除当前用户，确保不会完全失败
                }
                
                log.debug("管理员权限：排除admin和super_admin角色用户，总计排除 {} 个用户", excludedUserIds.size());
                
            } else {
                // 普通用户：没有查看用户列表的权限
                log.warn("权限验证失败：普通用户没有查看用户列表的权限，用户ID: {}, 角色: {}", 
                        currentUserId, currentUserRole.getRoleCode());
                throw new SecurityException("权限不足，需要管理员权限");
            }
            
            return excludedUserIds;
            
        } catch (SecurityException e) {
            // 重新抛出安全异常
            throw e;
        } catch (Exception e) {
            log.error("获取排除用户列表失败，用户ID: {}, 错误: {}", currentUserId, e.getMessage(), e);
            throw new SecurityException("获取权限信息失败: " + e.getMessage());
        }
    }
} 