package com.digit.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ShardingSphere 数据源配置类
 * 
 * <p>确保 ShardingSphere 与 JPA/Hibernate 正确集成。
 * ShardingSphere 会自动配置数据源，我们只需要确保配置的正确性。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Configuration
public class ShardingSphereDataSourceConfig {
    
    // ShardingSphere 会通过 spring.shardingsphere 配置自动创建数据源
    // 这里我们只需要确保配置文件正确即可
    
    /**
     * 配置说明：
     * 
     * 1. ShardingSphere 会读取 application.yml 中的 spring.shardingsphere 配置
     * 2. 自动创建分片数据源并注册为 Spring Bean
     * 3. Hibernate 会使用这个数据源，ID 生成由 ShardingSphere 的雪花算法处理
     * 4. 分片路由根据 user_id 字段自动进行：
     *    - 数据库分片：db${user_id % 2} (db0, db1)
     *    - 表分片：users_${user_id % 2} (users_0, users_1)
     * 5. 使用 @GeneratedValue(strategy = GenerationType.IDENTITY) 让 ShardingSphere 接管 ID 生成
     */
} 