package com.digit.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 
 * <p>提供JSON Web Token的生成、解析和验证功能。
 * 该工具类封装了JWT相关的核心操作，确保令牌的安全性和一致性。</p>
 * 
 * <p><strong>功能特性：</strong></p>
 * <ul>
 *   <li>安全的令牌生成：使用HMAC-SHA256算法</li>
 *   <li>令牌验证：检查签名、过期时间等</li>
 *   <li>Claims提取：获取用户ID、用户名等信息</li>
 *   <li>异常处理：统一处理各种JWT异常</li>
 * </ul>
 * 
 * @author System
 * @version 1.0.0
 * @since 2025-01-01
 */
@Slf4j
@Component
public class JwtUtil {
    
    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * JWT过期时间（秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.getSubject());
    }
    
    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 获取令牌过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 验证令牌是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
    
    /**
     * 从令牌中解析Claims
     * 
     * @param token JWT令牌
     * @return Claims对象
     * @throws JwtException 令牌解析异常
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 获取令牌过期时间（秒）
     * 
     * @return 过期时间
     */
    public Long getExpiration() {
        return expiration;
    }
} 