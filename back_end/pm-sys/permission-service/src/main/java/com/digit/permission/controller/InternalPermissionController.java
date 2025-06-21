package com.digit.permission.controller;

import com.digit.permission.dto.ApiResponse;
import com.digit.permission.dto.UserRoleResponse;
import com.digit.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 内部权限控制器
 * 
 * <p>提供内部RPC接口，供其他服务调用。这些接口不对外公开，
 * 仅用于微服务间的通信。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@RestController
@RequestMapping("/permission/internal")
@RequiredArgsConstructor
public class InternalPermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 为用户绑定默认角色
     * 
     * <p>该接口由user-service在用户注册时调用，
     * 作为Seata分布式事务的一部分。</p>
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/roles/bind-default")
    public ResponseEntity<ApiResponse<Void>> bindDefaultRole(@RequestParam("userId") Long userId) {
        log.info("收到绑定默认角色请求，用户ID: {}", userId);
        
        try {
            permissionService.bindDefaultRole(userId);
            return ResponseEntity.ok(ApiResponse.success("默认角色绑定成功"));
            
        } catch (Exception e) {
            log.error("绑定默认角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "绑定默认角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * 查询用户角色
     * 
     * <p>该接口用于权限验证，返回用户的角色信息。</p>
     * 
     * @param userId 用户ID
     * @return 用户角色信息
     */
    @GetMapping("/user/{userId}/role")
    public ResponseEntity<ApiResponse<UserRoleResponse>> getUserRole(@PathVariable("userId") Long userId) {
        log.debug("收到查询用户角色请求，用户ID: {}", userId);
        
        try {
            UserRoleResponse userRole = permissionService.getUserRole(userId);
            
            if (userRole == null) {
                log.warn("用户未分配角色，用户ID: {}", userId);
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "用户未分配角色"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", userRole));
            
        } catch (Exception e) {
            log.error("查询用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "查询用户角色失败"));
        }
    }
    
    /**
     * 为超级管理员绑定特殊角色
     * 
     * <p>该接口专门用于超级管理员账户初始化，只能为用户名为'super_admin'的用户绑定超级管理员角色。
     * 该接口由user-service在系统启动时调用。</p>
     * 
     * @param userId 用户ID
     * @param username 用户名，必须为'super_admin'
     * @return 操作结果
     */
    @PostMapping("/roles/bind-super-admin")
    public ResponseEntity<ApiResponse<Void>> bindSuperAdminRole(
            @RequestParam("userId") Long userId, 
            @RequestParam("username") String username) {
        log.info("收到绑定超级管理员角色请求，用户ID: {}, 用户名: {}", userId, username);
        
        try {
            permissionService.bindSuperAdminRole(userId, username);
            return ResponseEntity.ok(ApiResponse.success("超级管理员角色绑定成功"));
            
        } catch (Exception e) {
            log.error("绑定超级管理员角色失败，用户ID: {}, 用户名: {}, 错误: {}", userId, username, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "绑定超级管理员角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据角色代码查询用户ID列表
     * 
     * <p>该接口用于权限过滤，查询具有指定角色的所有用户ID。
     * 主要用于分页查询时过滤掉不应该显示的用户。</p>
     * 
     * @param roleCodes 角色代码列表，逗号分隔
     * @return 用户ID列表
     */
    @GetMapping("/users/by-roles")
    public ResponseEntity<ApiResponse<List<Long>>> getUserIdsByRoleCodes(@RequestParam("roleCodes") String roleCodes) {
        log.debug("收到根据角色代码查询用户ID列表请求，角色代码: {}", roleCodes);
        
        try {
            List<String> roleCodeList = Arrays.asList(roleCodes.split(","));
            List<Long> userIds = permissionService.getUserIdsByRoleCodes(roleCodeList);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", userIds));
            
        } catch (Exception e) {
            log.error("根据角色代码查询用户ID列表失败，角色代码: {}, 错误: {}", roleCodes, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "查询用户ID列表失败"));
        }
    }
} 