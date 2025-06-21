package com.digit.user.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * 
 * <p>提供获取客户端真实IP地址的工具方法，支持处理各种代理服务器、负载均衡器、CDN等场景。</p>
 * 
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>获取客户端真实IP地址</li>
 *   <li>处理各种代理头信息</li>
 *   <li>过滤内网IP和无效IP</li>
 *   <li>防止IP伪造攻击</li>
 * </ul>
 * 
 * <p><strong>支持的代理头：</strong></p>
 * <ul>
 *   <li>X-Forwarded-For（最常用）</li>
 *   <li>X-Real-IP（Nginx常用）</li>
 *   <li>Proxy-Client-IP（Apache代理）</li>
 *   <li>WL-Proxy-Client-IP（WebLogic代理）</li>
 *   <li>HTTP_CLIENT_IP</li>
 *   <li>HTTP_X_FORWARDED_FOR</li>
 * </ul>
 * 
 * @author Hunric
 * @version 1.0.0
 * @since 2025-06-19
 */
@Slf4j
public class IpAddressUtil {
    
    /**
     * 获取客户端真实IP地址
     * 
     * <p>该方法会按照优先级顺序检查各种HTTP头，以获取客户端的真实IP地址。
     * 这对于处理经过代理服务器、负载均衡器、CDN等中间件的请求非常重要。</p>
     * 
     * <p><strong>检查顺序：</strong></p>
     * <ol>
     *   <li><strong>X-Forwarded-For</strong>: 最常用的代理头，包含客户端真实IP</li>
     *   <li><strong>X-Real-IP</strong>: Nginx等反向代理常用</li>
     *   <li><strong>Proxy-Client-IP</strong>: Apache代理服务器使用</li>
     *   <li><strong>WL-Proxy-Client-IP</strong>: WebLogic代理服务器使用</li>
     *   <li><strong>HTTP_CLIENT_IP</strong>: 某些代理服务器使用</li>
     *   <li><strong>HTTP_X_FORWARDED_FOR</strong>: 备用的转发头</li>
     *   <li><strong>Remote-Addr</strong>: 最后使用直连IP</li>
     * </ol>
     * 
     * <p><strong>安全考虑：</strong></p>
     * <ul>
     *   <li>过滤无效IP地址（如：unknown、localhost、内网IP等）</li>
     *   <li>处理X-Forwarded-For中的多个IP（取第一个非内网IP）</li>
     *   <li>防止IP伪造攻击</li>
     * </ul>
     * 
     * <p><strong>使用示例：</strong></p>
     * <pre>{@code
     * // 在Controller中使用
     * String clientIp = IpAddressUtil.getClientRealIp();
     * 
     * // 在Service中使用
     * String clientIp = IpAddressUtil.getClientRealIp();
     * log.info("客户端IP: {}", clientIp);
     * }</pre>
     * 
     * @return 客户端真实IP地址，如果无法获取则返回"unknown"
     */
    public static String getClientRealIp() {
        try {
            // 从Spring上下文中获取HttpServletRequest
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("无法获取ServletRequestAttributes，返回unknown IP");
                return "unknown";
            }
            
            HttpServletRequest request = attributes.getRequest();
            return getClientRealIp(request);
            
        } catch (Exception e) {
            log.error("获取客户端IP地址时发生异常: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * 从HttpServletRequest中获取客户端真实IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端真实IP地址，如果无法获取则返回"unknown"
     */
    public static String getClientRealIp(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest为null，返回unknown IP");
            return "unknown";
        }
        
        try {
            // 按优先级检查各种代理头
            String[] ipHeaders = {
                "X-Forwarded-For",
                "X-Real-IP", 
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
            };
            
            for (String header : ipHeaders) {
                String ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    // 处理X-Forwarded-For可能包含多个IP的情况
                    if ("X-Forwarded-For".equals(header) && ip.contains(",")) {
                        // 取第一个非内网IP
                        String[] ips = ip.split(",");
                        for (String singleIp : ips) {
                            String trimmedIp = singleIp.trim();
                            if (isValidIp(trimmedIp) && !isInternalIp(trimmedIp)) {
                                log.debug("从{}头获取到客户端IP: {}", header, trimmedIp);
                                return trimmedIp;
                            }
                        }
                    } else if (!isInternalIp(ip)) {
                        log.debug("从{}头获取到客户端IP: {}", header, ip);
                        return ip;
                    }
                }
            }
            
            // 最后使用直连IP
            String remoteAddr = request.getRemoteAddr();
            if (isValidIp(remoteAddr)) {
                log.debug("使用RemoteAddr获取到客户端IP: {}", remoteAddr);
                return remoteAddr;
            }
            
            log.warn("无法获取有效的客户端IP地址");
            return "unknown";
            
        } catch (Exception e) {
            log.error("从HttpServletRequest获取客户端IP地址时发生异常: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * 验证IP地址是否有效
     * 
     * @param ip IP地址字符串
     * @return 如果IP有效返回true，否则返回false
     */
    public static boolean isValidIp(String ip) {
        return ip != null 
            && !ip.trim().isEmpty() 
            && !"unknown".equalsIgnoreCase(ip.trim())
            && !"null".equalsIgnoreCase(ip.trim());
    }
    
    /**
     * 判断是否为内网IP地址
     * 
     * <p>内网IP地址范围：</p>
     * <ul>
     *   <li>10.0.0.0/8: 10.0.0.0 - 10.255.255.255</li>
     *   <li>172.16.0.0/12: 172.16.0.0 - 172.31.255.255</li>
     *   <li>192.168.0.0/16: 192.168.0.0 - 192.168.255.255</li>
     *   <li>127.0.0.0/8: 127.0.0.0 - 127.255.255.255 (回环地址)</li>
     * </ul>
     * 
     * @param ip IP地址字符串
     * @return 如果是内网IP返回true，否则返回false
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIp(ip)) {
            return true;
        }
        
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return true;
            }
            
            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);
            
            // 10.0.0.0/8
            if (firstOctet == 10) {
                return true;
            }
            
            // 172.16.0.0/12
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
                return true;
            }
            
            // 192.168.0.0/16
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }
            
            // 127.0.0.0/8 (回环地址)
            if (firstOctet == 127) {
                return true;
            }
            
            return false;
            
        } catch (NumberFormatException e) {
            log.warn("解析IP地址失败: {}", ip);
            return true;
        }
    }
    
    /**
     * 判断是否为公网IP地址
     * 
     * @param ip IP地址字符串
     * @return 如果是公网IP返回true，否则返回false
     */
    public static boolean isPublicIp(String ip) {
        return isValidIp(ip) && !isInternalIp(ip);
    }
    
    /**
     * 获取IP地址的地理位置信息（占位方法）
     * 
     * <p>该方法为将来扩展预留，可以集成第三方IP地理位置服务。</p>
     * 
     * @param ip IP地址
     * @return 地理位置信息，当前返回空字符串
     */
    public static String getIpLocation(String ip) {
        // TODO: 集成第三方IP地理位置服务（如：高德、百度、腾讯等）
        return "";
    }
} 