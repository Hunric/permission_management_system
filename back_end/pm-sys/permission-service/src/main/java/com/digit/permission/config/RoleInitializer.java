package com.digit.permission.config;

import com.digit.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 角色数据初始化器
 * 
 * <p>在应用启动时自动初始化系统基础角色数据。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {
    
    private final PermissionService permissionService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统角色数据");
        
        try {
            permissionService.initializeRoles();
            log.info("系统角色数据初始化完成");
            
        } catch (Exception e) {
            log.error("系统角色数据初始化失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响应用启动
        }
    }
} 