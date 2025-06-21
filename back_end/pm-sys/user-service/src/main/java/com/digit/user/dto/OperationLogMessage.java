package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.sql.Timestamp;

/**
 * 操作日志消息对象
 * 
 * <p>该类用于封装用户操作日志信息，通过RocketMQ异步发送到日志服务进行持久化。
 * 遵循异步消息处理模式，确保主业务流程不受日志记录影响。</p>
 * 
 * <p><strong>设计原则：</strong></p>
 * <ul>
 *   <li><strong>异步处理：</strong>日志记录不影响主业务流程性能</li>
 *   <li><strong>结构化存储：</strong>支持JSON格式的详细信息记录</li>
 *   <li><strong>可追溯性：</strong>包含完整的操作上下文信息</li>
 *   <li><strong>序列化友好：</strong>实现Serializable接口，支持消息队列传输</li>
 * </ul>
 * 
 * <p><strong>核心业务场景：</strong></p>
 * <ul>
 *   <li><strong>用户注册：</strong>记录新用户创建事件</li>
 *   <li><strong>用户登录/登出：</strong>记录用户会话活动</li>
 *   <li><strong>信息修改：</strong>记录用户信息变更历史</li>
 *   <li><strong>密码操作：</strong>记录密码修改和重置事件</li>
 *   <li><strong>权限变更：</strong>记录角色升级和降级操作</li>
 * </ul>
 * 
 * <p><strong>技术特性：</strong></p>
 * <ul>
 *   <li><strong>RocketMQ集成：</strong>作为消息体在MQ中传输</li>
 *   <li><strong>JSON序列化：</strong>使用Jackson进行序列化配置</li>
 *   <li><strong>数据验证：</strong>使用Bean Validation进行字段验证</li>
 *   <li><strong>链路追踪：</strong>支持分布式链路追踪ID</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 * @see com.digit.user.service.impl.UserServiceImpl 用户服务实现
 * @see org.apache.rocketmq.spring.core.RocketMQTemplate RocketMQ模板
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationLogMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 操作者用户ID
     * 
     * <p>执行操作的用户唯一标识符。对于用户注册等操作，
     * 这个ID就是新创建的用户ID。</p>
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 分布式链路追踪ID
     * 
     * <p>用于关联一次完整请求在多个微服务间的调用链，
     * 便于问题排查和性能分析。可以使用Seata的XID或其他追踪ID。</p>
     */
    @JsonProperty("trace_id")
    @Size(max = 50, message = "追踪ID长度不能超过50个字符")
    private String traceId;
    
    /**
     * 操作类型
     * 
     * <p>描述用户执行的具体操作类型，用于日志分类和统计分析。</p>
     * 
     * <p><strong>标准操作类型：</strong></p>
     * <ul>
     *   <li><strong>REGISTER：</strong>用户注册</li>
     *   <li><strong>LOGIN：</strong>用户登录</li>
     *   <li><strong>LOGOUT：</strong>用户登出</li>
     *   <li><strong>UPDATE_USER_INFO：</strong>修改用户信息</li>
     *   <li><strong>CHANGE_PASSWORD：</strong>修改密码</li>
     *   <li><strong>RESET_PASSWORD：</strong>重置密码</li>
     *   <li><strong>UPGRADE_ROLE：</strong>角色升级</li>
     *   <li><strong>DOWNGRADE_ROLE：</strong>角色降级</li>
     * </ul>
     */
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    @JsonProperty("action")
    private String action;
    
    /**
     * 操作者IP地址
     * 
     * <p>记录执行操作的客户端IP地址，用于安全审计和异常行为检测。
     * 支持IPv4和IPv6地址格式。</p>
     */
    @JsonProperty("ip")
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String ip;
    
    /**
     * 操作详情
     * 
     * <p>以JSON格式存储操作的详细信息，如修改前后的字段值、
     * 操作参数等。这个字段提供了操作的完整上下文。</p>
     * 
     * <p><strong>示例格式：</strong></p>
     * <pre>
     * {
     *   "field": "email",
     *   "oldValue": "old@example.com",
     *   "newValue": "new@example.com",
     *   "operator": "admin",
     *   "reason": "用户申请修改"
     * }
     * </pre>
     */
    @JsonProperty("detail")
    private String detail;
    
    /**
     * 操作时间
     * 
     * <p>记录操作发生的时间戳，用于时间序列分析和审计。
     * 使用Timestamp类型，避免RocketMQ序列化问题。</p>
     */
    @JsonProperty("gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp gmtCreate;

    
    /**
     * 创建操作日志消息
     * 
     * @param userId 操作者用户ID
     * @param traceId 分布式链路追踪ID
     * @param action 操作类型
     * @param ip 操作者IP地址
     * @param detail 操作详情
     * @return 操作日志消息对象
     */
    public static OperationLogMessage createMessage(Long userId, String traceId, String action, String ip, String detail) {
        return OperationLogMessage.builder()
                .userId(userId)
                .traceId(traceId)
                .action(action)
                .ip(ip)
                .detail(detail)
                .gmtCreate(new Timestamp(System.currentTimeMillis()))
                .build();
    }
    
    /**
     * 设置操作时间为当前时间
     */
    public void setCurrentTime() {
        this.gmtCreate = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * 检查消息是否有效
     * 
     * @return 如果消息包含必要字段返回true，否则返回false
     */
    public boolean isValid() {
        return action != null && !action.trim().isEmpty() && gmtCreate != null;
    }
} 