package com.digit.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

/**
 * ShardingSphere 数据源配置
 * 
 * <p>使用ShardingSphere Driver方式配置分片数据源。
 * 该配置替代了原有的Spring Boot Starter自动配置方式。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-01-21
 */
@Configuration
public class ShardingSphereConfig {

    /**
     * 配置ShardingSphere数据源
     * 
     * <p>使用ShardingSphere Driver和classpath中的配置文件。
     * 配置文件位置：classpath:shardingsphere-config.yaml</p>
     * 
     * @return 配置好的ShardingSphere数据源
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            // 使用SimpleDriverDataSource配置ShardingSphere Driver
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriverClass(org.apache.shardingsphere.driver.ShardingSphereDriver.class);
            dataSource.setUrl("jdbc:shardingsphere:classpath:shardingsphere-config.yaml");
            
            return dataSource;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ShardingSphere DataSource", e);
        }
    }
} 