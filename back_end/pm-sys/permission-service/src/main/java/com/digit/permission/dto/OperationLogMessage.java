package com.digit.permission.dto;

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
import java.sql.Timestamp;

/**
 * 操作日志消息对象
 * 
 * <p>用于通过RocketMQ发送操作日志信息到日志服务。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
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
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 分布式链路追踪ID
     */
    @JsonProperty("trace_id")
    @Size(max = 50, message = "追踪ID长度不能超过50个字符")
    private String traceId;
    
    /**
     * 操作类型
     */
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    @JsonProperty("action")
    private String action;
    
    /**
     * 操作者IP地址
     */
    @JsonProperty("ip")
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String ip;
    
    /**
     * 操作详情（JSON格式）
     */
    @JsonProperty("detail")
    private String detail;
    
    /**
     * 操作时间
     */
    @JsonProperty("gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp gmtCreate;
} 