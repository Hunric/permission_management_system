package com.digit.user.controller;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.UserLoginDTO;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.service.UserService;
import com.digit.user.util.SecurityUtil;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserLoginVO;
import com.digit.user.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
