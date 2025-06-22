package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息更新请求数据传输对象
 * 
 * <p>封装用户信息修改时客户端提交的数据。该DTO只包含可修改的字段，
 * 用户名和用户ID等关键标识符不可修改。</p>
 * 
 * <p><strong>可修改字段：</strong></p>
 * <ul>
 *   <li>邮箱地址</li>
 *   <li>手机号码</li>
 * </ul>
 * 
 * <p><strong>不可修改字段：</strong></p>
 * <ul>
 *   <li>用户ID - 系统唯一标识符</li>
 *   <li>用户名 - 登录凭证，创建后不可修改</li>
 *   <li>密码 - 通过专门的密码修改接口处理</li>
 *   <li>创建时间和修改时间 - 系统自动维护</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    
    /**
     * 邮箱地址
     * 
     * <p>用户的电子邮件地址，用于账户验证、密码重置、系统通知等功能。
     * 如果不需要修改邮箱，此字段可以为空。</p>
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    /**
     * 手机号码
     * 
     * <p>用户的移动电话号码，用于短信验证、双因子认证、紧急通知等安全功能。
     * 如果不需要修改手机号，此字段可以为空。</p>
     */
    @Size(min = 11, max = 11, message = "手机号码必须为11位数字")
    private String phone;
    
    /**
     * 检查是否有任何字段需要更新
     * 
     * @return 如果有任何非空字段返回true，否则返回false
     */
    public boolean hasUpdates() {
        return (email != null && !email.trim().isEmpty()) ||
               (phone != null && !phone.trim().isEmpty());
    }
    
    /**
     * 检查邮箱是否需要更新
     * 
     * @return 如果邮箱字段非空返回true，否则返回false
     */
    public boolean hasEmailUpdate() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * 检查手机号是否需要更新
     * 
     * @return 如果手机号字段非空返回true，否则返回false
     */
    public boolean hasPhoneUpdate() {
        return phone != null && !phone.trim().isEmpty();
    }
    
    /**
     * 获取需要更新的字段列表
     * 
     * @return 包含需要更新字段名称的列表
     */
    public List<String> getUpdateFields() {
        List<String> fields = new ArrayList<>();
        
        if (hasEmailUpdate()) {
            fields.add("email");
        }
        
        if (hasPhoneUpdate()) {
            fields.add("phone");
        }
        
        return fields;
    }
} 