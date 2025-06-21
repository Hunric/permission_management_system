package com.digit.permission.controller;

import com.digit.permission.dto.UserRoleResponse;
import com.digit.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> bindDefaultRole(@RequestParam("userId") Long userId) {
        log.info("收到绑定默认角色请求，用户ID: {}", userId);
        
        try {
            permissionService.bindDefaultRole(userId);
            return ResponseEntity.ok("默认角色绑定成功");
            
        } catch (Exception e) {
            log.error("绑定默认角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("绑定默认角色失败: " + e.getMessage());
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
    public ResponseEntity<UserRoleResponse> getUserRole(@PathVariable("userId") Long userId) {
        log.debug("收到查询用户角色请求，用户ID: {}", userId);
        
        try {
            UserRoleResponse userRole = permissionService.getUserRole(userId);
            
            if (userRole == null) {
                log.warn("用户未分配角色，用户ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(userRole);
            
        } catch (Exception e) {
            log.error("查询用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 