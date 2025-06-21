package com.digit.permission.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 用户角色关系实体类
 * 
 * <p>映射permission_db数据库中的user_roles表，用于存储用户与角色的关联关系。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles")
public class UserRole {
    
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 用户ID（关联users表的user_id）
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 角色ID（关联roles表的role_id）
     */
    @Column(name = "role_id", nullable = false)
    private Integer roleId;
    
    /**
     * 创建时间
     */
    @Column(name = "gmt_create", nullable = false)
    private Timestamp gmtCreate;
} 