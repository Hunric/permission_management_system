package com.digit.user.service.component;

import com.digit.user.dto.UserUpdateDTO;
import com.digit.user.entity.User;
import com.digit.user.exception.UserNotFoundException;
import com.digit.user.repository.UserRepository;
import com.digit.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息更新组件
 * 
 * <p>负责处理用户信息修改相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>用户信息查询</li>
 *   <li>用户信息更新</li>
 *   <li>变更记录生成</li>
 *   <li>DTO与实体的转换</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserUpdateComponent {
    
    private final UserRepository userRepository;
    
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
     * 更新用户信息
     * 
     * @param user 用户实体
     * @param updateDTO 更新数据
     * @return 更新后的用户实体
     */
    public User updateUserInfo(User user, UserUpdateDTO updateDTO) {
        log.debug("更新用户信息，用户ID: {}", user.getUserId());
        
        // 记录原始值用于日志
        String originalEmail = user.getEmail();
        String originalPhone = user.getPhone();
        
        // 更新邮箱
        if (updateDTO.hasEmailUpdate()) {
            user.setEmail(updateDTO.getEmail());
            log.debug("更新邮箱，用户ID: {}, 原邮箱: {}, 新邮箱: {}", 
                     user.getUserId(), originalEmail, updateDTO.getEmail());
        }
        
        // 更新手机号
        if (updateDTO.hasPhoneUpdate()) {
            user.setPhone(updateDTO.getPhone());
            log.debug("更新手机号，用户ID: {}, 原手机号: {}, 新手机号: {}", 
                     user.getUserId(), originalPhone, updateDTO.getPhone());
        }
        
        // 保存更新
        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功，用户ID: {}", user.getUserId());
        
        return updatedUser;
    }
    
    /**
     * 生成变更详情JSON字符串
     * 
     * @param originalUser 原始用户信息
     * @param updateDTO 更新数据
     * @return JSON格式的变更详情
     */
    public String generateChangeDetails(User originalUser, UserUpdateDTO updateDTO) {
        Map<String, Object> changes = new HashMap<>();
        
        if (updateDTO.hasEmailUpdate() && !updateDTO.getEmail().equals(originalUser.getEmail())) {
            Map<String, String> emailChange = new HashMap<>();
            emailChange.put("field", "email");
            emailChange.put("oldValue", originalUser.getEmail());
            emailChange.put("newValue", updateDTO.getEmail());
            changes.put("email", emailChange);
        }
        
        if (updateDTO.hasPhoneUpdate() && !updateDTO.getPhone().equals(originalUser.getPhone())) {
            Map<String, String> phoneChange = new HashMap<>();
            phoneChange.put("field", "phone");
            phoneChange.put("oldValue", originalUser.getPhone());
            phoneChange.put("newValue", updateDTO.getPhone());
            changes.put("phone", phoneChange);
        }
        
        // 简单的JSON序列化，实际项目中建议使用Jackson
        if (changes.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            @SuppressWarnings("unchecked")
            Map<String, String> change = (Map<String, String>) entry.getValue();
            json.append("{");
            json.append("\"field\":\"").append(change.get("field")).append("\",");
            json.append("\"oldValue\":\"").append(change.get("oldValue") != null ? change.get("oldValue") : "").append("\",");
            json.append("\"newValue\":\"").append(change.get("newValue") != null ? change.get("newValue") : "").append("\"");
            json.append("}");
            
            first = false;
        }
        json.append("}");
        
        return json.toString();
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
    
    /**
     * 检查更新数据是否有效
     * 
     * @param updateDTO 更新数据
     * @throws IllegalArgumentException 如果没有任何字段需要更新
     */
    public void validateUpdateData(UserUpdateDTO updateDTO) {
        if (!updateDTO.hasUpdates()) {
            log.warn("用户信息更新失败：没有提供任何需要更新的字段");
            throw new IllegalArgumentException("至少需要提供一个需要更新的字段");
        }
    }
} 