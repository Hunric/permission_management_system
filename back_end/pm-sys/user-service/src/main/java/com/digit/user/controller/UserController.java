package com.digit.user.controller;

import com.digit.user.dto.ApiResponse;
import com.digit.user.dto.UserRegisterDTO;
import com.digit.user.service.UserService;
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
        
        try {
            UserRegisterVO result = userService.register(userRegisterDTO);
            ApiResponse<UserRegisterVO> response = ApiResponse.created("User registration successful", result);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            ApiResponse<UserRegisterVO> response = ApiResponse.conflict(e.getMessage());
            return ResponseEntity.status(409).body(response);
        } catch (Exception e) {
            log.error("Registration failed due to system error", e);
            ApiResponse<UserRegisterVO> response = ApiResponse.internalServerError("Registration failed, please try again later");
            return ResponseEntity.status(500).body(response);
        }
    }
}
