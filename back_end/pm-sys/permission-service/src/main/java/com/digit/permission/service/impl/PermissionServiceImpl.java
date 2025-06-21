package com.digit.permission.service.impl;

import com.digit.permission.dto.UserRoleResponse;
import com.digit.permission.entity.Role;
import com.digit.permission.entity.UserRole;
import com.digit.permission.repository.RoleRepository;
import com.digit.permission.repository.UserRoleRepository;
import com.digit.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 权限服务实现类
 * 
 * <p>实现权限管理的核心业务逻辑，包括角色绑定、查询等功能。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
    public class PermissionServiceImpl implements PermissionService {
    
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    /**
     * 为用户绑定默认角色
     * 
     * <p>该方法是用户注册流程的重要组成部分，参与Seata分布式事务。
     * 如果该操作失败，整个用户注册事务将回滚。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindDefaultRole(Long userId) {
        log.info("开始为用户绑定默认角色，用户ID: {}", userId);
        
        try {
            // 检查用户是否已经分配角色
            if (userRoleRepository.existsByUserId(userId)) {
                log.warn("用户已存在角色绑定，用户ID: {}", userId);
                throw new RuntimeException("用户已存在角色绑定");
            }
            
            // 查找默认角色（普通用户）
            Optional<Role> defaultRoleOpt = roleRepository.findByRoleCode("user");
            if (!defaultRoleOpt.isPresent()) {
                log.error("系统错误：默认角色'user'不存在");
                throw new RuntimeException("系统错误：默认角色不存在");
            }
            
            Role defaultRole = defaultRoleOpt.get();
            log.debug("找到默认角色: {} (ID: {})", defaultRole.getRoleName(), defaultRole.getRoleId());
            
            // 创建用户角色关系
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(defaultRole.getRoleId());
            userRole.setGmtCreate(Timestamp.valueOf(LocalDateTime.now()));
            
            // 保存到数据库
            userRoleRepository.save(userRole);
            log.info("成功为用户绑定默认角色，用户ID: {}, 角色: {}", userId, defaultRole.getRoleName());
            
        } catch (Exception e) {
            // 当前的异常处理机制不够精确，需要优化
            log.error("为用户绑定默认角色失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("绑定默认角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查询用户角色信息
     */
    @Override
    public UserRoleResponse getUserRole(Long userId) {
        log.debug("查询用户角色信息，用户ID: {}", userId);
        
        try {
            Optional<Object[]> roleInfoOpt = userRoleRepository.findUserRoleInfo(userId);
            
            // 按理来说，必须要保证每个用户都有角色，如果用户没有角色，则需要抛出异常
            if (!roleInfoOpt.isPresent()) {
                log.warn("用户未分配角色，用户ID: {}", userId);
                return null;
            }
            
            Object[] roleInfo = roleInfoOpt.get();
            String roleCode = (String) roleInfo[0];
            String roleName = (String) roleInfo[1];
            
            UserRoleResponse response = UserRoleResponse.builder()
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .build();
            
            log.debug("查询到用户角色信息，用户ID: {}, 角色: {} ({})", userId, roleName, roleCode);
            return response;
            
        } catch (Exception e) {
            // 按理来说，必须要保证每个用户都有角色，如果用户没有角色，则需要抛出异常
            log.error("查询用户角色信息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 初始化角色数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeRoles() {
        log.info("开始初始化角色数据");
        
        try {
            // 检查并创建基础角色
            // 基础角色在数据库初始化时，已经创建了，所以这里不需要创建
            createRoleIfNotExists("super_admin", "超级管理员");
            createRoleIfNotExists("admin", "管理员");
            createRoleIfNotExists("user", "普通用户");
            
            log.info("角色数据初始化完成");
            
        } catch (Exception e) {
            log.error("初始化角色数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化角色数据失败", e);
        }
    }
    
    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String roleCode, String roleName) {
        if (!roleRepository.existsByRoleCode(roleCode)) {
            Role role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setGmtCreate(Timestamp.valueOf(LocalDateTime.now()));
            
            roleRepository.save(role);
            log.info("创建角色: {} ({})", roleName, roleCode);
        } else {
            log.debug("角色已存在: {} ({})", roleName, roleCode);
        }
    }
} 