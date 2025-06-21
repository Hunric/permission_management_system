package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 用户分页查询请求数据对象
 * 
 * <p>封装用户列表查询的分页、排序和筛选参数。</p>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPageQueryDTO {
    
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    @Builder.Default
    private Integer page = 1;
    
    /**
     * 每页大小，默认10条，最大100条
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    @Builder.Default
    private Integer size = 10;
    
    /**
     * 排序字段和方向
     * 格式：字段名,方向 (例如: gmt_create,desc 或 username,asc)
     * 支持多个排序字段，用分号分隔 (例如: gmt_create,desc;username,asc)
     */
    private String sort;
    
    /**
     * 用户名筛选条件（模糊匹配）
     */
    private String username;
    
    /**
     * 邮箱筛选条件（模糊匹配）
     */
    private String email;
    
    /**
     * 手机号筛选条件（模糊匹配）
     */
    private String phone;
    
    /**
     * 创建时间范围筛选 - 开始时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String gmtCreateStart;
    
    /**
     * 创建时间范围筛选 - 结束时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String gmtCreateEnd;
} 