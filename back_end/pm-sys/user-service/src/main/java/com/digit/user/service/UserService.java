package com.digit.user.service;

import com.digit.user.dto.UserLoginDTO;
import com.digit.user.dto.UserPageQueryDTO;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import com.digit.user.vo.UserPageVO;
import com.digit.user.vo.UserRegisterVO;

/**
 * 用户服务接口
 * 
 * <p>定义了用户管理相关的核心业务操作接口。该接口采用面向服务的架构设计，
 * 将用户相关的业务逻辑封装在服务层，为控制层提供清晰的业务操作抽象。</p>
 * 
 * <p><strong>服务设计原则：</strong></p>
 * <ul>
 *   <li>单一职责：每个方法专注于特定的业务功能</li>
 *   <li>事务管理：在实现类中统一处理数据库事务</li>
 *   <li>异常处理：将底层异常转换为业务异常</li>
 *   <li>数据转换：负责DTO与Entity之间的数据映射</li>
 * </ul>
 * 
 * <p><strong>技术特性：</strong></p>
 * <ul>
 *   <li>分布式支持：支持分库分表的用户数据存储</li>
 *   <li>事务保证：关键操作支持分布式事务</li>
 *   <li>消息集成：与RocketMQ集成进行异步处理</li>
 *   <li>缓存优化：热点数据的缓存策略（第二版实现）</li>
 * </ul>
 * 
 * <p><strong>实现要求：</strong></p>
 * <ul>
 *   <li>所有实现类必须保证线程安全</li>
 *   <li>关键操作需要记录审计日志</li>
 *   <li>异常情况需要进行适当的降级处理</li>
 *   <li>性能敏感操作需要监控和优化</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-09
 * @see com.digit.user.service.impl.UserServiceImpl 默认服务实现
 * @see com.digit.user.controller.UserController 用户控制器
 * @see com.digit.user.entity.User 用户实体类
 */
public interface UserService {
    
    /**
     * 用户注册服务方法
     * 
     * <p>处理新用户的注册业务逻辑，包括数据验证、密码加密、用户信息存储等操作。
     * 该方法是用户管理系统的核心入口，确保新用户能够安全、可靠地加入系统。</p>
     * 
     * <p><strong>业务流程详述：</strong></p>
     * <ol>
     *   <li><strong>数据预处理：</strong>
     *       <ul>
     *         <li>去除用户名和邮箱的前后空格</li>
     *         <li>验证邮箱格式的有效性（如果提供）</li>
     *       </ul>
     *   </li>
     *   <li><strong>业务规则验证：</strong>
     *       <ul>
     *         <li>检查用户名是否已存在（数据库查询）</li>
     *         <li>验证用户名是否包含敏感词汇</li>
     *       </ul>
     *   </li>
     *   <li><strong>密码安全处理：</strong>
     *       <ul>
     *         <li>使用BCrypt算法加密密码（cost factor = 12）</li>
     *         <li>生成密码盐值确保安全性</li>
     *         <li>清理内存中的明文密码</li>
     *       </ul>
     *   </li>
     *   <li><strong>用户数据创建：</strong>
     *       <ul>
     *         <li>使用雪花算法生成用户唯一ID</li>
     *         <li>记录用户注册时间</li>
     *         <li>保存用户信息到分片数据库（ShardingSphere）</li>
     *       </ul>
     *   </li>
     *   <li><strong>后续处理：</strong>
     *       <ul>
     *         <li>发送用户注册成功的异步消息</li>
     *         <li>记录用户注册操作日志</li>
     *         <li>返回注册成功的响应数据</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><strong>事务管理：</strong></p>
     * <ul>
     *   <li>整个注册过程在单个数据库事务中执行</li>
     *   <li>如果任何步骤失败，将回滚所有数据库操作</li>
     *   <li>消息发送失败不会影响用户注册成功</li>
     *   <li>支持分布式事务（如果涉及多个数据源）</li>
     * </ul>
     * 
     * <p><strong>性能优化：</strong></p>
     * <ul>
     *   <li>用户名唯一性检查使用数据库唯一索引</li>
     *   <li>密码加密采用异步处理（在可能的情况下）</li>
     *   <li>大量注册时支持批量处理模式</li>
     *   <li>数据库连接池优化高并发场景</li>
     * </ul>
     * 
     * <p><strong>安全措施：</strong></p>
     * <ul>
     *   <li>所有SQL操作使用预编译语句防止注入</li>
     *   <li>敏感信息（密码）不会记录到日志中</li>
     *   <li>用户输入数据进行XSS防护处理</li>
     *   <li>支持注册频率限制防止恶意注册</li>
     * </ul>
     * 
     * <p><strong>异常处理策略：</strong></p>
     * <ul>
     *   <li>用户名重复：抛出 {@code UserAlreadyExistsException}</li>
     *   <li>数据库异常：抛出 {@code UserRegistrationException}</li>
     *   <li>网络异常：记录日志并返回友好错误信息</li>
     * </ul>
     * 
     * @param userRegisterDTO 用户注册信息数据传输对象，包含以下必要字段：
     *                        <ul>
     *                          <li>{@code username} - 用户名，3-20字符，必填，系统唯一</li>
     *                          <li>{@code password} - 密码，6-20字符，必填，将进行BCrypt加密</li>
     *                          <li>{@code email} - 邮箱，格式验证，可选</li>
     *                          <li>{@code phone} - 手机号，11位数字，可选</li>
     *                        </ul>
     *                        
     * @return {@link UserRegisterVO} 用户注册成功响应对象，包含：
     *         <ul>
     *           <li>{@code userId} - 系统生成的用户唯一标识符</li>
     *           <li>{@code username} - 注册成功的用户名（供前端确认显示）</li>
     *         </ul>
     *         
     * @throws IllegalArgumentException 当输入参数为null或包含无效数据时
     * @throws UserAlreadyExistsException 当用户名已存在时
     * @throws UserRegistrationException 当注册过程中发生系统异常时
     * @throws DataAccessException 当数据库操作失败时
     * 
     * @implNote 实现类需要确保：
     *           <ul>
     *             <li>方法执行的原子性（全部成功或全部失败）</li>
     *             <li>并发安全性（支持多线程同时注册）</li>
     *             <li>数据一致性（分片环境下的数据同步）</li>
     *             <li>性能要求（单次注册耗时不超过500ms）</li>
     *           </ul>
     * 
     * @see UserRegisterDTO 用户注册请求数据对象
     * @see UserRegisterVO 用户注册响应数据对象
     * @see com.digit.user.entity.User 用户实体类
     * @see com.digit.user.repository.UserRepository 用户数据访问层
     * 
     * @since 1.0.0
     * @apiNote 该方法是幂等的：相同参数的重复调用应该返回相同结果（用户名重复异常）
     * @implSpec 实现类必须遵循以下规范：
     *           <ul>
     *             <li>使用 {@code @Transactional} 注解确保事务管理</li>
     *             <li>使用 {@code @Valid} 注解验证输入参数</li>
     *             <li>记录关键操作的审计日志</li>
     *             <li>异常时提供有意义的错误信息</li>
     *           </ul>
     */
    UserRegisterVO register(UserRegisterDTO userRegisterDTO);
    
    /**
     * 用户登录服务方法
     * 
     * <p>处理用户登录认证的核心业务逻辑，包括身份验证、JWT令牌生成、
     * 登录日志记录等操作。该方法是用户访问系统的主要入口。</p>
     * 
     * <p><strong>业务流程详述：</strong></p>
     * <ol>
     *   <li><strong>用户查询：</strong>
     *       <ul>
     *         <li>根据用户名在分片数据库中查询用户信息</li>
     *         <li>ShardingSphere会广播查询到所有分片</li>
     *         <li>验证用户是否存在</li>
     *       </ul>
     *   </li>
     *   <li><strong>密码验证：</strong>
     *       <ul>
     *         <li>使用BCrypt验证提供的密码与存储的哈希值</li>
     *         <li>确保密码匹配的安全性</li>
     *       </ul>
     *   </li>
     *   <li><strong>JWT令牌生成：</strong>
     *       <ul>
     *         <li>创建包含用户ID和用户名的JWT令牌</li>
     *         <li>设置合适的过期时间</li>
     *         <li>使用安全的签名算法</li>
     *       </ul>
     *   </li>
     *   <li><strong>异步日志记录：</strong>
     *       <ul>
     *         <li>向RocketMQ发送LOGIN事件消息</li>
     *         <li>记录登录时间、IP地址等信息</li>
     *         <li>用于审计和安全监控</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><strong>安全特性：</strong></p>
     * <ul>
     *   <li>密码验证失败时不泄露具体原因</li>
     *   <li>登录尝试次数限制（可扩展）</li>
     *   <li>JWT令牌安全生成和管理</li>
     *   <li>登录行为审计日志记录</li>
     * </ul>
     * 
     * <p><strong>性能优化：</strong></p>
     * <ul>
     *   <li>数据库查询使用索引优化</li>
     *   <li>JWT令牌生成采用高效算法</li>
     *   <li>异步消息发送不阻塞响应</li>
     *   <li>适当的缓存策略（可扩展）</li>
     * </ul>
     * 
     * @param userLoginDTO 用户登录信息数据传输对象，包含以下必要字段：
     *                     <ul>
     *                       <li>{@code username} - 用户名，必填</li>
     *                       <li>{@code password} - 密码，必填</li>
     *                     </ul>
     *                     
     * @return {@link UserLoginVO} 用户登录成功响应对象，包含：
     *         <ul>
     *           <li>{@code token} - JWT访问令牌</li>
     *           <li>{@code expiresIn} - 令牌过期时间（秒）</li>
     *           <li>{@code userId} - 用户ID</li>
     *           <li>{@code username} - 用户名</li>
     *         </ul>
     *         
     * @throws IllegalArgumentException 当输入参数为null或无效时
     * @throws AuthenticationException 当用户名或密码错误时
     * @throws UserNotFoundException 当用户不存在时
     * @throws SystemException 当系统异常导致登录失败时
     * 
     * @implNote 实现类需要确保：
     *           <ul>
     *             <li>密码验证的安全性（防止时序攻击）</li>
     *             <li>JWT令牌的唯一性和安全性</li>
     *             <li>登录日志的完整性和准确性</li>
     *             <li>异常处理的统一性和友好性</li>
     *           </ul>
     * 
     * @see UserLoginDTO 用户登录请求数据对象
     * @see UserLoginVO 用户登录响应数据对象
     * @see com.digit.user.util.JwtUtil JWT工具类
     * 
     * @since 1.0.0
     * @apiNote 该方法支持高并发场景，但建议对单个用户的登录频率进行限制
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);
    
    /**
     * 获取用户信息服务方法
     * 
     * <p>根据用户ID获取用户的详细信息，用于当前登录用户查看自己的个人资料。
     * 该方法利用ShardingSphere的分片键特性，能够高效地直接路由到正确的数据库分片。</p>
     * 
     * <p><strong>业务流程详述：</strong></p>
     * <ol>
     *   <li><strong>参数验证：</strong>
     *       <ul>
     *         <li>验证用户ID的有效性</li>
     *         <li>确保用户ID不为空</li>
     *       </ul>
     *   </li>
     *   <li><strong>数据查询：</strong>
     *       <ul>
     *         <li>使用用户ID作为分片键进行精确查询</li>
     *         <li>ShardingSphere直接路由到对应的物理表</li>
     *         <li>避免广播查询，性能最优</li>
     *       </ul>
     *   </li>
     *   <li><strong>数据转换：</strong>
     *       <ul>
     *         <li>将User实体转换为UserInfoVO</li>
     *         <li>过滤敏感信息（如密码）</li>
     *         <li>返回用户友好的数据格式</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><strong>性能特性：</strong></p>
     * <ul>
     *   <li>精确路由：基于分片键的查询，直接定位到单个分片</li>
     *   <li>索引优化：主键查询，使用聚簇索引，性能最佳</li>
     *   <li>缓存友好：单条记录查询，适合缓存优化</li>
     *   <li>网络开销最小：只访问一个数据库节点</li>
     * </ul>
     * 
     * <p><strong>安全特性：</strong></p>
     * <ul>
     *   <li>敏感信息过滤：不返回密码等敏感字段</li>
     *   <li>用户隔离：只能查询自己的信息</li>
     *   <li>JWT验证：确保请求来自已认证用户</li>
     * </ul>
     * 
     * @param userId 用户唯一标识符，必须是有效的已存在用户ID
     *               
     * @return {@link UserInfoVO} 用户信息响应对象，包含：
     *         <ul>
     *           <li>{@code userId} - 用户唯一标识符</li>
     *           <li>{@code username} - 用户名</li>
     *           <li>{@code email} - 邮箱地址（如果设置）</li>
     *           <li>{@code phone} - 手机号码（如果设置）</li>
     *           <li>{@code gmtCreate} - 账户创建时间</li>
     *           <li>{@code gmtModified} - 最后修改时间</li>
     *         </ul>
     *         
     * @throws IllegalArgumentException 当用户ID为null或无效时
     * @throws UserNotFoundException 当指定的用户不存在时
     * @throws DataAccessException 当数据库查询失败时
     * 
     * @implNote 实现类需要确保：
     *           <ul>
     *             <li>利用分片键优势进行高效查询</li>
     *             <li>正确过滤敏感信息</li>
     *             <li>适当的异常处理和日志记录</li>
     *             <li>数据转换的准确性</li>
     *           </ul>
     * 
     * @see UserInfoVO 用户信息响应数据对象
     * @see com.digit.user.entity.User 用户实体类
     * @see com.digit.user.repository.UserRepository 用户数据访问层
     * 
     * @since 1.0.0
     * @apiNote 该方法性能优异，适合高频调用场景，建议结合缓存使用
     */
    UserInfoVO getUserInfo(Long userId);
    
    /**
     * 分页查询用户列表服务方法
     * 
     * <p>提供管理员使用的用户列表分页查询功能，支持多条件筛选、排序和分页。
     * 该方法需要管理员或超级管理员权限，会通过OpenFeign调用权限服务验证用户角色。</p>
     * 
     * <p><strong>业务流程详述：</strong></p>
     * <ol>
     *   <li><strong>权限验证：</strong>
     *       <ul>
     *         <li>通过JWT获取当前操作者用户ID</li>
     *         <li>调用permission-service验证用户角色</li>
     *         <li>确保用户具有admin或super_admin权限</li>
     *       </ul>
     *   </li>
     *   <li><strong>参数处理：</strong>
     *       <ul>
     *         <li>验证分页参数的合法性</li>
     *         <li>解析排序字段和方向</li>
     *         <li>处理筛选条件</li>
     *       </ul>
     *   </li>
     *   <li><strong>数据查询：</strong>
     *       <ul>
     *         <li>构建动态查询条件</li>
     *         <li>执行跨分片的分页查询</li>
     *         <li>ShardingSphere处理复杂的分页和排序</li>
     *       </ul>
     *   </li>
     *   <li><strong>结果处理：</strong>
     *       <ul>
     *         <li>转换User实体为UserInfoVO</li>
     *         <li>构建分页信息</li>
     *         <li>返回完整的分页响应</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><strong>性能特性：</strong></p>
     * <ul>
     *   <li>分片查询：ShardingSphere自动处理跨库跨表查询</li>
     *   <li>智能分页：支持复杂的分页和排序合并</li>
     *   <li>条件优化：根据筛选条件减少查询范围</li>
     *   <li>结果缓存：可配合Redis缓存提升性能</li>
     * </ul>
     * 
     * <p><strong>安全特性：</strong></p>
     * <ul>
     *   <li>权限控制：严格的角色权限验证</li>
     *   <li>数据过滤：自动过滤敏感信息</li>
     *   <li>参数校验：防止SQL注入和参数攻击</li>
     *   <li>审计日志：记录管理员操作日志</li>
     * </ul>
     * 
     * <p><strong>支持的筛选条件：</strong></p>
     * <ul>
     *   <li>用户名模糊匹配</li>
     *   <li>邮箱模糊匹配</li>
     *   <li>手机号模糊匹配</li>
     *   <li>创建时间范围查询</li>
     * </ul>
     * 
     * <p><strong>支持的排序字段：</strong></p>
     * <ul>
     *   <li>userId - 用户ID</li>
     *   <li>username - 用户名</li>
     *   <li>email - 邮箱</li>
     *   <li>gmtCreate - 创建时间</li>
     *   <li>gmtModified - 修改时间</li>
     * </ul>
     * 
     * @param queryDTO 分页查询参数，包含：
     *                 <ul>
     *                   <li>{@code page} - 页码（从1开始）</li>
     *                   <li>{@code size} - 每页大小（1-100）</li>
     *                   <li>{@code sort} - 排序字段和方向</li>
     *                   <li>{@code username} - 用户名筛选</li>
     *                   <li>{@code email} - 邮箱筛选</li>
     *                   <li>{@code phone} - 手机号筛选</li>
     *                   <li>{@code gmtCreateStart} - 创建时间开始</li>
     *                   <li>{@code gmtCreateEnd} - 创建时间结束</li>
     *                 </ul>
     *                 
     * @return {@link UserPageVO} 分页查询结果，包含：
     *         <ul>
     *           <li>{@code users} - 用户信息列表</li>
     *           <li>{@code currentPage} - 当前页码</li>
     *           <li>{@code pageSize} - 每页大小</li>
     *           <li>{@code totalElements} - 总记录数</li>
     *           <li>{@code totalPages} - 总页数</li>
     *           <li>{@code isFirst/isLast} - 分页状态</li>
     *           <li>{@code hasPrevious/hasNext} - 翻页状态</li>
     *         </ul>
     *         
     * @throws IllegalArgumentException 当查询参数无效时
     * @throws SecurityException 当用户权限不足时
     * @throws DataAccessException 当数据库查询失败时
     * @throws ServiceException 当权限服务调用失败时
     * 
     * @implNote 实现类需要确保：
     *           <ul>
     *             <li>严格的权限验证</li>
     *             <li>参数的安全校验</li>
     *             <li>高效的分片查询</li>
     *             <li>完整的异常处理</li>
     *             <li>敏感信息的过滤</li>
     *           </ul>
     * 
     * @see UserPageQueryDTO 分页查询请求对象
     * @see UserPageVO 分页查询响应对象
     * @see UserInfoVO 用户信息对象
     * @see com.digit.user.repository.UserRepository 用户数据访问层
     * @see com.digit.user.rcp.PermissionFeignClient 权限服务客户端
     * 
     * @since 1.0.0
     * @apiNote 该方法涉及跨服务调用和复杂查询，建议合理设置超时时间和缓存策略
     */
    UserPageVO getUsers(UserPageQueryDTO queryDTO);
} 