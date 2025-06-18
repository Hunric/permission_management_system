package com.digit.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "permission-service") // 必须与 Nacos 中的服务名匹配
public interface PermissionFeignClient {

    @GetMapping("/api/permission/internal/users/{userId}/role")
    Map<String, Object> getUserRole(@PathVariable("userId") Long userId);
}
