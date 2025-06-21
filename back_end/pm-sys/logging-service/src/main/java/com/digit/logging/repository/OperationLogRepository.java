package com.digit.logging.repository;

import com.digit.logging.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 操作日志数据访问接口
 * 
 * <p>提供操作日志相关的数据库操作方法，基于Spring Data JPA实现。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    /**
     * 根据用户ID查询操作日志
     * 
     * @param userId 用户ID
     * @return 用户的操作日志列表
     */
    List<OperationLog> findByUserIdOrderByGmtCreateDesc(Long userId);
    
    /**
     * 根据操作类型查询日志
     * 
     * @param action 操作类型
     * @return 指定操作类型的日志列表
     */
    List<OperationLog> findByActionOrderByGmtCreateDesc(String action);
    
    /**
     * 根据用户ID和操作类型查询日志
     * 
     * @param userId 用户ID
     * @param action 操作类型
     * @return 符合条件的日志列表
     */
    List<OperationLog> findByUserIdAndActionOrderByGmtCreateDesc(Long userId, String action);
    
    /**
     * 根据时间范围查询日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 指定时间范围内的日志列表
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.gmtCreate BETWEEN :startTime AND :endTime ORDER BY ol.gmtCreate DESC")
    List<OperationLog> findByTimeRange(@Param("startTime") Timestamp startTime, 
                                       @Param("endTime") Timestamp endTime);
    
    /**
     * 根据链路追踪ID查询日志
     * 
     * @param traceId 链路追踪ID
     * @return 同一链路的日志列表
     */
    List<OperationLog> findByTraceIdOrderByGmtCreateDesc(String traceId);
} 