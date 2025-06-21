package com.digit.logging.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 操作日志实体类
 * 
 * <p>映射logging_db数据库中的operation_logs表，用于存储用户操作日志。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    
    /**
     * 日志ID（自增主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    /**
     * 操作者用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 分布式链路追踪ID
     */
    @Column(name = "trace_id", length = 50)
    private String traceId;
    
    /**
     * 操作类型
     * 如：REGISTER, LOGIN, LOGOUT, UPDATE_USER_INFO, CHANGE_PASSWORD等
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    
    /**
     * 操作者IP地址
     */
    @Column(name = "ip", length = 45)
    private String ip;
    
    /**
     * 操作详情（JSON格式）
     */
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;
    
    /**
     * 操作时间
     */
    @Column(name = "gmt_create", nullable = false)
    private Timestamp gmtCreate;
}