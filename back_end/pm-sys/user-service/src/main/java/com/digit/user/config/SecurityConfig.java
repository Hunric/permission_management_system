package com.digit.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 * 
 * <p>提供密码加密相关的Bean配置，以及Web安全策略配置。</p>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * BCrypt密码编码器Bean
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP安全配置
     * 
     * <p>配置哪些端点需要认证，哪些端点可以公开访问。</p>
     * 
     * @param http HttpSecurity配置对象
     * @throws Exception 配置异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为我们使用JWT
            .csrf().disable()
            // 配置会话管理为无状态
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置授权规则
            .authorizeRequests()
                // 允许公开访问的端点
                .antMatchers(
                    "/user/register",      // 用户注册
                    "/user/login",         // 用户登录
                    "/actuator/**",        // Spring Boot Actuator端点
                    "/swagger-ui/**",      // Swagger UI
                    "/v3/api-docs/**"      // OpenAPI文档
                ).permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            .and()
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
} 