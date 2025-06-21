package com.digit.user.service.impl;

import com.digit.user.dto.UserLoginDTO;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.dto.OperationLogMessage;
import com.digit.user.entity.User;
import com.digit.user.exception.AuthenticationException;
import com.digit.user.exception.UserAlreadyExistsException;
import com.digit.user.exception.UserNotFoundException;
import com.digit.user.repository.UserRepository;
import com.digit.user.rcp.PermissionFeignClient;
import com.digit.user.service.UserService;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import com.digit.user.vo.UserRegisterVO;
import com.digit.user.util.IpAddressUtil;
import com.digit.user.util.JwtUtil;
// import com.digit.user.util.ShardingSphereIdGenerator;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.core.context.RootContext;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    // 注入依赖的组件
    private final UserRepository userRepository;
    private final PermissionFeignClient permissionFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final BCryptPasswordEncoder passwordEncoder;// 需要创建安全配置类
    private final JwtUtil jwtUtil;
    // private final ShardingSphereIdGenerator idGenerator;
    
    /**
     * 用户注册实现
     * 按照以下步骤执行：
     * 1. 检查用户名是否已存在
     * 2. 对密码进行加密
     * 3. 将 DTO 转换为 User 实体
     * 4. 持久化用户实体 (ShardingSphere 在此介入)
     * 5. RPC调用，为用户绑定默认角色 (Seata 分支事务二)
     * 6. 异步发送操作日志消息 (在事务成功后执行)
     * 7. 将结果转换为VO并返回
     */
    @Override
    @GlobalTransactional(name = "user-register-tx", rollbackFor = Exception.class)
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {
        log.info("开始注册用户: {}", userRegisterDTO.getUsername());
        
        try {
            // 步骤 1: 检查用户名是否已存在
            log.debug("步骤 1: 检查用户名是否已存在");
            if (userRepository.existsByUsername(userRegisterDTO.getUsername())) {
                log.warn("用户名已存在: {}", userRegisterDTO.getUsername());
                throw UserAlreadyExistsException.forUsername(userRegisterDTO.getUsername());
            }
            
            // 步骤 2: 对密码进行加密
            log.debug("步骤 2: 对密码进行BCrypt加密");
            String encodedPassword = passwordEncoder.encode(userRegisterDTO.getPassword());
            log.debug("密码加密完成");
            
            // 步骤 3: 将 DTO 转换为 User 实体
            log.debug("步骤 3: 将 DTO 转换为 User 实体");
            User user = convertDtoToEntity(userRegisterDTO, encodedPassword);
            log.debug("实体转换完成，用户名: {}", user.getUsername());
            
            // 步骤 4: 持久化用户实体 (ShardingSphere 自动生成雪花算法ID)
            log.debug("步骤 4: 持久化用户实体到分片数据库，ShardingSphere将自动生成雪花算法ID");
            
            log.debug("准备保存用户实体，用户名: {}, ID将由ShardingSphere自动生成", user.getUsername());
            
            // 使用 save 而不是 saveAndFlush，让 Seata 管理事务
            User savedUser = userRepository.save(user);
            log.info("用户数据持久化成功，用户ID: {}", savedUser.getUserId());
            
            // 步骤 5: RPC调用，为用户绑定默认角色 (Seata 分支事务二)
            log.debug("步骤 5: 通过RPC调用permission-service绑定默认角色");
            try {
                permissionFeignClient.bindDefaultRole(savedUser.getUserId());
                log.info("默认角色绑定成功，用户ID: {}", savedUser.getUserId());
            } catch (Exception e) {
                log.error("RPC调用失败，绑定默认角色异常，用户ID: {}, 错误: {}", 
                         savedUser.getUserId(), e.getMessage());
                throw new RuntimeException("绑定默认角色失败: " + e.getMessage(), e);
            }
            
            // 步骤 6: 异步发送操作日志消息 (在事务成功后执行)
            log.debug("步骤 6: 异步发送用户注册操作日志");
            sendRegistrationLogAsync(savedUser);
            
            // 步骤 7: 将结果转换为VO并返回
            log.debug("步骤 7: 转换结果为VO对象");
            UserRegisterVO result = convertEntityToVO(savedUser);
            
            log.info("用户 {} 注册成功，用户ID: {}", savedUser.getUsername(), savedUser.getUserId());
            return result;
            
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}, 错误: {}", userRegisterDTO.getUsername(), e.getMessage(), e);
            // Seata会自动回滚所有操作：
            // 1. 删除在 user_db 中插入的用户记录
            // 2. 回滚在 permission_db 中插入的用户角色关系
            throw e;
        }
    }
    
    /**
     * 用户登录实现
     * 按照以下步骤执行：
     * 1. 根据用户名查询用户信息
     * 2. 验证密码是否正确
     * 3. 生成JWT令牌
     * 4. 异步发送登录日志消息
     * 5. 返回登录响应信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        log.info("用户登录请求: {}", userLoginDTO.getUsername());
        
        try {
            // 步骤 1: 根据用户名查询用户信息
            log.debug("步骤 1: 查询用户信息，用户名: {}", userLoginDTO.getUsername());
            User user = userRepository.findByUsername(userLoginDTO.getUsername())
                    .orElseThrow(() -> {
                        log.warn("登录失败：用户不存在，用户名: {}", userLoginDTO.getUsername());
                        return new AuthenticationException("用户名或密码错误");
                    });
            
            // 步骤 2: 验证密码是否正确
            log.debug("步骤 2: 验证密码，用户ID: {}", user.getUserId());
            if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
                log.warn("登录失败：密码错误，用户ID: {}", user.getUserId());
                throw new AuthenticationException("用户名或密码错误");
            }
            
            // 步骤 3: 生成JWT令牌
            log.debug("步骤 3: 生成JWT令牌，用户ID: {}", user.getUserId());
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
            Long expiresIn = jwtUtil.getExpiration();
            
            // 步骤 4: 异步发送登录日志消息
            log.debug("步骤 4: 异步发送登录日志");
            sendLoginLogAsync(user);
            
            // 步骤 5: 构建并返回登录响应信息
            log.debug("步骤 5: 构建登录响应信息");
            UserLoginVO result = UserLoginVO.builder()
                    .token(token)
                    .expiresIn(expiresIn)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .build();
            
            log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getUserId(), user.getUsername());
            return result;
            
        } catch (AuthenticationException e) {
            // 认证失败，直接抛出
            throw e;
        } catch (Exception e) {
            log.error("用户登录系统异常，用户名: {}, 错误: {}", userLoginDTO.getUsername(), e.getMessage(), e);
            throw new RuntimeException("登录失败，请稍后重试", e);
        }
    }
    
    /**
     * 异步发送登录日志消息
     */
    private void sendLoginLogAsync(User user) {
        try {
            // 使用工具类获取客户端真实IP地址
            String clientIp = IpAddressUtil.getClientRealIp();
            
            // 获取分布式链路追踪ID
            String traceId = getTraceId();
            
            // 构建操作详情JSON
            String detail = buildLoginDetail(user);
            
            // 构建日志消息对象
            OperationLogMessage logMessage = OperationLogMessage.createMessage(
                user.getUserId(),
                traceId,
                "LOGIN",
                clientIp,
                detail
            );
            
            // 发送到RocketMQ
            rocketMQTemplate.convertAndSend("user-operation-log", logMessage);
            log.debug("用户登录日志消息发送成功，用户ID: {}, IP: {}, TraceID: {}", 
                     user.getUserId(), clientIp, traceId);
            
        } catch (Exception e) {
            // 日志发送失败不影响主业务流程
            log.warn("发送登录日志消息失败，用户ID: {}, 错误: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * 构建用户登录操作的详情JSON
     * 
     * @param user 用户实体
     * @return 格式化的JSON字符串
     */
    private String buildLoginDetail(User user) {
        StringBuilder detail = new StringBuilder();
        detail.append("{");
        detail.append("\"operation\":\"user_login\",");
        detail.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
        detail.append("\"user_id\":").append(user.getUserId()).append(",");
        detail.append("\"login_time\":\"").append(java.time.LocalDateTime.now()).append("\"");
        detail.append("}");
        
        return detail.toString();
    }
    
    /**
     * 将DTO转换为User实体
     * 
     * <p>注意：不需要手动设置userId、gmtCreate和gmtModified，
     * userId由ShardingSphere雪花算法自动生成，
     * 时间戳字段由JPA的@PrePersist回调自动设置。</p>
     */
    private User convertDtoToEntity(UserRegisterDTO dto, String encodedPassword) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        // userId、gmtCreate、gmtModified 由框架自动处理
        // - userId: ShardingSphere雪花算法生成
        // - gmtCreate/gmtModified: JPA @PrePersist 回调设置
        
        return user;
    }
    
    /**
     * 将User实体转换为VO
     */
    private UserRegisterVO convertEntityToVO(User user) {
        UserRegisterVO vo = new UserRegisterVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        return vo;
    }
    
    /**
     * 异步发送注册日志消息
     */
    private void sendRegistrationLogAsync(User user) {
        try {
            // 使用工具类获取客户端真实IP地址
            String clientIp = IpAddressUtil.getClientRealIp();
            
            // 获取分布式链路追踪ID
            String traceId = getTraceId();
            
            // 构建操作详情JSON
            String detail = buildRegistrationDetail(user);
            
            // 构建日志消息对象
            OperationLogMessage logMessage = OperationLogMessage.createMessage(
                user.getUserId(),
                traceId,
                "REGISTER",
                clientIp,
                detail
            );
            
            // 发送到RocketMQ
            rocketMQTemplate.convertAndSend("user-operation-log", logMessage);
            log.debug("用户注册日志消息发送成功，用户ID: {}, IP: {}, TraceID: {}", 
                     user.getUserId(), clientIp, traceId);
            
        } catch (Exception e) {
            // 日志发送失败不影响主业务流程
            log.warn("发送注册日志消息失败，用户ID: {}, 错误: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * 获取分布式链路追踪ID
     * 
     * <p>优先使用Seata的XID作为追踪ID，如果不可用则生成UUID。</p>
     * 
     * @return 链路追踪ID
     */
    private String getTraceId() {
        try {
            // 尝试获取Seata的全局事务ID
            String xid = RootContext.getXID();
            if (xid != null && !xid.trim().isEmpty()) {
                log.debug("使用Seata XID作为TraceID: {}", xid);
                return xid;
            }
        } catch (Exception e) {
            log.debug("获取Seata XID失败: {}", e.getMessage());
        }
        
        // 如果无法获取Seata XID，则生成UUID作为追踪ID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        log.debug("生成UUID作为TraceID: {}", uuid);
        return uuid;
    }
    
    /**
     * 构建用户注册操作的详情JSON
     * 
     * @param user 用户实体
     * @return 格式化的JSON字符串
     */
    private String buildRegistrationDetail(User user) {
        StringBuilder detail = new StringBuilder();
        detail.append("{");
        detail.append("\"operation\":\"user_registration\",");
        detail.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
        
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            detail.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\",");
        }
        
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            detail.append("\"phone\":\"").append(escapeJson(user.getPhone())).append("\",");
        }
        
        detail.append("\"registration_time\":\"").append(user.getGmtCreate()).append("\",");
        detail.append("\"user_id\":").append(user.getUserId());
        detail.append("}");
        
        return detail.toString();
    }
    
    /**
     * 获取用户信息实现
     * 根据用户ID查询用户详细信息，利用ShardingSphere分片键进行高效查询
     */
    @Override
    @Transactional(readOnly = true)
    public UserInfoVO getUserInfo(Long userId) {
        log.info("获取用户信息请求，用户ID: {}", userId);
        
        // 参数验证
        if (userId == null) {
            log.warn("获取用户信息失败：用户ID为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        try {
            // 使用分片键进行精确查询，ShardingSphere会直接路由到对应分片
            log.debug("根据用户ID查询用户信息，用户ID: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("用户不存在，用户ID: {}", userId);
                        return new UserNotFoundException(userId);
                    });
            
            // 转换为VO对象
            UserInfoVO result = convertUserToInfoVO(user);
            
            log.info("获取用户信息成功，用户ID: {}, 用户名: {}", userId, user.getUsername());
            return result;
            
        } catch (UserNotFoundException e) {
            // 用户不存在异常，直接抛出
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息系统异常，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("获取用户信息失败，请稍后重试", e);
        }
    }
    
    /**
     * 将User实体转换为UserInfoVO
     * 
     * @param user 用户实体
     * @return 用户信息VO
     */
    private UserInfoVO convertUserToInfoVO(User user) {
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gmtCreate(user.getGmtCreate())
                .gmtModified(user.getGmtModified())
                .build();
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     * 
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
