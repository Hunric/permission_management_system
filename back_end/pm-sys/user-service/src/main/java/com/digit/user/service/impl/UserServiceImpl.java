package com.digit.user.service.impl;

import com.digit.user.dto.UserLoginDTO;
import com.digit.user.dto.UserPageQueryDTO;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.dto.UserUpdateDTO;
import com.digit.user.entity.User;
import com.digit.user.service.UserService;
import com.digit.user.service.component.LoggingComponent;
import com.digit.user.service.component.UserAuthenticationComponent;
import com.digit.user.service.component.UserPermissionComponent;
import com.digit.user.service.component.UserQueryComponent;
import com.digit.user.service.component.UserRegistrationComponent;
import com.digit.user.service.component.UserUpdateComponent;
import com.digit.user.util.SecurityUtil;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import com.digit.user.vo.UserPageVO;
import com.digit.user.vo.UserRegisterVO;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务实现类
 * 
 * <p>采用组件化架构，将不同功能拆分到独立的组件中：</p>
 * <ul>
 *   <li>{@link UserRegistrationComponent} - 用户注册相关逻辑</li>
 *   <li>{@link UserAuthenticationComponent} - 用户认证相关逻辑</li>
 *   <li>{@link UserQueryComponent} - 用户查询相关逻辑</li>
 *   <li>{@link UserPermissionComponent} - 权限验证相关逻辑</li>
 *   <li>{@link LoggingComponent} - 日志记录相关逻辑</li>
 * </ul>
 * 
 * @author System
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    // 注入功能组件
    private final UserRegistrationComponent userRegistrationComponent;
    private final UserAuthenticationComponent userAuthenticationComponent;
    private final UserQueryComponent userQueryComponent;
    private final UserPermissionComponent userPermissionComponent;
    private final UserUpdateComponent userUpdateComponent;
    private final LoggingComponent loggingComponent;
    
    /**
     * 用户注册实现
     * 
     * <p>按照以下步骤执行：</p>
     * <ol>
     *   <li>检查用户名是否已存在</li>
     *   <li>对密码进行加密</li>
     *   <li>创建并保存用户实体</li>
     *   <li>为用户绑定默认角色</li>
     *   <li>异步发送操作日志消息</li>
     *   <li>返回注册结果</li>
     * </ol>
     */
    @Override
    @GlobalTransactional(name = "user-register-tx", rollbackFor = Exception.class)
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {
        log.info("开始注册用户: {}", userRegisterDTO.getUsername());
        
        try {
            // 步骤 1: 检查用户名是否已存在
            log.debug("步骤 1: 检查用户名是否已存在");
            userRegistrationComponent.validateUsernameUniqueness(userRegisterDTO.getUsername());
            
            // 步骤 2: 对密码进行加密
            log.debug("步骤 2: 对密码进行BCrypt加密");
            String encodedPassword = userRegistrationComponent.encodePassword(userRegisterDTO.getPassword());
            
            // 步骤 3: 创建并保存用户实体
            log.debug("步骤 3: 创建并保存用户实体");
            User savedUser = userRegistrationComponent.createAndSaveUser(userRegisterDTO, encodedPassword);
            
            // 步骤 4: 为用户绑定默认角色
            log.debug("步骤 4: 通过RPC调用permission-service绑定默认角色");
            userRegistrationComponent.bindDefaultRole(savedUser.getUserId());
            
            // 步骤 5: 异步发送操作日志消息
            log.debug("步骤 5: 异步发送用户注册操作日志");
            loggingComponent.sendRegistrationLogAsync(savedUser);
            
            // 步骤 6: 返回注册结果
            log.debug("步骤 6: 转换结果为VO对象");
            UserRegisterVO result = userRegistrationComponent.convertEntityToVO(savedUser);
            
            log.info("用户 {} 注册成功，用户ID: {}", savedUser.getUsername(), savedUser.getUserId());
            return result;
            
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}, 错误: {}", userRegisterDTO.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 用户登录实现
     * 
     * <p>按照以下步骤执行：</p>
     * <ol>
     *   <li>根据用户名查询用户信息</li>
     *   <li>验证密码是否正确</li>
     *   <li>生成JWT令牌</li>
     *   <li>异步发送登录日志消息</li>
     *   <li>返回登录响应信息</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        log.info("用户登录请求: {}", userLoginDTO.getUsername());
        
        try {
            // 步骤 1: 根据用户名查询用户信息
            log.debug("步骤 1: 查询用户信息，用户名: {}", userLoginDTO.getUsername());
            User user = userAuthenticationComponent.findUserByUsername(userLoginDTO.getUsername());
            
            // 步骤 2: 验证密码是否正确
            log.debug("步骤 2: 验证密码，用户ID: {}", user.getUserId());
            userAuthenticationComponent.validatePassword(user, userLoginDTO.getPassword());
            
            // 步骤 3: 生成JWT令牌
            log.debug("步骤 3: 生成JWT令牌，用户ID: {}", user.getUserId());
            UserLoginVO result = userAuthenticationComponent.generateLoginResponse(user);
            
            // 步骤 4: 异步发送登录日志消息
            log.debug("步骤 4: 异步发送登录日志");
            loggingComponent.sendLoginLogAsync(user);
            
            log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getUserId(), user.getUsername());
            return result;
            
        } catch (Exception e) {
            log.error("用户登录失败，用户名: {}, 错误: {}", userLoginDTO.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取当前用户信息实现
     * 
     * @param userId 用户ID
     * @return 用户信息VO
     */
    @Override
    @Transactional(readOnly = true)
    public UserInfoVO getUserInfo(Long userId) {
        log.info("获取用户信息请求，用户ID: {}", userId);
        
        try {
            // 查询用户信息
            User user = userAuthenticationComponent.findUserById(userId);
            
            // 转换为VO并返回
            UserInfoVO result = userAuthenticationComponent.convertUserToInfoVO(user);
            
            log.debug("用户信息查询成功，用户ID: {}", userId);
            return result;
            
        } catch (Exception e) {
            log.error("获取用户信息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 分页查询用户列表实现
     * 
     * <p>按照以下步骤执行：</p>
     * <ol>
     *   <li>验证管理员权限并获取排除用户列表</li>
     *   <li>验证查询参数</li>
     *   <li>构建分页对象</li>
     *   <li>执行分页查询（带角色过滤）</li>
     *   <li>转换并返回结果</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public UserPageVO getUsers(UserPageQueryDTO queryDTO) {
        log.info("分页查询用户列表请求，页码: {}, 每页大小: {}", queryDTO.getPage(), queryDTO.getSize());
        
        try {
            // 步骤 1: 验证管理员权限并获取排除用户列表
            log.debug("步骤 1: 验证管理员权限并获取排除用户列表");
            List<Long> excludeUserIds = userPermissionComponent.getExcludedUserIds();
            
            // 步骤 2: 验证查询参数
            log.debug("步骤 2: 验证查询参数");
            userQueryComponent.validateQueryParameters(queryDTO);
            
            // 步骤 3: 构建分页对象
            log.debug("步骤 3: 构建分页对象");
            Pageable pageable = userQueryComponent.buildPageable(queryDTO);
            
            // 步骤 4: 执行分页查询（带角色过滤）
            log.debug("步骤 4: 执行分页查询（带角色过滤），排除用户数: {}", excludeUserIds.size());
            Page<User> userPage = userQueryComponent.executePageQueryWithRoleFilter(queryDTO, pageable, excludeUserIds);
            
            // 步骤 5: 转换并返回结果
            log.debug("步骤 5: 转换查询结果为VO");
            UserPageVO result = userQueryComponent.convertToPageVO(userPage);
            
            log.info("分页查询用户列表成功，总记录数: {}, 当前页: {}, 排除用户数: {}", 
                    result.getTotalElements(), result.getCurrentPage(), excludeUserIds.size());
            return result;
            
        } catch (SecurityException e) {
            log.warn("分页查询用户列表权限不足: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("分页查询用户列表失败，错误: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 查询指定用户信息实现
     * 
     * <p>按照以下步骤执行：</p>
     * <ol>
     *   <li>获取当前操作者用户ID</li>
     *   <li>验证查看权限（本人或管理员）</li>
     *   <li>查询用户信息</li>
     *   <li>转换并返回结果</li>
     * </ol>
     * 
     * @param userId 要查询的用户ID
     * @return 用户信息VO
     */
    @Override
    @Transactional(readOnly = true)
    public UserInfoVO getUserById(Long userId) {
        log.info("查询指定用户信息请求，用户ID: {}", userId);
        
        try {
            // 步骤 1: 获取当前操作者用户ID
            log.debug("步骤 1: 获取当前操作者用户ID");
            Long currentUserId = SecurityUtil.getCurrentUserId();
            
            // 步骤 2: 验证查看权限（本人或管理员）
            log.debug("步骤 2: 验证查看权限，当前用户ID: {}, 目标用户ID: {}", currentUserId, userId);
            if (!currentUserId.equals(userId)) {
                // 不是本人操作，需要验证管理员权限
                userPermissionComponent.verifyAdminOrSuperAdminPermission(currentUserId);
            }
            
            // 步骤 3: 查询用户信息
            log.debug("步骤 3: 查询用户信息");
            User user = userUpdateComponent.findUserById(userId);
            
            // 步骤 4: 转换并返回结果
            log.debug("步骤 4: 转换用户信息为VO");
            UserInfoVO result = userUpdateComponent.convertUserToInfoVO(user);
            
            log.info("查询指定用户信息成功，用户ID: {}, 用户名: {}", userId, user.getUsername());
            return result;
            
        } catch (SecurityException e) {
            log.warn("查询指定用户信息权限不足，用户ID: {}, 错误: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("查询指定用户信息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新指定用户信息实现
     * 
     * <p>按照以下步骤执行：</p>
     * <ol>
     *   <li>获取当前操作者用户ID</li>
     *   <li>验证更新权限（本人或管理员）</li>
     *   <li>验证更新数据有效性</li>
     *   <li>查询目标用户信息</li>
     *   <li>执行更新操作</li>
     *   <li>异步发送更新日志</li>
     *   <li>转换并返回结果</li>
     * </ol>
     * 
     * @param userId 要更新的用户ID
     * @param updateDTO 更新数据DTO
     * @return 更新后的用户信息VO
     */
    @Override
    @GlobalTransactional(name = "user-update-tx", rollbackFor = Exception.class)
    public UserInfoVO updateUserById(Long userId, UserUpdateDTO updateDTO) {
        log.info("更新指定用户信息请求，用户ID: {}, 更新字段: {}", userId, updateDTO.getUpdateFields());
        
        try {
            // 步骤 1: 获取当前操作者用户ID
            log.debug("步骤 1: 获取当前操作者用户ID");
            Long currentUserId = SecurityUtil.getCurrentUserId();
            
            // 步骤 2: 验证更新权限（本人或管理员）
            log.debug("步骤 2: 验证更新权限，当前用户ID: {}, 目标用户ID: {}", currentUserId, userId);
            if (!currentUserId.equals(userId)) {
                // 不是本人操作，需要验证管理员权限
                userPermissionComponent.verifyAdminOrSuperAdminPermission(currentUserId);
            }
            
            // 步骤 3: 验证更新数据有效性
            log.debug("步骤 3: 验证更新数据有效性");
            userUpdateComponent.validateUpdateData(updateDTO);
            
            // 步骤 4: 查询目标用户信息
            log.debug("步骤 4: 查询目标用户信息");
            User targetUser = userUpdateComponent.findUserById(userId);
            
            // 步骤 5: 执行更新操作
            log.debug("步骤 5: 执行更新操作");
            String changeDetails = userUpdateComponent.generateChangeDetails(targetUser, updateDTO);
            User updatedUser = userUpdateComponent.updateUserInfo(targetUser, updateDTO);
            
            // 步骤 6: 异步发送更新日志
            log.debug("步骤 6: 异步发送更新日志");
            loggingComponent.sendUpdateUserLogAsync(updatedUser, currentUserId, changeDetails);
            
            // 步骤 7: 转换并返回结果
            log.debug("步骤 7: 转换更新结果为VO");
            UserInfoVO result = userUpdateComponent.convertUserToInfoVO(updatedUser);
            
            log.info("更新指定用户信息成功，用户ID: {}, 操作者ID: {}, 变更详情: {}", 
                    userId, currentUserId, changeDetails);
            return result;
            
        } catch (SecurityException e) {
            log.warn("更新指定用户信息权限不足，用户ID: {}, 操作者ID: {}, 错误: {}", 
                    userId, SecurityUtil.getCurrentUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新指定用户信息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
} 