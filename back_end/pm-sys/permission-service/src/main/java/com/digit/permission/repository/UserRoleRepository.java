package com.digit.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRoleRepository<UserRole> extends JpaRepository<UserRole, Long> {
    // 我们稍后会添加自定义查询
}
