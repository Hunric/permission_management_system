package com.digit.permission.controller;

import com.digit.permission.dto.ApiResponse;
import com.digit.permission.dto.RoleChangeResponse;
import com.digit.permission.dto.UserRoleResponse;
import com.digit.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理控制器
 * 
 * <p>提供用户角色管理的外部API接口，包括角色升级和降级功能。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 升级用户角色为管理员
     * 
     * <p>只有超级管理员可以执行此操作。</p>
     * 
     * @param userId 要升级的用户ID
     * @return 操作结果
     */
    @PutMapping("/user/{userId}/upgrade-to-admin")
    public ResponseEntity<ApiResponse<RoleChangeResponse>> upgradeUserToAdmin(@PathVariable("userId") Long userId) {
        log.info("收到升级用户角色请求，用户ID: {}", userId);
        
        try {
            // 获取当前操作者信息
            Long operatorId = getCurrentUserId();
            
            // 检查权限：只有超级管理员可以执行此操作
            if (!isSuperAdmin(operatorId)) {
                log.warn("权限不足，用户ID: {} 尝试升级用户角色", operatorId);
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 执行升级操作
            permissionService.upgradeUserToAdmin(userId, operatorId);
            
            // 构造响应数据
            RoleChangeResponse response = RoleChangeResponse.builder()
                    .userId(userId)
                    .newRole("admin")
                    .newRoleName("管理员")
                    .build();
            
            log.info("用户角色升级成功，用户ID: {}, 操作者ID: {}", userId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("用户角色已升级为管理员", response));
            
        } catch (RuntimeException e) {
            log.error("升级用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            
            if (e.getMessage().contains("用户未找到")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "用户未找到"));
            } else if (e.getMessage().contains("无法升级")) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error(400, e.getMessage()));
            } else {
                return ResponseEntity.status(500)
                        .body(ApiResponse.error(500, "升级用户角色失败"));
            }
        } catch (Exception e) {
            log.error("升级用户角色时发生未知错误，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "系统内部错误"));
        }
    }
    
    /**
     * 降级用户角色为普通用户
     * 
     * <p>只有超级管理员可以执行此操作。</p>
     * 
     * @param userId 要降级的用户ID
     * @return 操作结果
     */
    @PutMapping("/user/{userId}/downgrade-to-user")
    public ResponseEntity<ApiResponse<RoleChangeResponse>> downgradeUserToUser(@PathVariable("userId") Long userId) {
        log.info("收到降级用户角色请求，用户ID: {}", userId);
        
        try {
            // 获取当前操作者信息
            Long operatorId = getCurrentUserId();
            
            // 检查权限：只有超级管理员可以执行此操作
            if (!isSuperAdmin(operatorId)) {
                log.warn("权限不足，用户ID: {} 尝试降级用户角色", operatorId);
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "权限不足"));
            }
            
            // 执行降级操作
            permissionService.downgradeUserToUser(userId, operatorId);
            
            // 构造响应数据
            RoleChangeResponse response = RoleChangeResponse.builder()
                    .userId(userId)
                    .newRole("user")
                    .newRoleName("普通用户")
                    .build();
            
            log.info("用户角色降级成功，用户ID: {}, 操作者ID: {}", userId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("用户角色已降级为普通用户", response));
            
        } catch (RuntimeException e) {
            log.error("降级用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            
            if (e.getMessage().contains("用户未找到")) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "用户未找到"));
            } else if (e.getMessage().contains("无法降级")) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error(400, e.getMessage()));
            } else {
                return ResponseEntity.status(500)
                        .body(ApiResponse.error(500, "降级用户角色失败"));
            }
        } catch (Exception e) {
            log.error("降级用户角色时发生未知错误，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "系统内部错误"));
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new RuntimeException("无法获取当前用户信息");
    }
    
    /**
     * 检查当前用户是否为超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        try {
            UserRoleResponse userRole = permissionService.getUserRole(userId);
            return userRole != null && "super_admin".equals(userRole.getRoleCode());
        } catch (Exception e) {
            log.error("检查用户权限失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }
} 