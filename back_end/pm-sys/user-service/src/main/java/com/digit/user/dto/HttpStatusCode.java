package com.digit.user.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * HTTP响应状态码枚举
 * 基于RFC 2616和RFC 7231标准定义
 */
@Getter
@AllArgsConstructor
public enum HttpStatusCode {
    
    // ============ 信息响应 (100–199) ============
    CONTINUE(100, "Continue", "继续"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols", "切换协议"),
    PROCESSING(102, "Processing", "处理中"),
    EARLY_HINTS(103, "Early Hints", "早期提示"),
    
    // ============ 成功响应 (200–299) ============
    OK(200, "OK", "成功"),
    CREATED(201, "Created", "已创建"),
    ACCEPTED(202, "Accepted", "已接受"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information", "非权威信息"),
    NO_CONTENT(204, "No Content", "无内容"),
    RESET_CONTENT(205, "Reset Content", "重置内容"),
    PARTIAL_CONTENT(206, "Partial Content", "部分内容"),
    
    // ============ 重定向消息 (300–399) ============
    MULTIPLE_CHOICES(300, "Multiple Choices", "多种选择"),
    MOVED_PERMANENTLY(301, "Moved Permanently", "永久移动"),
    FOUND(302, "Found", "找到"),
    SEE_OTHER(303, "See Other", "查看其他"),
    NOT_MODIFIED(304, "Not Modified", "未修改"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect", "临时重定向"),
    PERMANENT_REDIRECT(308, "Permanent Redirect", "永久重定向"),
    
    // ============ 客户端错误响应 (400–499) ============
    BAD_REQUEST(400, "Bad Request", "错误请求"),
    UNAUTHORIZED(401, "Unauthorized", "未授权"),
    PAYMENT_REQUIRED(402, "Payment Required", "需要付款"),
    FORBIDDEN(403, "Forbidden", "禁止访问"),
    NOT_FOUND(404, "Not Found", "未找到"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", "方法不允许"),
    NOT_ACCEPTABLE(406, "Not Acceptable", "不可接受"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required", "需要代理身份验证"),
    REQUEST_TIMEOUT(408, "Request Timeout", "请求超时"),
    CONFLICT(409, "Conflict", "冲突"),
    GONE(410, "Gone", "已删除"),
    LENGTH_REQUIRED(411, "Length Required", "需要内容长度"),
    PRECONDITION_FAILED(412, "Precondition Failed", "先决条件失败"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large", "请求实体过大"),
    URI_TOO_LONG(414, "URI Too Long", "URI过长"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", "不支持的媒体类型"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable", "范围无法满足"),
    EXPECTATION_FAILED(417, "Expectation Failed", "期望失败"),
    IM_A_TEAPOT(418, "I'm a teapot", "我是茶壶"),
    MISDIRECTED_REQUEST(421, "Misdirected Request", "请求方向错误"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity", "无法处理的实体"),
    LOCKED(423, "Locked", "已锁定"),
    FAILED_DEPENDENCY(424, "Failed Dependency", "失败的依赖"),
    TOO_EARLY(425, "Too Early", "过早"),
    UPGRADE_REQUIRED(426, "Upgrade Required", "需要升级"),
    PRECONDITION_REQUIRED(428, "Precondition Required", "需要先决条件"),
    TOO_MANY_REQUESTS(429, "Too Many Requests", "请求过多"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large", "请求头字段过大"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons", "因法律原因不可用"),
    
    // ============ 服务端错误响应 (500–599) ============
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", "内部服务器错误"),
    NOT_IMPLEMENTED(501, "Not Implemented", "未实现"),
    BAD_GATEWAY(502, "Bad Gateway", "错误网关"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable", "服务不可用"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout", "网关超时"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", "HTTP版本不支持"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates", "变体也协商"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage", "存储空间不足"),
    LOOP_DETECTED(508, "Loop Detected", "检测到循环"),
    NOT_EXTENDED(510, "Not Extended", "未扩展"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required", "需要网络身份验证");
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 英文描述
     */
    private final String reasonPhrase;
    
    /**
     * 中文描述
     */
    private final String description;
    
    /**
     * 根据状态码查找对应的枚举
     * @param code 状态码
     * @return HttpStatusCode枚举，如果不存在则返回null
     */
    public static HttpStatusCode valueOf(int code) {
        for (HttpStatusCode status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为信息响应
     * @return true表示是信息响应(1xx)
     */
    public boolean isInformational() {
        return code >= 100 && code < 200;
    }
    
    /**
     * 判断是否为成功响应
     * @return true表示是成功响应(2xx)
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
    
    /**
     * 判断是否为重定向响应
     * @return true表示是重定向响应(3xx)
     */
    public boolean isRedirection() {
        return code >= 300 && code < 400;
    }
    
    /**
     * 判断是否为客户端错误响应
     * @return true表示是客户端错误响应(4xx)
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }
    
    /**
     * 判断是否为服务端错误响应
     * @return true表示是服务端错误响应(5xx)
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
    
    @Override
    public String toString() {
        return code + " " + reasonPhrase;
    }
} 