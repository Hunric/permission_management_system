package com.digit.permission.repository;

import com.digit.permission.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * <p>提供角色相关的数据库操作方法，基于Spring Data JPA实现。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * 根据角色代码查询角色
     * 
     * @param roleCode 角色代码，如"user", "admin", "super_admin"
     * @return 角色信息的Optional包装
     */
    Optional<Role> findByRoleCode(String roleCode);
    
    /**
     * 检查角色代码是否存在
     * 
     * @param roleCode 角色代码
     * @return 存在返回true，否则返回false
     */
    boolean existsByRoleCode(String roleCode);
} 