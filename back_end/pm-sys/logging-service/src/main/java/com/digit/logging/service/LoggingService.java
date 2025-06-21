package com.digit.logging.service;

import com.digit.logging.dto.OperationLogMessage;

/**
 * 日志服务接口
 * 
 * <p>定义日志处理相关的业务方法。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
public interface LoggingService {
    
    /**
     * 处理操作日志消息
     * 
     * <p>将从RocketMQ接收到的日志消息持久化到数据库。</p>
     * 
     * @param logMessage 操作日志消息
     * @throws RuntimeException 当处理失败时抛出异常
     */
    void processOperationLog(OperationLogMessage logMessage);
} 