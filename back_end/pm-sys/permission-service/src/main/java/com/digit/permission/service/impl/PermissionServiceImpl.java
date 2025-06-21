package com.digit.permission.service.impl;

import com.digit.permission.dto.OperationLogMessage;
import com.digit.permission.dto.UserRoleResponse;
import com.digit.permission.entity.Role;
import com.digit.permission.entity.UserRole;
import com.digit.permission.repository.RoleRepository;
import com.digit.permission.repository.UserRoleRepository;
import com.digit.permission.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
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
    private final RocketMQTemplate rocketMQTemplate;
    
    @Value("${app.operation-log.topic:user-operation-log}")
    private String operationLogTopic;
    
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
            
            // 如果用户没有角色，返回null
            if (!roleInfoOpt.isPresent()) {
                log.warn("用户未分配角色，用户ID: {}", userId);
                return null;
            }
            
            Object[] roleInfo = roleInfoOpt.get();
            
            // 调试信息：打印查询结果
            log.debug("查询结果数组长度: {}", roleInfo.length);
            for (int i = 0; i < roleInfo.length; i++) {
                log.debug("数组元素[{}]: {} (类型: {})", i, roleInfo[i], roleInfo[i] != null ? roleInfo[i].getClass().getSimpleName() : "null");
            }
            
            // 处理JPA返回结果的特殊情况
            String roleCode = null;
            String roleName = null;
            
            if (roleInfo.length == 2) {
                // 正常情况：直接包含两个字段
                roleCode = roleInfo[0] != null ? roleInfo[0].toString() : null;
                roleName = roleInfo[1] != null ? roleInfo[1].toString() : null;
            } else if (roleInfo.length == 1 && roleInfo[0] instanceof Object[]) {
                // JPA包装情况：第一个元素是Object[]
                Object[] innerArray = (Object[]) roleInfo[0];
                if (innerArray.length >= 2) {
                    roleCode = innerArray[0] != null ? innerArray[0].toString() : null;
                    roleName = innerArray[1] != null ? innerArray[1].toString() : null;
                } else {
                    String errorMsg = String.format("内层数组长度不足，用户ID: %d, 内层数组长度: %d", userId, innerArray.length);
                    log.error(errorMsg);
                    throw new RuntimeException("用户角色数据异常: " + errorMsg);
                }
            } else {
                String errorMsg = String.format("角色信息数据格式异常，用户ID: %d, 数组长度: %d", userId, roleInfo.length);
                log.error(errorMsg);
                throw new RuntimeException("用户角色数据异常: " + errorMsg);
            }
            
            log.debug("解析后的角色信息 - roleCode: {}, roleName: {}", roleCode, roleName);
            
            if (roleCode == null || roleName == null || roleCode.trim().isEmpty() || roleName.trim().isEmpty()) {
                String errorMsg = String.format("角色信息包含空值，用户ID: %d, roleCode: %s, roleName: %s", userId, roleCode, roleName);
                log.error(errorMsg);
                throw new RuntimeException("用户角色数据异常: " + errorMsg);
            }
            
            UserRoleResponse response = UserRoleResponse.builder()
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .build();
            
            log.debug("查询到用户角色信息，用户ID: {}, 角色: {} ({})", userId, roleName, roleCode);
            return response;
            
        } catch (Exception e) {
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
     * 升级用户角色为管理员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upgradeUserToAdmin(Long userId, Long operatorId) {
        log.info("开始升级用户角色为管理员，用户ID: {}, 操作者ID: {}", userId, operatorId);
        
        try {
            // 查找用户当前角色
            Optional<UserRole> userRoleOpt = userRoleRepository.findByUserId(userId);
            if (!userRoleOpt.isPresent()) {
                log.error("用户不存在或未分配角色，用户ID: {}", userId);
                throw new RuntimeException("用户未找到");
            }
            
            UserRole userRole = userRoleOpt.get();
            
            // 查找当前角色信息
            Optional<Role> currentRoleOpt = roleRepository.findById(userRole.getRoleId());
            if (!currentRoleOpt.isPresent()) {
                log.error("用户当前角色不存在，角色ID: {}", userRole.getRoleId());
                throw new RuntimeException("用户角色数据异常");
            }
            
            Role currentRole = currentRoleOpt.get();
            
            // 检查当前角色是否为普通用户
            if (!"user".equals(currentRole.getRoleCode())) {
                log.warn("用户当前角色不是普通用户，无法升级，用户ID: {}, 当前角色: {}", 
                        userId, currentRole.getRoleCode());
                throw new RuntimeException("用户当前角色不是普通用户，无法升级");
            }
            
            // 查找管理员角色
            Optional<Role> adminRoleOpt = roleRepository.findByRoleCode("admin");
            if (!adminRoleOpt.isPresent()) {
                log.error("系统错误：管理员角色不存在");
                throw new RuntimeException("系统错误：管理员角色不存在");
            }
            
            Role adminRole = adminRoleOpt.get();
            
            // 更新用户角色
            userRole.setRoleId(adminRole.getRoleId());
            userRoleRepository.save(userRole);
            
            log.info("成功升级用户角色为管理员，用户ID: {}", userId);
            
            // 发送异步日志消息
            sendOperationLog(operatorId, "UPGRADE_ROLE", 
                String.format("{\"targetUserId\":%d,\"fromRole\":\"user\",\"toRole\":\"admin\"}", userId));
            
        } catch (Exception e) {
            log.error("升级用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("升级用户角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 降级用户角色为普通用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downgradeUserToUser(Long userId, Long operatorId) {
        log.info("开始降级用户角色为普通用户，用户ID: {}, 操作者ID: {}", userId, operatorId);
        
        try {
            // 查找用户当前角色
            Optional<UserRole> userRoleOpt = userRoleRepository.findByUserId(userId);
            if (!userRoleOpt.isPresent()) {
                log.error("用户不存在或未分配角色，用户ID: {}", userId);
                throw new RuntimeException("用户未找到");
            }
            
            UserRole userRole = userRoleOpt.get();
            
            // 查找当前角色信息
            Optional<Role> currentRoleOpt = roleRepository.findById(userRole.getRoleId());
            if (!currentRoleOpt.isPresent()) {
                log.error("用户当前角色不存在，角色ID: {}", userRole.getRoleId());
                throw new RuntimeException("用户角色数据异常");
            }
            
            Role currentRole = currentRoleOpt.get();
            
            // 检查当前角色是否为管理员
            if (!"admin".equals(currentRole.getRoleCode())) {
                log.warn("用户当前角色不是管理员，无法降级，用户ID: {}, 当前角色: {}", 
                        userId, currentRole.getRoleCode());
                throw new RuntimeException("用户当前角色不是管理员，无法降级");
            }
            
            // 查找普通用户角色
            Optional<Role> userRoleRoleOpt = roleRepository.findByRoleCode("user");
            if (!userRoleRoleOpt.isPresent()) {
                log.error("系统错误：普通用户角色不存在");
                throw new RuntimeException("系统错误：普通用户角色不存在");
            }
            
            Role userRoleRole = userRoleRoleOpt.get();
            
            // 更新用户角色
            userRole.setRoleId(userRoleRole.getRoleId());
            userRoleRepository.save(userRole);
            
            log.info("成功降级用户角色为普通用户，用户ID: {}", userId);
            
            // 发送异步日志消息
            sendOperationLog(operatorId, "DOWNGRADE_ROLE", 
                String.format("{\"targetUserId\":%d,\"fromRole\":\"admin\",\"toRole\":\"user\"}", userId));
            
        } catch (Exception e) {
            log.error("降级用户角色失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("降级用户角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送操作日志消息到RocketMQ
     */
    private void sendOperationLog(Long userId, String action, String detail) {
        try {
            String ip = getClientIpAddress();
            String traceId = generateTraceId();
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(userId)
                    .traceId(traceId)
                    .action(action)
                    .ip(ip)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.convertAndSend(operationLogTopic, logMessage);
            log.debug("发送操作日志消息成功，操作: {}, 用户ID: {}, 追踪ID: {}", action, userId, traceId);
            
        } catch (Exception e) {
            // 日志发送失败不应该影响主业务
            log.warn("发送操作日志消息失败，操作: {}, 用户ID: {}, 错误: {}", action, userId, e.getMessage());
        }
    }
    
    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return "PERM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                    return xForwardedFor.split(",")[0].trim();
                }
                
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                    return xRealIp;
                }
                
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("获取客户端IP地址失败: {}", e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * 为超级管理员绑定特殊角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSuperAdminRole(Long userId, String username) {
        log.info("开始为超级管理员绑定特殊角色，用户ID: {}, 用户名: {}", userId, username);
        
        // 安全检查：只有用户名为'super_admin'的用户才能绑定超级管理员角色
        if (!"super_admin".equals(username)) {
            log.error("安全检查失败：尝试为非超级管理员用户绑定超级管理员角色，用户名: {}", username);
            throw new RuntimeException("只有super_admin用户才能绑定超级管理员角色");
        }
        
        try {
            // 检查用户是否已经分配角色，如果有则先删除
            Optional<UserRole> existingUserRole = userRoleRepository.findByUserId(userId);
            if (existingUserRole.isPresent()) {
                log.info("用户已存在角色绑定，先删除旧角色，用户ID: {}", userId);
                userRoleRepository.delete(existingUserRole.get());
            }
            
            // 查找超级管理员角色
            Optional<Role> superAdminRoleOpt = roleRepository.findByRoleCode("super_admin");
            if (!superAdminRoleOpt.isPresent()) {
                log.error("系统错误：超级管理员角色不存在");
                throw new RuntimeException("系统错误：超级管理员角色不存在");
            }
            
            Role superAdminRole = superAdminRoleOpt.get();
            log.debug("找到超级管理员角色: {} (ID: {})", superAdminRole.getRoleName(), superAdminRole.getRoleId());
            
            // 创建用户角色关系
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(superAdminRole.getRoleId());
            userRole.setGmtCreate(Timestamp.valueOf(LocalDateTime.now()));
            
            // 保存到数据库
            userRoleRepository.save(userRole);
            log.info("成功为超级管理员绑定特殊角色，用户ID: {}, 角色: {}", userId, superAdminRole.getRoleName());
            
        } catch (Exception e) {
            log.error("为超级管理员绑定特殊角色失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("绑定超级管理员角色失败: " + e.getMessage(), e);
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