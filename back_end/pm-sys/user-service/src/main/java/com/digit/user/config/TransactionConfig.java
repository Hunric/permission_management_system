package com.digit.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;

/**
 * 事务配置类
 * 
 * <p>配置 Seata 分布式事务与 JPA 本地事务的协同工作。
 * 确保在分布式事务场景下，本地数据库操作能够正确参与全局事务。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 配置 JPA 事务管理器
     * 
     * <p>虽然使用了 Seata 的全局事务管理，但仍需要本地事务管理器
     * 来处理单个数据源的事务操作。Seata 会在全局事务下协调本地事务。</p>
     * 
     * @param entityManagerFactory JPA 实体管理器工厂
     * @return JPA 事务管理器
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
} 