package com.digit.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * <p>映射到users逻辑表，ShardingSphere会根据user_id进行分片路由。
 * 该实体是用户服务的核心聚合根，包含用户的基本信息和认证凭据。</p>
 * 
 * <p><strong>分片策略：</strong></p>
 * <ul>
 *   <li>分片键：user_id</li>
 *   <li>分库算法：user_id % 2 (分到 user_db_0 或 user_db_1)</li>
 *   <li>分表算法：user_id % 2 (分到 users_0 或 users_1)</li>
 *   <li>ID生成：ShardingSphere雪花算法自动生成</li>
 * </ul>
 * 
 * <p><strong>业务功能：</strong></p>
 * <ul>
 *   <li>用户注册、登录、登出</li>
 *   <li>用户信息查询和修改</li>
 *   <li>密码修改和重置</li>
 *   <li>支持用户名和邮箱登录</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 * @see com.digit.user.repository.UserRepository 用户数据访问层
 * @see com.digit.user.dto.UserRegisterDTO 用户注册DTO
 * @see com.digit.user.vo.UserRegisterVO 用户注册响应VO
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "uk_username", columnList = "username", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /**
     * 用户唯一标识符
     * 
     * <p>由ShardingSphere的雪花算法自动生成的分布式唯一ID。
     * 该ID作为分片键，用于确定数据存储的物理位置。
     * ID生成策略在shardingsphere-config.yaml中配置。</p>
     * 
     * <p><strong>注意：</strong>使用ShardingSphere时，不使用JPA的@GeneratedValue注解，
     * ID生成完全由ShardingSphere的keyGenerateStrategy处理。</p>
     */
    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "BIGINT COMMENT '用户唯一标识符，雪花算法生成'")
    private Long userId;
    
    /**
     * 用户名
     * 
     * <p>用户的登录凭证和唯一标识符。创建后不可修改，
     * 必须在整个系统中保持唯一性。</p>
     */
    @Column(name = "username", nullable = false, unique = true, length = 50, 
            columnDefinition = "VARCHAR(50) NOT NULL COMMENT '用户名，登录凭证'")
    private String username;
    
    /**
     * 登录密码
     * 
     * <p>用户的认证凭据，使用BCrypt算法加密存储。
     * 原始密码永远不会以明文形式存储在数据库中。</p>
     */
    @Column(name = "password", nullable = false, length = 255,
            columnDefinition = "VARCHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码'")
    private String password;
    
    /**
     * 邮箱地址
     * 
     * <p>用户的电子邮件地址，用于账户验证、密码重置、系统通知等功能。
     * 可以作为备用登录凭证。</p>
     */
    @Column(name = "email", length = 100,
            columnDefinition = "VARCHAR(100) NULL COMMENT '邮箱地址，用于通知和验证'")
    private String email;
    
    /**
     * 手机号码
     * 
     * <p>用户的移动电话号码，用于短信验证、双因子认证、紧急通知等安全功能。</p>
     */
    @Column(name = "phone", length = 20,
            columnDefinition = "VARCHAR(20) NULL COMMENT '手机号码，用于短信验证'")
    private String phone;
    
    /**
     * 创建时间
     * 
     * <p>记录用户账户的创建时间，用于审计和统计分析。
     * 该字段在插入时自动设置，后续不可修改。</p>
     */
    @Column(name = "gmt_create", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'")
    private Timestamp gmtCreate;
    
    /**
     * 最后修改时间
     * 
     * <p>记录用户信息的最后修改时间，用于数据同步和变更追踪。
     * 该字段在每次更新时自动更新。</p>
     */
    @Column(name = "gmt_modified", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '最后修改时间'")
    private Timestamp gmtModified;
    
    /**
     * JPA生命周期回调：插入前设置创建和修改时间
     */
    @PrePersist
    protected void onCreate() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        this.gmtCreate = now;
        this.gmtModified = now;
    }
    
    /**
     * JPA生命周期回调：更新前设置修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.gmtModified = Timestamp.valueOf(LocalDateTime.now());
    }
    
    /**
     * 检查用户是否有邮箱
     * 
     * @return 如果用户设置了邮箱返回true，否则返回false
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * 检查用户是否有手机号
     * 
     * @return 如果用户设置了手机号返回true，否则返回false
     */
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }
    
    /**
     * 重写toString方法，避免输出敏感信息
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }
} 