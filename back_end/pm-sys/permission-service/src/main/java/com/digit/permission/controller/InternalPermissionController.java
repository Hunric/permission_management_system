package com.digit.permission.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/permission/internal")
public class InternalPermissionController {
    // @Autowired 服务于此

    @GetMapping("/users/{userId}/role")
    public Map<String, Object> getUserRole(@PathVariable Long userId) {
        // TODO: 实现查询数据库并找到角色代码的逻辑
        // 现在，我们返回一个虚拟响应
        Map<String, String> roleData = new HashMap<>();
        roleData.put("roleCode", "user"); // 虚拟角色

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", roleData);
        return response;
    }
}
