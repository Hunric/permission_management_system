package com.digit.permission.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 角色实体类
 * 
 * <p>映射permission_db数据库中的roles表，用于存储系统角色信息。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    
    /**
     * 角色ID（自增主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    
    /**
     * 角色代码（程序中使用）
     * 如：super_admin, admin, user
     */
    @Column(name = "role_code", nullable = false, unique = true, length = 20)
    private String roleCode;
    
    /**
     * 角色名称（用于显示）
     * 如：超级管理员, 管理员, 普通用户
     */
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;
    
    /**
     * 创建时间
     */
    @Column(name = "gmt_create", nullable = false)
    private Timestamp gmtCreate;
} 