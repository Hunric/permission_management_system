package com.digit.user.service.component;

import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.entity.User;
import com.digit.user.exception.UserAlreadyExistsException;
import com.digit.user.repository.UserRepository;
import com.digit.user.rcp.PermissionFeignClient;
import com.digit.user.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 用户注册组件
 * 
 * <p>负责处理用户注册相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>用户名唯一性检查</li>
 *   <li>密码加密</li>
 *   <li>用户实体创建和保存</li>
 *   <li>默认角色绑定</li>
 *   <li>DTO与实体的转换</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationComponent {
    
    private final UserRepository userRepository;
    private final PermissionFeignClient permissionFeignClient;
    private final BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @throws UserAlreadyExistsException 如果用户名已存在
     */
    public void validateUsernameUniqueness(String username) {
        log.debug("检查用户名是否已存在: {}", username);
        if (userRepository.existsByUsername(username)) {
            log.warn("用户名已存在: {}", username);
            throw UserAlreadyExistsException.forUsername(username);
        }
    }
    
    /**
     * 对密码进行加密
     * 
     * @param password 明文密码
     * @return 加密后的密码
     */
    public String encodePassword(String password) {
        log.debug("对密码进行BCrypt加密");
        String encodedPassword = passwordEncoder.encode(password);
        log.debug("密码加密完成");
        return encodedPassword;
    }
    
    /**
     * 创建并保存用户实体
     * 
     * @param userRegisterDTO 用户注册DTO
     * @param encodedPassword 加密后的密码
     * @return 保存后的用户实体
     */
    public User createAndSaveUser(UserRegisterDTO userRegisterDTO, String encodedPassword) {
        log.debug("创建用户实体，用户名: {}", userRegisterDTO.getUsername());
        
        User user = convertDtoToEntity(userRegisterDTO, encodedPassword);
        
        log.debug("保存用户实体到分片数据库，ShardingSphere将自动生成雪花算法ID");
        User savedUser = userRepository.save(user);
        
        log.info("用户数据持久化成功，用户ID: {}", savedUser.getUserId());
        return savedUser;
    }
    
    /**
     * 为用户绑定默认角色
     * 
     * @param userId 用户ID
     */
    public void bindDefaultRole(Long userId) {
        log.debug("通过RPC调用permission-service绑定默认角色，用户ID: {}", userId);
        try {
            permissionFeignClient.bindDefaultRole(userId);
            log.info("默认角色绑定成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("RPC调用失败，绑定默认角色异常，用户ID: {}, 错误: {}", userId, e.getMessage());
            throw new RuntimeException("绑定默认角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将DTO转换为实体
     * 
     * @param dto 用户注册DTO
     * @param encodedPassword 加密后的密码
     * @return 用户实体
     */
    private User convertDtoToEntity(UserRegisterDTO dto, String encodedPassword) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setGmtCreate(Timestamp.valueOf(LocalDateTime.now()));
        user.setGmtModified(Timestamp.valueOf(LocalDateTime.now()));
        return user;
    }
    
    /**
     * 将实体转换为VO
     * 
     * @param user 用户实体
     * @return 用户注册VO
     */
    public UserRegisterVO convertEntityToVO(User user) {
        return UserRegisterVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
} 