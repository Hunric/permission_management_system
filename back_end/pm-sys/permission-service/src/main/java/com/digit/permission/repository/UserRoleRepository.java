package com.digit.permission.repository;

import com.digit.permission.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户角色关系数据访问接口
 * 
 * <p>提供用户角色关系相关的数据库操作方法，基于Spring Data JPA实现。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * 根据用户ID查询用户角色关系
     * 
     * @param userId 用户ID
     * @return 用户角色关系的Optional包装
     */
    Optional<UserRole> findByUserId(Long userId);
    
    /**
     * 检查用户是否已经分配角色
     * 
     * @param userId 用户ID
     * @return 存在返回true，否则返回false
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 联表查询用户角色信息
     * 
     * @param userId 用户ID
     * @return 包含角色代码和角色名称的数组，[0]为roleCode，[1]为roleName
     */
    @Query("SELECT r.roleCode, r.roleName FROM UserRole ur JOIN Role r ON ur.roleId = r.roleId WHERE ur.userId = :userId")
    Optional<Object[]> findUserRoleInfo(@Param("userId") Long userId);
    
    /**
     * 删除用户的角色关系
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    int deleteByUserId(Long userId);
    

} 