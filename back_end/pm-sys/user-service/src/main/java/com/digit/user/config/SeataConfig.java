package com.digit.user.config;

import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Seata 分布式事务配置类
 * 
 * <p>配置 Seata 在 ShardingSphere 分片环境下的事务处理。
 * 解决分片数据源与 Seata 数据源代理的兼容性问题。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Configuration
@EnableAutoDataSourceProxy
public class SeataConfig {

    /**
     * 禁用 Seata 自动数据源代理
     * 
     * <p>在 ShardingSphere 分片环境下，让 ShardingSphere 管理数据源，
     * Seata 通过其他方式参与事务协调。</p>
     */
    // 注意：实际的数据源代理配置由 ShardingSphere 和 application.yml 中的
    // seata.data-source-proxy-mode: AT 配置来处理
} 