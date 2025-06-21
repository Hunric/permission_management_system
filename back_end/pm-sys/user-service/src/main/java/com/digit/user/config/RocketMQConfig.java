package com.digit.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RocketMQ 配置类
 * 
 * <p>配置 RocketMQ 消息序列化，特别是 Java 8 时间类型的处理。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Configuration
public class RocketMQConfig {

    /**
     * 配置 ObjectMapper 以支持 Java 8 时间类型
     * 
     * <p>这个 ObjectMapper 会被 Spring 自动注入到 RocketMQ 的消息转换器中</p>
     * 
     * @return 配置好的 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册 Java 8 时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略空值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return mapper;
    }
} 