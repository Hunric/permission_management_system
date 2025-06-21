package com.digit.logging.service.impl;

import com.digit.logging.dto.OperationLogMessage;
import com.digit.logging.entity.OperationLog;
import com.digit.logging.repository.OperationLogRepository;
import com.digit.logging.service.LoggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * 日志服务实现类
 * 
 * <p>实现日志处理的核心业务逻辑。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingServiceImpl implements LoggingService {
    
    private final OperationLogRepository operationLogRepository;
    
    /**
     * 处理操作日志消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOperationLog(OperationLogMessage logMessage) {
        log.debug("开始处理操作日志消息: {}", logMessage);
        
        try {
            // 验证消息有效性
            if (logMessage == null || !logMessage.isValid()) {
                log.warn("收到无效的日志消息: {}", logMessage);
                return;
            }
            
            // 转换为实体对象
            OperationLog operationLog = convertToEntity(logMessage);
            
            // 保存到数据库
            OperationLog savedLog = operationLogRepository.save(operationLog);
            
            log.debug("操作日志保存成功，日志ID: {}, 用户ID: {}, 操作: {}", 
                     savedLog.getLogId(), savedLog.getUserId(), savedLog.getAction());
            
        } catch (Exception e) {
            log.error("处理操作日志消息失败: {}, 错误: {}", logMessage, e.getMessage(), e);
            throw new RuntimeException("处理操作日志失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将消息对象转换为实体对象
     */
    private OperationLog convertToEntity(OperationLogMessage logMessage) {
        OperationLog operationLog = new OperationLog();
        
        operationLog.setUserId(logMessage.getUserId());
        operationLog.setTraceId(logMessage.getTraceId());
        operationLog.setAction(logMessage.getAction());
        operationLog.setIp(logMessage.getIp());
        operationLog.setDetail(logMessage.getDetail());
        
        // 设置时间
        if (logMessage.getGmtCreate() != null) {
            operationLog.setGmtCreate(logMessage.getGmtCreate());
        } else {
            operationLog.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        }
        
        return operationLog;
    }
}