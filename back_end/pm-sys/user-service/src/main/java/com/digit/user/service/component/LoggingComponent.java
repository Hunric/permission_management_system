package com.digit.user.service.component;

import com.digit.user.dto.OperationLogMessage;
import com.digit.user.entity.User;
import com.digit.user.util.IpAddressUtil;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * 日志记录组件
 * 
 * <p>负责处理操作日志相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>用户注册日志记录</li>
 *   <li>用户登录日志记录</li>
 *   <li>操作日志消息构建</li>
 *   <li>异步消息发送</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingComponent {
    
    private final RocketMQTemplate rocketMQTemplate;
    
    // RocketMQ Topic
    private static final String OPERATION_LOG_TOPIC = "user-operation-log";
    
    /**
     * 异步发送用户注册日志
     * 
     * @param user 用户实体
     */
    public void sendRegistrationLogAsync(User user) {
        try {
            String traceId = getTraceId();
            String clientIp = IpAddressUtil.getClientRealIp();
            String detail = buildRegistrationDetail(user);
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(user.getUserId())
                    .traceId(traceId)
                    .action("REGISTER")
                    .ip(clientIp)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.asyncSend(OPERATION_LOG_TOPIC, logMessage, new LogSendCallback("注册"));
            log.debug("用户注册日志消息已发送到MQ，用户ID: {}", user.getUserId());
            
        } catch (Exception e) {
            log.error("发送用户注册日志失败，用户ID: {}, 错误: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * 异步发送用户登录日志
     * 
     * @param user 用户实体
     */
    public void sendLoginLogAsync(User user) {
        try {
            String traceId = getTraceId();
            String clientIp = IpAddressUtil.getClientRealIp();
            String detail = buildLoginDetail(user);
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(user.getUserId())
                    .traceId(traceId)
                    .action("LOGIN")
                    .ip(clientIp)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.asyncSend(OPERATION_LOG_TOPIC, logMessage, new LogSendCallback("登录"));
            log.debug("用户登录日志消息已发送到MQ，用户ID: {}", user.getUserId());
            
        } catch (Exception e) {
            log.error("发送用户登录日志失败，用户ID: {}, 错误: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * 异步发送用户信息更新日志
     * 
     * @param user 更新后的用户实体
     * @param operatorId 操作者用户ID
     * @param changeDetails 变更详情JSON字符串
     */
    public void sendUpdateUserLogAsync(User user, Long operatorId, String changeDetails) {
        try {
            String traceId = getTraceId();
            String clientIp = IpAddressUtil.getClientRealIp();
            String detail = buildUpdateUserDetail(user, operatorId, changeDetails);
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(operatorId)  // 使用操作者ID作为日志主体
                    .traceId(traceId)
                    .action("UPDATE_USER_INFO")
                    .ip(clientIp)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.asyncSend(OPERATION_LOG_TOPIC, logMessage, new LogSendCallback("用户信息更新"));
            log.debug("用户信息更新日志消息已发送到MQ，目标用户ID: {}, 操作者ID: {}", user.getUserId(), operatorId);
            
        } catch (Exception e) {
            log.error("发送用户信息更新日志失败，目标用户ID: {}, 操作者ID: {}, 错误: {}", 
                    user.getUserId(), operatorId, e.getMessage());
        }
    }
    
    /**
     * 异步发送用户密码修改日志
     * 
     * @param user 用户实体
     * @param changeDetails 变更详情JSON字符串
     */
    public void sendChangePasswordLogAsync(User user, String changeDetails) {
        try {
            String traceId = getTraceId();
            String clientIp = IpAddressUtil.getClientRealIp();
            String detail = buildPasswordChangeDetail(user, "CHANGE_PASSWORD", changeDetails);
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(user.getUserId())
                    .traceId(traceId)
                    .action("CHANGE_PASSWORD")
                    .ip(clientIp)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.asyncSend(OPERATION_LOG_TOPIC, logMessage, new LogSendCallback("密码修改"));
            log.debug("密码修改日志消息已发送到MQ，用户ID: {}", user.getUserId());
            
        } catch (Exception e) {
            log.error("发送密码修改日志失败，用户ID: {}, 错误: {}", user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * 异步发送用户密码重置日志
     * 
     * @param user 目标用户实体
     * @param operatorId 操作者用户ID
     * @param changeDetails 变更详情JSON字符串
     */
    public void sendResetPasswordLogAsync(User user, Long operatorId, String changeDetails) {
        try {
            String traceId = getTraceId();
            String clientIp = IpAddressUtil.getClientRealIp();
            String detail = buildPasswordResetDetail(user, operatorId, changeDetails);
            
            OperationLogMessage logMessage = OperationLogMessage.builder()
                    .userId(operatorId)  // 使用操作者ID作为日志主体
                    .traceId(traceId)
                    .action("RESET_PASSWORD")
                    .ip(clientIp)
                    .detail(detail)
                    .gmtCreate(new Timestamp(System.currentTimeMillis()))
                    .build();
            
            rocketMQTemplate.asyncSend(OPERATION_LOG_TOPIC, logMessage, new LogSendCallback("密码重置"));
            log.debug("密码重置日志消息已发送到MQ，目标用户ID: {}, 操作者ID: {}", user.getUserId(), operatorId);
            
        } catch (Exception e) {
            log.error("发送密码重置日志失败，目标用户ID: {}, 操作者ID: {}, 错误: {}", 
                    user.getUserId(), operatorId, e.getMessage());
        }
    }
    
    /**
     * 获取链路追踪ID
     * 
     * @return 链路追踪ID
     */
    private String getTraceId() {
        try {
            String xid = RootContext.getXID();
            if (xid != null && !xid.isEmpty()) {
                return xid;
            }
        } catch (Exception e) {
            log.debug("获取Seata XID失败: {}", e.getMessage());
        }
        
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 构建用户注册详情JSON
     * 
     * @param user 用户实体
     * @return JSON格式的详情字符串
     */
    private String buildRegistrationDetail(User user) {
        return String.format("{\"action\":\"user_register\",\"username\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"userId\":%d}",
                escapeJson(user.getUsername()),
                escapeJson(user.getEmail() != null ? user.getEmail() : ""),
                escapeJson(user.getPhone() != null ? user.getPhone() : ""),
                user.getUserId());
    }
    
    /**
     * 构建用户登录详情JSON
     * 
     * @param user 用户实体
     * @return JSON格式的详情字符串
     */
    private String buildLoginDetail(User user) {
        return String.format("{\"action\":\"user_login\",\"username\":\"%s\",\"userId\":%d}",
                escapeJson(user.getUsername()),
                user.getUserId());
    }
    
    /**
     * 构建用户信息更新详情JSON
     * 
     * @param user 更新后的用户实体
     * @param operatorId 操作者用户ID
     * @param changeDetails 变更详情JSON字符串
     * @return JSON格式的详情字符串
     */
    private String buildUpdateUserDetail(User user, Long operatorId, String changeDetails) {
        return String.format("{\"action\":\"update_user_info\",\"userId\":%d,\"operatorId\":%d,\"changeDetails\":%s}",
                user.getUserId(),
                operatorId,
                changeDetails);
    }
    
    /**
     * 构建密码修改详情JSON
     * 
     * @param user 用户实体
     * @param action 操作类型
     * @param changeDetails 变更详情JSON字符串
     * @return JSON格式的详情字符串
     */
    private String buildPasswordChangeDetail(User user, String action, String changeDetails) {
        return String.format("{\"action\":\"%s\",\"userId\":%d,\"username\":\"%s\",\"details\":%s}",
                action,
                user.getUserId(),
                escapeJson(user.getUsername()),
                changeDetails);
    }
    
    /**
     * 构建密码重置详情JSON
     * 
     * @param user 目标用户实体
     * @param operatorId 操作者用户ID
     * @param changeDetails 变更详情JSON字符串
     * @return JSON格式的详情字符串
     */
    private String buildPasswordResetDetail(User user, Long operatorId, String changeDetails) {
        return String.format("{\"action\":\"reset_password\",\"targetUserId\":%d,\"targetUsername\":\"%s\",\"operatorId\":%d,\"details\":%s}",
                user.getUserId(),
                escapeJson(user.getUsername()),
                operatorId,
                changeDetails);
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     * 
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * 日志发送回调类
     */
    private static class LogSendCallback implements org.apache.rocketmq.client.producer.SendCallback {
        private final String operation;
        
        public LogSendCallback(String operation) {
            this.operation = operation;
        }
        
        @Override
        public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
            log.debug("{}日志消息发送成功: {}", operation, sendResult.getMsgId());
        }
        
        @Override
        public void onException(Throwable e) {
            log.error("{}日志消息发送失败: {}", operation, e.getMessage());
        }
    }
} 