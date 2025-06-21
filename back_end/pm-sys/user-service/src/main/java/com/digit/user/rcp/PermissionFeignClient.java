package com.digit.user.rcp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 权限服务Feign客户端
 * 
 * <p>该接口定义了与权限服务(permission-service)的RPC通信协议。
 * 使用Spring Cloud OpenFeign实现服务间的HTTP调用。</p>
 * 
 * <p><strong>核心功能：</strong></p>
 * <ul>
 *   <li><strong>用户注册：</strong>为新用户绑定默认角色</li>
 *   <li><strong>权限验证：</strong>查询用户角色进行权限校验</li>
 *   <li><strong>角色管理：</strong>支持角色升级和降级操作</li>
 * </ul>
 * 
 * <p><strong>技术特性：</strong></p>
 * <ul>
 *   <li><strong>服务发现：</strong>通过Nacos自动发现permission-service实例</li>
 *   <li><strong>负载均衡：</strong>使用Ribbon进行客户端负载均衡</li>
 *   <li><strong>容错保护：</strong>提供fallback降级处理</li>
 *   <li><strong>超时控制：</strong>设置合理的超时时间</li>
 * </ul>
 * 
 * <p><strong>分布式事务集成：</strong></p>
 * <ul>
 *   <li>支持Seata分布式事务，确保跨服务数据一致性</li>
 *   <li>权限服务操作作为分布式事务的分支事务</li>
 *   <li>如果用户服务或权限服务任一失败，整个事务回滚</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 * @see com.digit.user.service.impl.UserServiceImpl 用户服务实现
 */
@FeignClient(
    name = "permission-service",
    path = "/permission",
    fallback = PermissionFeignClientFallback.class
)
public interface PermissionFeignClient {
    
    /**
     * 为用户绑定默认角色
     * 
     * <p>该方法是用户注册流程中的关键步骤，用于为新注册用户分配默认角色。
     * 这是Seata分布式事务的第二个分支事务，必须与用户创建操作保持原子性。</p>
     * 
     * <p><strong>业务逻辑：</strong></p>
     * <ul>
     *   <li>为新用户分配"普通用户"角色</li>
     *   <li>在permission_db中插入用户角色关系记录</li>
     *   <li>如果失败，Seata会回滚整个注册事务</li>
     * </ul>
     * 
     * <p><strong>容错机制：</strong></p>
     * <ul>
     *   <li>超时：防止长时间阻塞</li>
     *   <li>降级：提供fallback处理</li>
     * </ul>
     * 
     * @param userId 用户ID，必须是有效的用户标识符
     * @throws feign.FeignException 当权限服务调用失败时抛出
     * @throws java.util.concurrent.TimeoutException 当调用超时时抛出
     */
    @PostMapping("/internal/roles/bind-default")
    void bindDefaultRole(@RequestParam("userId") Long userId);
    
    /**
     * 查询用户角色
     * 
     * <p>获取用户当前的角色信息，用于权限验证和业务逻辑判断。
     * 该方法主要用于权限校验场景。</p>
     * 
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *   <li>用户访问需要权限的接口时进行角色验证</li>
     *   <li>管理员功能的权限检查</li>
     *   <li>用户信息展示时显示角色信息</li>
     * </ul>
     * 
     * @param userId 用户ID
     * @return 用户角色信息，包含roleCode等字段
     */
    @GetMapping("/internal/user/{userId}/role")
    UserRoleResponse getUserRole(@PathVariable("userId") Long userId);
    
    /**
     * 升级用户角色为管理员
     * 
     * <p>将普通用户的角色提升为管理员。只有超级管理员才能执行此操作。</p>
     * 
     * @param userId 要升级的用户ID
     */
    @PostMapping("/user/{userId}/upgrade-to-admin")
    void upgradeToAdmin(@PathVariable("userId") Long userId);
    
    /**
     * 降级用户角色为普通用户
     * 
     * <p>将管理员的角色降级为普通用户。只有超级管理员才能执行此操作。</p>
     * 
     * @param userId 要降级的用户ID
     */
    @PostMapping("/user/{userId}/downgrade-to-user")
    void downgradeToUser(@PathVariable("userId") Long userId);
}

/**
 * 用户角色响应对象
 * 
 * <p>用于封装从权限服务返回的用户角色信息。</p>
 */
class UserRoleResponse {
    private String roleCode;
    private String roleName;
    
    // Getters and Setters
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    @Override
    public String toString() {
        return "UserRoleResponse{" +
                "roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}

/**
 * Feign客户端降级处理类
 * 
 * <p>当权限服务完全不可用时的降级处理实现。</p>
 */
class PermissionFeignClientFallback implements PermissionFeignClient {
    
    @Override
    public void bindDefaultRole(Long userId) {
        throw new RuntimeException("权限服务暂时不可用，无法为用户绑定默认角色");
    }
    
    @Override
    public UserRoleResponse getUserRole(Long userId) {
        // 返回默认的普通用户角色
        UserRoleResponse response = new UserRoleResponse();
        response.setRoleCode("user");
        response.setRoleName("普通用户");
        return response;
    }
    
    @Override
    public void upgradeToAdmin(Long userId) {
        throw new RuntimeException("权限服务暂时不可用，无法升级用户角色");
    }
    
    @Override
    public void downgradeToUser(Long userId) {
        throw new RuntimeException("权限服务暂时不可用，无法降级用户角色");
    }
} 