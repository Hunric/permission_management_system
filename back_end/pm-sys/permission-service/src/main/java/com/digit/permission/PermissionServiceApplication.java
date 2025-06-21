package com.digit.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Permission Service Application
 * 
 * @author System
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PermissionServiceApplication {

    /**
     * 权限服务启动入口方法
     * 
     * <p>启动权限服务应用程序，初始化权限管理相关的所有组件。
     * 应用启动后将自动注册到Nacos服务注册中心，并准备接受权限相关的服务请求。</p>
     * 
     * @param args 命令行参数，支持以下常用启动参数：
     *             <ul>
     *               <li>--server.port=8082 指定权限服务端口</li>
     *               <li>--spring.profiles.active=dev 指定激活的配置环境</li>
     *               <li>--spring.cloud.nacos.discovery.server-addr=localhost:8848 Nacos地址</li>
     *               <li>--spring.datasource.url=jdbc:mysql://localhost:3306/permission_db 数据库连接</li>
     *             </ul>
     * @throws Exception 如果应用启动过程中发生异常
     * 
     * @see SpringApplication#run(Class, String...) Spring Boot启动方法
     */
    public static void main(String[] args) {
        // 简单粗暴地忽略 RocketMQ 日志警告
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        SpringApplication.run(PermissionServiceApplication.class, args);
    }
}