# 全局异常处理使用说明

## 概述

本项目使用了统一的全局异常处理机制，通过`@RestControllerAdvice`注解的`GlobalExceptionHandler`类来统一处理所有异常，确保API响应格式的一致性。

## 自定义异常类

### 1. UserNotFoundException
- **用途**: 当查找用户时用户不存在
- **HTTP状态码**: 404 Not Found
- **使用示例**:
```java
throw new UserNotFoundException("username", "testuser");
throw new UserNotFoundException(123L);
```

### 2. AuthenticationException
- **用途**: 身份验证失败（如密码错误）
- **HTTP状态码**: 401 Unauthorized
- **使用示例**:
```java
throw new AuthenticationException("用户名或密码错误");
```

### 3. UserAlreadyExistsException
- **用途**: 用户注册时用户名已存在
- **HTTP状态码**: 409 Conflict
- **使用示例**:
```java
throw UserAlreadyExistsException.forUsername("testuser");
throw new UserAlreadyExistsException("用户名已存在");
```

### 4. BusinessException
- **用途**: 通用业务异常
- **HTTP状态码**: 400 Bad Request
- **使用示例**:
```java
throw new BusinessException("USER_001", "业务规则验证失败");
```

## 标准异常处理

### 参数校验异常
- `MethodArgumentNotValidException`: 请求体参数校验失败
- `ConstraintViolationException`: 约束验证失败
- `HttpMessageNotReadableException`: 请求体格式错误

### 系统异常
- `IllegalArgumentException`: 非法参数异常
- `RuntimeException`: 运行时异常
- `Exception`: 通用异常

## 控制器使用方式

**推荐方式**（使用全局异常处理）:
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<UserLoginVO>> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
    UserLoginVO result = userService.login(userLoginDTO);
    ApiResponse<UserLoginVO> response = ApiResponse.success("登录成功", result);
    return ResponseEntity.ok(response);
}
```

**不推荐方式**（手动处理异常）:
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<UserLoginVO>> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
    try {
        UserLoginVO result = userService.login(userLoginDTO);
        return ResponseEntity.ok(ApiResponse.success("登录成功", result));
    } catch (AuthenticationException e) {
        return ResponseEntity.status(401).body(ApiResponse.unauthorized(e.getMessage()));
    }
}
```

## 响应格式

所有异常都会被转换为统一的API响应格式：

```json
{
  "code": "400",
  "message": "用户名不能为空",
  "data": null
}
```

## 最佳实践

1. **业务逻辑层**：抛出具体的业务异常，不要在Service层处理HTTP响应
2. **控制器层**：简化异常处理，让全局异常处理器统一处理
3. **异常消息**：使用用户友好的中文错误消息
4. **日志记录**：全局异常处理器会自动记录适当级别的日志
5. **安全性**：不要在异常消息中暴露敏感的系统信息 