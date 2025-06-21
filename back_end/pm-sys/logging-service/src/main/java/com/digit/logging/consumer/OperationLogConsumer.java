package com.digit.logging.consumer;

import com.digit.logging.dto.OperationLogMessage;
import com.digit.logging.service.LoggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 操作日志消息消费者
 * 
 * <p>监听RocketMQ中的用户操作日志消息，并进行异步处理。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "user-operation-log",
    consumerGroup = "logging-service-consumer-group"
)
public class OperationLogConsumer implements RocketMQListener<OperationLogMessage> {
    
    private final LoggingService loggingService;
    
    @Override
    public void onMessage(OperationLogMessage message) {
        log.info("收到操作日志消息: 用户ID={}, 操作={}, 追踪ID={}", 
                message.getUserId(), message.getAction(), message.getTraceId());
        
        try {
            // 处理日志消息
            loggingService.processOperationLog(message);
            
            log.debug("操作日志消息处理完成: 用户ID={}, 操作={}", 
                     message.getUserId(), message.getAction());
            
        } catch (Exception e) {
            log.error("处理操作日志消息失败: {}, 错误: {}", message, e.getMessage(), e);
            // 注意：这里不重新抛出异常，避免影响消息队列的正常运行
            // 在生产环境中，可以考虑将失败的消息发送到死信队列
        }
    }
} 