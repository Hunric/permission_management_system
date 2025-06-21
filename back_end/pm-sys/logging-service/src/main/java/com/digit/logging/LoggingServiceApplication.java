package com.digit.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Logging Service Application
 * 
 * @author System
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LoggingServiceApplication {

    public static void main(String[] args) {
        // 简单粗暴地忽略 RocketMQ 日志警告
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        SpringApplication.run(LoggingServiceApplication.class, args);
    }
} 