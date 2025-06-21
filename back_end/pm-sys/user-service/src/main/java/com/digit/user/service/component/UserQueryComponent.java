package com.digit.user.service.component;

import com.digit.user.dto.UserPageQueryDTO;
import com.digit.user.entity.User;
import com.digit.user.repository.UserRepository;
import com.digit.user.vo.UserInfoVO;
import com.digit.user.vo.UserPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户查询组件
 * 
 * <p>负责处理用户查询相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>分页查询参数验证</li>
 *   <li>排序条件构建</li>
 *   <li>分页查询执行</li>
 *   <li>查询结果转换</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueryComponent {
    
    private final UserRepository userRepository;
    
    // 允许的排序字段
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
            "userId", "username", "email", "phone", "gmtCreate", "gmtModified"
    );
    
    // 日期时间格式
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 验证查询参数
     * 
     * @param queryDTO 查询参数DTO
     */
    public void validateQueryParameters(UserPageQueryDTO queryDTO) {
        log.debug("验证查询参数");
        
        // 验证分页参数
        if (queryDTO.getPage() < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (queryDTO.getSize() < 1 || queryDTO.getSize() > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
        
        // 验证时间格式
        if (StringUtils.hasText(queryDTO.getGmtCreateStart())) {
            validateDateTimeFormat(queryDTO.getGmtCreateStart(), "gmtCreateStart");
        }
        
        if (StringUtils.hasText(queryDTO.getGmtCreateEnd())) {
            validateDateTimeFormat(queryDTO.getGmtCreateEnd(), "gmtCreateEnd");
        }
        
        log.debug("查询参数验证通过");
    }
    
    /**
     * 构建分页对象
     * 
     * @param queryDTO 查询参数DTO
     * @return 分页对象
     */
    public Pageable buildPageable(UserPageQueryDTO queryDTO) {
        log.debug("构建分页对象，页码: {}, 每页大小: {}", queryDTO.getPage(), queryDTO.getSize());
        
        Sort sort = buildSort(queryDTO.getSort());
        return PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize(), sort);
    }
    
    /**
     * 执行分页查询
     * 
     * @param queryDTO 查询参数DTO
     * @param pageable 分页对象
     * @return 分页查询结果
     */
    public Page<User> executePageQuery(UserPageQueryDTO queryDTO, Pageable pageable) {
        log.debug("执行分页查询");
        
        Timestamp startTime = null;
        Timestamp endTime = null;
        
        if (StringUtils.hasText(queryDTO.getGmtCreateStart())) {
            startTime = parseTimestamp(queryDTO.getGmtCreateStart());
        }
        
        if (StringUtils.hasText(queryDTO.getGmtCreateEnd())) {
            endTime = parseTimestamp(queryDTO.getGmtCreateEnd());
        }
        
        return userRepository.findUsersWithFilters(
                queryDTO.getUsername(),
                queryDTO.getEmail(),
                queryDTO.getPhone(),
                startTime,
                endTime,
                pageable
        );
    }
    
    /**
     * 将分页查询结果转换为VO
     * 
     * @param userPage 分页查询结果
     * @return 用户分页VO
     */
    public UserPageVO convertToPageVO(Page<User> userPage) {
        log.debug("转换分页查询结果为VO");
        
        List<UserInfoVO> userList = userPage.getContent().stream()
                .map(this::convertUserToInfoVO)
                .collect(Collectors.toList());
        
        return UserPageVO.builder()
                .users(userList)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber() + 1)
                .pageSize(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .isFirst(userPage.isFirst())
                .isLast(userPage.isLast())
                .build();
    }
    
    /**
     * 验证日期时间格式
     * 
     * @param dateTimeStr 日期时间字符串
     * @param fieldName 字段名称
     */
    private void validateDateTimeFormat(String dateTimeStr, String fieldName) {
        try {
            LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("%s格式错误，正确格式为: yyyy-MM-dd HH:mm:ss", fieldName));
        }
    }
    
    /**
     * 构建排序条件
     * 
     * @param sortStr 排序字符串
     * @return 排序对象
     */
    private Sort buildSort(String sortStr) {
        if (!StringUtils.hasText(sortStr)) {
            return Sort.by(Sort.Direction.DESC, "gmtCreate");
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        String[] sortParts = sortStr.split(",");
        
        for (String part : sortParts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            Sort.Direction direction = Sort.Direction.ASC;
            String field = part;
            
            if (part.startsWith("-")) {
                direction = Sort.Direction.DESC;
                field = part.substring(1);
            } else if (part.startsWith("+")) {
                field = part.substring(1);
            }
            
            validateSortField(field);
            orders.add(new Sort.Order(direction, field));
        }
        
        return orders.isEmpty() ? Sort.by(Sort.Direction.DESC, "gmtCreate") : Sort.by(orders);
    }
    
    /**
     * 验证排序字段
     * 
     * @param field 排序字段
     */
    private void validateSortField(String field) {
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    String.format("不支持的排序字段: %s，支持的字段: %s", field, ALLOWED_SORT_FIELDS));
        }
    }
    
    /**
     * 解析时间戳
     * 
     * @param dateTimeStr 日期时间字符串
     * @return 时间戳
     */
    private Timestamp parseTimestamp(String dateTimeStr) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            return Timestamp.valueOf(localDateTime);
        } catch (DateTimeParseException e) {
            log.error("时间格式解析失败: {}", dateTimeStr);
            throw new IllegalArgumentException("时间格式错误，正确格式为: yyyy-MM-dd HH:mm:ss");
        }
    }
    
    /**
     * 将用户实体转换为用户信息VO
     * 
     * @param user 用户实体
     * @return 用户信息VO
     */
    private UserInfoVO convertUserToInfoVO(User user) {
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gmtCreate(user.getGmtCreate())
                .gmtModified(user.getGmtModified())
                .build();
    }
} 