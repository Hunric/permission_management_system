package com.digit.user.controller;

import com.digit.user.client.PermissionFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private PermissionFeignClient permissionFeignClient;

    @GetMapping("/test-rpc/{userId}")
    public Map<String, Object> testRpc(@PathVariable Long userId) {
        // 这将通过 Feign 和 Nacos 调用 permission-service
        return permissionFeignClient.getUserRole(userId);
    }
}
