package com.digit.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 * 
 * <p>用户服务是权限管理系统的核心模块之一，负责用户的注册、登录、个人信息管理等功能。
 * 该服务采用微服务架构，集成了以下核心技术：</p>
 * 
 * <ul>
 *   <li>Spring Boot 2.7.12 - 提供基础的Web框架和自动配置</li>
 *   <li>Spring Cloud Alibaba - 微服务治理框架</li>
 *   <li>Nacos Discovery - 服务注册与发现</li>
 *   <li>OpenFeign - 声明式HTTP客户端，用于服务间调用</li>
 *   <li>Spring Data JPA - 数据持久层框架</li>
 *   <li>ShardingSphere-JDBC - 数据库分片中间件</li>
 *   <li>RocketMQ - 消息队列，用于异步消息传递</li>
 *   <li>SpringDoc OpenAPI 3 - API文档生成</li>
 * </ul>
 * 
 * <p><strong>主要功能模块：</strong></p>
 * <ul>
 *   <li>用户注册 - 支持用户名、邮箱注册</li>
 *   <li>用户登录 - 基于用户名/邮箱的认证</li>
 *   <li>个人信息管理 - 用户资料的增删改查</li>
 *   <li>密码管理 - 密码修改、重置功能</li>
 * </ul>
 * 
 * <p><strong>API文档访问地址：</strong></p>
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8081/swagger-ui/index.html">http://localhost:8081/swagger-ui/index.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8081/v3/api-docs">http://localhost:8081/v3/api-docs</a></li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-09
 * @see com.digit.user.controller.UserController 用户控制器
 * @see com.digit.user.service.UserService 用户服务接口
 * @see com.digit.user.service.impl.UserServiceImpl 用户服务实现
 */
@SpringBootApplication(scanBasePackages = "com.digit.user")
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {
    
    /**
     * 用户服务启动入口方法
     * 
     * <p>启动Spring Boot应用程序，初始化所有必要的组件和配置。
     * 应用启动后将自动注册到Nacos服务注册中心。</p>
     * 
     * @param args 命令行参数，支持Spring Boot的标准启动参数
     *             <ul>
     *               <li>--server.port=8081 指定服务端口</li>
     *               <li>--spring.profiles.active=dev 指定激活的配置文件</li>
     *               <li>--spring.cloud.nacos.discovery.server-addr=localhost:8848 指定Nacos地址</li>
     *             </ul>
     * @throws Exception 如果应用启动过程中发生异常
     * 
     * @see SpringApplication#run(Class, String...) Spring Boot启动方法
     */
    public static void main(String[] args) {
        // 简单粗暴地忽略 RocketMQ 日志警告
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        SpringApplication.run(UserServiceApplication.class, args);
    }
}