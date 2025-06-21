package com.digit.user.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * ShardingSphere 与 Seata 集成配置
 * 
 * <p>确保 ShardingSphere 和 Seata 在分片环境下正确协作。
 * 避免数据源代理冲突，让 ShardingSphere 管理数据源，
 * Seata 通过事务协调器参与分布式事务。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Configuration
@AutoConfigureAfter(name = "org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration")
public class ShardingSphereSeataIntegrationConfig {
    
    /**
     * 配置说明：
     * 
     * 1. ShardingSphere 自动配置创建分片数据源
     * 2. Seata 通过 enable-auto-data-source-proxy: false 禁用自动代理
     * 3. 分布式事务通过 @GlobalTransactional 注解启用
     * 4. Seata 在 RM 层面参与事务协调，不直接代理 ShardingSphere 数据源
     * 5. 事务回滚由 Seata 协调各个分片的本地事务完成
     */
} 