package com.digit.user.controller;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.UserLoginDTO;
import com.digit.user.dto.UserPageQueryDTO;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.dto.UserUpdateDTO;
import com.digit.user.dto.ChangePasswordDTO;
import com.digit.user.dto.ResetPasswordResponse;
import com.digit.user.service.UserService;
import com.digit.user.util.SecurityUtil;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import com.digit.user.vo.UserPageVO;
import com.digit.user.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

// OpenAPI annotations removed for Java 8 compatibility

/**
 * User Management Controller
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    /**
     * 用户服务依赖注入
     * 
     * <p>通过 {@code @RequiredArgsConstructor} 注解实现构造器注入，
     * 确保依赖的不可变性和线程安全性。</p>
     */
    private final UserService userService;
    
    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterVO>> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("User registration request for username: {}", userRegisterDTO.getUsername());
        
        UserRegisterVO result = userService.register(userRegisterDTO);
        ApiResponse<UserRegisterVO> response = ApiResponse.created("用户注册成功", result);
        return ResponseEntity.status(201).body(response);
    }
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginVO>> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("User login request for username: {}", userLoginDTO.getUsername());
        
        UserLoginVO result = userService.login(userLoginDTO);
        ApiResponse<UserLoginVO> response = ApiResponse.success("登录成功", result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user info endpoint
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoVO>> getCurrentUserInfo() {
        // 从Security上下文中获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        if (userId == null) {
            log.warn("获取当前用户信息失败：用户未认证");
            ApiResponse<UserInfoVO> response = ApiResponse.unauthorized("用户未认证");
            return ResponseEntity.status(401).body(response);
        }
        
        log.info("获取当前用户信息请求，用户ID: {}", userId);
        
        UserInfoVO result = userService.getUserInfo(userId);
        ApiResponse<UserInfoVO> response = ApiResponse.success("获取用户信息成功", result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get users list with pagination endpoint (Admin/Super Admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserPageVO>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gmtCreateStart,
            @RequestParam(required = false) String gmtCreateEnd) {
        
        log.info("分页查询用户列表请求，页码: {}, 每页大小: {}", page, size);
        
        // 构建查询参数对象
        UserPageQueryDTO queryDTO = UserPageQueryDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .username(username)
                .email(email)
                .phone(phone)
                .gmtCreateStart(gmtCreateStart)
                .gmtCreateEnd(gmtCreateEnd)
                .build();
        
        UserPageVO result = userService.getUsers(queryDTO);
        ApiResponse<UserPageVO> response = ApiResponse.success("查询用户列表成功", result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get specific user info endpoint (Self or Admin/Super Admin only)
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoVO>> getUserById(@PathVariable Long userId) {
        log.info("查询指定用户信息请求，用户ID: {}", userId);
        
        try {
            UserInfoVO result = userService.getUserById(userId);
            ApiResponse<UserInfoVO> response = ApiResponse.success("查询用户信息成功", result);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("查询用户信息权限不足，用户ID: {}, 错误: {}", userId, e.getMessage());
            ApiResponse<UserInfoVO> response = ApiResponse.forbidden("权限不足");
            return ResponseEntity.status(403).body(response);
            
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("用户不存在")) {
                log.warn("查询用户信息失败，用户不存在，用户ID: {}", userId);
                ApiResponse<UserInfoVO> response = ApiResponse.notFound("用户不存在");
                return ResponseEntity.status(404).body(response);
            }
            throw e;
        }
    }
    
    /**
     * Update specific user info endpoint (Self or Admin/Super Admin only)
     * 
     * @param userId 用户ID
     * @param updateDTO 更新数据
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoVO>> updateUserById(
            @PathVariable Long userId, 
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        log.info("更新指定用户信息请求，用户ID: {}, 更新字段: {}", userId, updateDTO.getUpdateFields());
        
        try {
            UserInfoVO result = userService.updateUserById(userId, updateDTO);
            ApiResponse<UserInfoVO> response = ApiResponse.success("更新用户信息成功", result);
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            log.warn("更新用户信息权限不足，用户ID: {}, 错误: {}", userId, e.getMessage());
            ApiResponse<UserInfoVO> response = ApiResponse.forbidden("权限不足");
            return ResponseEntity.status(403).body(response);
            
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("用户不存在")) {
                log.warn("更新用户信息失败，用户不存在，用户ID: {}", userId);
                ApiResponse<UserInfoVO> response = ApiResponse.notFound("用户不存在");
                return ResponseEntity.status(404).body(response);
            }
            throw e;
        }
    }
    
    /**
     * 修改当前用户密码端点
     * 
     * <p>用户可以通过提供旧密码和新密码来修改自己的密码。</p>
     * 
     * @param changePasswordDTO 密码修改请求数据
     * @return API响应
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("收到修改密码请求");
        
        try {
            userService.changePassword(changePasswordDTO);
            
            log.info("修改密码成功");
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
            
        } catch (RuntimeException e) {
            log.warn("修改密码失败: {}", e.getMessage());
            
            if (e.getMessage().contains("旧密码不正确")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("400", "旧密码不正确"));
            } else if (e.getMessage().contains("用户不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("404", "用户不存在"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("400", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("修改密码失败，错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "修改密码失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置指定用户密码端点
     * 
     * <p>管理员或超级管理员可以重置其他用户的密码为默认密码。</p>
     * 
     * @param userId 要重置密码的用户ID
     * @return API响应，包含新的临时密码
     */
    @PostMapping("/user/{userId}/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(@PathVariable Long userId) {
        log.info("收到重置密码请求，目标用户ID: {}", userId);
        
        try {
            ResetPasswordResponse response = userService.resetPassword(userId);
            
            log.info("重置密码成功，目标用户ID: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", response));
            
        } catch (SecurityException e) {
            log.warn("重置密码权限不足，目标用户ID: {}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("403", "权限不足"));
        } catch (RuntimeException e) {
            log.warn("重置密码失败，目标用户ID: {}, 错误: {}", userId, e.getMessage());
            
            if (e.getMessage().contains("用户不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("404", "用户不存在"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("400", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("重置密码失败，目标用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "重置密码失败: " + e.getMessage()));
        }
    }
}
