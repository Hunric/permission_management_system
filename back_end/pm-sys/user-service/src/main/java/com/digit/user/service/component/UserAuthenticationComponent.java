package com.digit.user.service.component;

import com.digit.user.dto.UserLoginDTO;
import com.digit.user.entity.User;
import com.digit.user.exception.AuthenticationException;
import com.digit.user.exception.UserNotFoundException;
import com.digit.user.repository.UserRepository;
import com.digit.user.util.JwtUtil;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户认证组件
 * 
 * <p>负责处理用户认证相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>用户登录验证</li>
 *   <li>密码验证</li>
 *   <li>JWT令牌生成</li>
 *   <li>用户信息查询</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthenticationComponent {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户实体
     * @throws AuthenticationException 如果用户不存在
     */
    public User findUserByUsername(String username) {
        log.debug("查询用户信息，用户名: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("登录失败：用户不存在，用户名: {}", username);
                    return new AuthenticationException("用户名或密码错误");
                });
    }
    
    /**
     * 验证用户密码
     * 
     * @param user 用户实体
     * @param password 输入的密码
     * @throws AuthenticationException 如果密码错误
     */
    public void validatePassword(User user, String password) {
        log.debug("验证密码，用户ID: {}", user.getUserId());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败：密码错误，用户ID: {}", user.getUserId());
            throw new AuthenticationException("用户名或密码错误");
        }
    }
    
    /**
     * 生成JWT令牌
     * 
     * @param user 用户实体
     * @return 登录响应VO
     */
    public UserLoginVO generateLoginResponse(User user) {
        log.debug("生成JWT令牌，用户ID: {}", user.getUserId());
        
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        Long expiresIn = jwtUtil.getExpiration();
        
        return UserLoginVO.builder()
                .token(token)
                .expiresIn(expiresIn)
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户实体
     * @throws UserNotFoundException 如果用户不存在
     */
    public User findUserById(Long userId) {
        log.debug("查询用户信息，用户ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });
    }
    
    /**
     * 将用户实体转换为用户信息VO
     * 
     * @param user 用户实体
     * @return 用户信息VO
     */
    public UserInfoVO convertUserToInfoVO(User user) {
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gmtCreate(user.getGmtCreate())
                .gmtModified(user.getGmtModified())
                .build();
    }
} 