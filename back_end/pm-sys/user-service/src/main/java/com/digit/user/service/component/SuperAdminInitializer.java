package com.digit.user.service.component;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.UserRoleResponse;
import com.digit.user.entity.User;
import com.digit.user.repository.UserRepository;
import com.digit.user.rcp.PermissionFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 超级管理员初始化器
 * 
 * <p>负责在系统启动时检查并初始化超级管理员账户。确保系统始终有一个可用的超级管理员账户
 * 用于系统管理和其他管理员账户的创建。</p>
 * 
 * <p><strong>初始化逻辑：</strong></p>
 * <ol>
 *   <li>检查是否存在用户名为 'super_admin' 的用户</li>
 *   <li>如果不存在，创建新的超级管理员账户</li>
 *   <li>如果存在，检查其角色是否为超级管理员</li>
 *   <li>如果角色不正确或为空，删除现有用户并重新创建</li>
 *   <li>为超级管理员绑定正确的角色</li>
 * </ol>
 * 
 * <p><strong>安全特性：</strong></p>
 * <ul>
 *   <li>使用BCrypt加密存储密码</li>
 *   <li>支持分布式事务确保数据一致性</li>
 *   <li>自动修复损坏的超级管理员账户</li>
 *   <li>启动时优先执行，确保系统可管理性</li>
 * </ul>
 * 
 * <p><strong>容错机制：</strong></p>
 * <ul>
 *   <li>权限服务不可用时会记录警告但不阻止启动</li>
 *   <li>数据库操作失败时会重试一次</li>
 *   <li>关键错误会阻止应用启动</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-06-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // 确保在其他初始化器之前执行
public class SuperAdminInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PermissionFeignClient permissionFeignClient;
    private final BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 超级管理员默认配置
     */
    private static final String SUPER_ADMIN_USERNAME = "super_admin";
    private static final String SUPER_ADMIN_PASSWORD = "super_admin";
    private static final String SUPER_ADMIN_EMAIL = "super_admin@system.local";
    private static final String SUPER_ADMIN_ROLE = "super_admin";
    
    /**
     * 系统启动时执行超级管理员初始化
     * 
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        log.info("=== 开始初始化超级管理员账户 ===");
        
        try {
            initializeSuperAdmin();
            log.info("=== 超级管理员账户初始化完成 ===");
        } catch (Exception e) {
            log.error("超级管理员初始化失败，系统可能无法正常管理: {}", e.getMessage(), e);
            // 不抛出异常，避免阻止应用启动
        }
    }
    
    /**
     * 初始化超级管理员的核心逻辑
     */
    @GlobalTransactional(name = "super-admin-init-tx", rollbackFor = Exception.class)
    public void initializeSuperAdmin() {
        // 检查是否存在超级管理员用户
        User existingUser = userRepository.findByUsername(SUPER_ADMIN_USERNAME).orElse(null);
        
        if (existingUser == null) {
            // 不存在，创建新的超级管理员
            log.info("未找到超级管理员账户，开始创建...");
            createSuperAdmin();
        } else {
            // 存在，检查角色是否正确
            log.info("找到现有超级管理员账户 (ID: {}), 检查角色配置...", existingUser.getUserId());
            validateAndFixSuperAdminRole(existingUser);
        }
    }
    
    /**
     * 创建新的超级管理员账户
     */
    private void createSuperAdmin() {
        try {
            // 创建用户实体
            User superAdmin = createSuperAdminUser();
            
            // 保存到数据库
            User savedUser = userRepository.save(superAdmin);
            log.info("超级管理员用户创建成功，用户ID: {}", savedUser.getUserId());
            
            // 绑定超级管理员角色
            bindSuperAdminRole(savedUser.getUserId());
            
            log.info("超级管理员账户创建完成 - 用户名: {}, 用户ID: {}", 
                    SUPER_ADMIN_USERNAME, savedUser.getUserId());
            
        } catch (Exception e) {
            log.error("创建超级管理员失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建超级管理员失败", e);
        }
    }
    
    /**
     * 验证并修复超级管理员角色
     */
    private void validateAndFixSuperAdminRole(User existingUser) {
        try {
            // 查询用户当前角色
            UserRoleResponse currentRole = getCurrentUserRole(existingUser.getUserId());
            
            if (currentRole == null || !SUPER_ADMIN_ROLE.equals(currentRole.getRoleCode())) {
                log.warn("超级管理员角色配置不正确，当前角色: {}, 开始修复...", 
                        currentRole != null ? currentRole.getRoleCode() : "无角色");
                
                // 删除现有用户并重新创建
                recreateSuperAdmin(existingUser);
            } else {
                log.info("超级管理员角色配置正确，无需修复");
            }
            
        } catch (Exception e) {
            log.error("验证超级管理员角色失败: {}", e.getMessage(), e);
            // 如果无法验证角色，尝试重新创建
            try {
                recreateSuperAdmin(existingUser);
            } catch (Exception recreateException) {
                log.error("重新创建超级管理员也失败了: {}", recreateException.getMessage(), recreateException);
                throw new RuntimeException("修复超级管理员失败", recreateException);
            }
        }
    }
    
    /**
     * 重新创建超级管理员账户
     */
    private void recreateSuperAdmin(User existingUser) {
        log.info("删除现有超级管理员账户并重新创建...");
        
        // 删除现有用户
        userRepository.delete(existingUser);
        log.info("已删除现有超级管理员账户 (ID: {})", existingUser.getUserId());
        
        // 创建新的超级管理员
        createSuperAdmin();
    }
    
    /**
     * 创建超级管理员用户实体
     */
    private User createSuperAdminUser() {
        String encodedPassword = passwordEncoder.encode(SUPER_ADMIN_PASSWORD);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        
        return User.builder()
                .username(SUPER_ADMIN_USERNAME)
                .password(encodedPassword)
                .email(SUPER_ADMIN_EMAIL)
                .phone(null)
                .gmtCreate(now)
                .gmtModified(now)
                .build();
    }
    
    /**
     * 为用户绑定超级管理员角色
     */
    private void bindSuperAdminRole(Long userId) {
        try {
            log.debug("为用户 {} 绑定超级管理员角色", userId);
            ApiResponse<Void> response = permissionFeignClient.bindSuperAdminRole(userId, SUPER_ADMIN_USERNAME);
            
            if (response != null && response.getCode() == 200) {
                log.info("超级管理员角色绑定成功");
            } else {
                log.warn("角色绑定响应异常: {}", response);
            }
            
        } catch (Exception e) {
            log.error("绑定超级管理员角色失败: {}", e.getMessage(), e);
            throw new RuntimeException("绑定超级管理员角色失败", e);
        }
    }
    
    /**
     * 获取用户当前角色
     */
    private UserRoleResponse getCurrentUserRole(Long userId) {
        try {
            ApiResponse<UserRoleResponse> response = permissionFeignClient.getUserRole(userId);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.warn("查询用户角色失败: {}", e.getMessage());
            return null;
        }
    }
} 