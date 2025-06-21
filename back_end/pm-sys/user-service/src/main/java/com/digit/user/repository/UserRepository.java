package com.digit.user.repository;

import com.digit.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

/**
 * 用户数据访问层接口
 * 
 * <p>该接口继承自JpaRepository，提供用户实体的基础CRUD操作。
 * 由于使用了ShardingSphere分片中间件，某些查询会自动路由到相应的分片。</p>
 * 
 * <p><strong>分片查询策略：</strong></p>
 * <ul>
 *   <li><strong>精确查询：</strong>根据user_id查询时，ShardingSphere会精确路由到对应分片</li>
 *   <li><strong>广播查询：</strong>根据username查询时，会广播到所有分片进行查询</li>
 *   <li><strong>全表扫描：</strong>无分片键的复杂查询会扫描所有分片并合并结果</li>
 * </ul>
 * 
 * <p><strong>核心业务功能：</strong></p>
 * <ul>
 *   <li>用户注册时的用户名唯一性验证</li>
 *   <li>用户登录时的身份验证查询</li>
 *   <li>用户信息的查询和更新操作</li>
 *   <li>管理员的用户列表分页查询</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 * @see User 用户实体类
 * @see com.digit.user.service.UserService 用户业务服务
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 检查用户名是否已存在
     * 
     * <p>该方法用于用户注册时的用户名唯一性验证。
     * ShardingSphere会将此查询广播到所有分片进行检查，
     * 只要任何一个分片返回true，整体结果就为true。</p>
     * 
     * <p><strong>查询特性：</strong></p>
     * <ul>
     *   <li>广播查询：查询会发送到所有分片</li>
     *   <li>索引优化：利用username唯一索引提升性能</li>
     *   <li>快速响应：一旦发现存在即返回true</li>
     * </ul>
     * 
     * @param username 要检查的用户名
     * @return 如果用户名已存在返回true，否则返回false
     * @throws IllegalArgumentException 如果username为null或空字符串
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据用户名查找用户
     * 
     * <p>该方法主要用于用户登录认证。由于username具有全局唯一性，
     * ShardingSphere会广播查询到所有分片，但最多只会返回一个结果。</p>
     * 
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *   <li>用户登录验证</li>
     *   <li>密码重置验证</li>
     *   <li>用户信息查询</li>
     * </ul>
     * 
     * @param username 用户名
     * @return Optional包装的用户对象，如果不存在则为empty
     * @throws IllegalArgumentException 如果username为null或空字符串
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据用户名或邮箱查找用户
     * 
     * <p>支持多种登录方式的统一查询接口。
     * 用户可以使用用户名或邮箱进行登录。</p>
     * 
     * <p><strong>注意：</strong>该查询会广播到所有分片，性能相对较低，
     * 建议优先使用username查询。</p>
     * 
     * @param username 用户名
     * @param email 邮箱地址
     * @return Optional包装的用户对象
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * 分页查询用户列表
     * 
     * <p>用于管理员的用户管理功能。该查询会扫描所有分片，
     * ShardingSphere会处理跨库/跨表的分页、排序和聚合。</p>
     * 
     * <p><strong>性能建议：</strong></p>
     * <ul>
     *   <li>建议在非高峰期执行大量数据的分页查询</li>
     *   <li>合理设置页面大小，避免单次查询数据量过大</li>
     *   <li>可以考虑添加筛选条件减少查询范围</li>
     * </ul>
     * 
     * @param pageable 分页参数（页码、页面大小、排序）
     * @return 分页结果
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * 根据用户名模糊查询用户列表
     * 
     * <p>用于管理员的用户搜索功能。
     * 注意：模糊查询会导致全分片扫描，性能较差，建议谨慎使用。</p>
     * 
     * @param username 用户名关键字
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    /**
     * 批量查询用户
     * 
     * <p>根据用户ID列表批量查询用户信息。
     * ShardingSphere会根据每个ID路由到对应的分片，性能较好。</p>
     * 
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findByUserIdIn(@Param("userIds") List<Long> userIds);
} 