package com.digit.user.service.component;

import com.digit.user.dto.ChangePasswordDTO;
import com.digit.user.dto.ResetPasswordResponse;
import com.digit.user.entity.User;
import com.digit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码管理组件
 * 
 * <p>负责处理密码相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>密码修改</li>
 *   <li>密码重置</li>
 *   <li>密码验证</li>
 *   <li>密码加密</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordManagementComponent {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 默认重置密码
    private static final String DEFAULT_RESET_PASSWORD = "123456";
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户实体
     * @throws RuntimeException 如果用户不存在
     */
    public User findUserById(Long userId) {
        log.debug("查找用户，用户ID: {}", userId);
        
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new RuntimeException("用户不存在");
                });
    }
    
    /**
     * 验证旧密码是否正确
     * 
     * @param user 用户实体
     * @param oldPassword 旧密码（明文）
     * @throws RuntimeException 如果密码不匹配
     */
    public void validateOldPassword(User user, String oldPassword) {
        log.debug("验证用户旧密码，用户ID: {}", user.getUserId());
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("旧密码验证失败，用户ID: {}", user.getUserId());
            throw new RuntimeException("旧密码不正确");
        }
        
        log.debug("旧密码验证通过，用户ID: {}", user.getUserId());
    }
    
    /**
     * 修改用户密码
     * 
     * @param user 用户实体
     * @param changePasswordDTO 密码修改请求
     * @return 更新后的用户实体
     */
    public User changePassword(User user, ChangePasswordDTO changePasswordDTO) {
        log.debug("修改用户密码，用户ID: {}", user.getUserId());
        
        // 验证旧密码
        validateOldPassword(user, changePasswordDTO.getOldPassword());
        
        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        
        // 更新密码
        user.setPassword(encodedNewPassword);
        
        // 保存到数据库
        User savedUser = userRepository.save(user);
        
        log.info("用户密码修改成功，用户ID: {}", user.getUserId());
        return savedUser;
    }
    
    /**
     * 重置用户密码为默认密码
     * 
     * @param user 用户实体
     * @return 重置密码响应
     */
    public ResetPasswordResponse resetPassword(User user) {
        log.debug("重置用户密码，用户ID: {}", user.getUserId());
        
        // 加密默认密码
        String encodedDefaultPassword = passwordEncoder.encode(DEFAULT_RESET_PASSWORD);
        
        // 更新密码
        user.setPassword(encodedDefaultPassword);
        
        // 保存到数据库
        userRepository.save(user);
        
        log.info("用户密码重置成功，用户ID: {}, 用户名: {}", user.getUserId(), user.getUsername());
        
        return ResetPasswordResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .newPassword(DEFAULT_RESET_PASSWORD)
                .message("密码已重置为默认密码，请尽快修改")
                .build();
    }
    
    /**
     * 生成密码修改变更详情JSON
     * 
     * @param user 用户实体
     * @param operationType 操作类型（CHANGE_PASSWORD 或 RESET_PASSWORD）
     * @return JSON格式的变更详情
     */
    public String generatePasswordChangeDetails(User user, String operationType) {
        return String.format(
                "{\"action\":\"%s\",\"userId\":%d,\"username\":\"%s\",\"timestamp\":\"%s\"}",
                operationType,
                user.getUserId(),
                escapeJson(user.getUsername()),
                java.time.Instant.now().toString()
        );
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     * 
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
} 