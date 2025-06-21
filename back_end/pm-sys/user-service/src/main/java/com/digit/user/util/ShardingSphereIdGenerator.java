package com.digit.user.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ShardingSphere ID 生成器工具类
 * 
 * <p>使用雪花算法生成分布式唯一ID。这个实现是一个简化版的雪花算法，
 * 在生产环境中建议使用更完善的实现。</p>
 * 
 * @author digit
 * @since 1.0
 */
@Slf4j
@Component
public class ShardingSphereIdGenerator {
    
    // 起始时间戳 (2023-01-01 00:00:00)
    private static final long START_TIMESTAMP = 1672531200000L;
    
    // 序列号占用的位数
    private static final long SEQUENCE_BIT = 12;
    
    // 机器标识占用的位数
    private static final long MACHINE_BIT = 5;
    
    // 数据中心占用的位数
    private static final long DATACENTER_BIT = 5;
    
    // 最大序列号
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    
    // 机器ID最大值
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    
    // 数据中心ID最大值
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    
    // 机器ID偏移量
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    
    // 数据中心ID偏移量
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    
    // 时间戳偏移量
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    
    private final long datacenterId;
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public ShardingSphereIdGenerator() {
        // 在实际应用中，这些值应该从配置中获取
        this.datacenterId = 1L;
        this.machineId = 1L;
        
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
    }
    
    /**
     * 生成下一个ID
     * 
     * @return 分布式唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();
        
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currentTimestamp = getNextTimestamp();
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = currentTimestamp;
        
        long id = (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
        
        log.debug("生成雪花算法ID: {}", id);
        return id;
    }
    
    private long getNextTimestamp() {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
    
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
} 