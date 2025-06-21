# 分页查询用户列表 API 使用指南

## 功能概述

分页查询用户列表功能允许管理员和超级管理员查看系统中的用户，支持分页、排序和多条件筛选。根据用户角色权限，查询结果会有所不同：

- **管理员（admin）**：只能查询到普通用户（user角色）的信息
- **超级管理员（super_admin）**：可以查询到所有其他用户的信息（包括普通用户和管理员）

## 权限要求

- **Actor**: 管理员 (admin) 或超级管理员 (super_admin)
- **前置条件**: 用户已登录并具有有效的JWT令牌

## 权限控制规则

### 管理员权限
- **可查看**：普通用户（user角色）
- **不可查看**：其他管理员（admin角色）、超级管理员（super_admin角色）、自己

### 超级管理员权限  
- **可查看**：普通用户（user角色）、管理员（admin角色）
- **不可查看**：其他超级管理员（super_admin角色）、自己

### 权限验证流程
1. 系统验证JWT令牌的有效性
2. 查询当前用户的角色信息
3. 根据角色权限确定可查看的用户范围
4. 在数据库查询时自动过滤掉无权查看的用户

## API 端点

```
GET /user/users
```

## 请求格式

### 请求头 (Headers)

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### 查询参数 (Query Parameters)

| 参数名             | 类型    | 必填 | 默认值         | 说明                   |
| ------------------ | ------- | ---- | -------------- | ---------------------- |
| `page`           | Integer | 否   | 1              | 页码（从1开始）        |
| `size`           | Integer | 否   | 10             | 每页大小（1-100）      |
| `sort`           | String  | 否   | gmtCreate,desc | 排序规则               |
| `username`       | String  | 否   | -              | 用户名筛选（模糊匹配） |
| `email`          | String  | 否   | -              | 邮箱筛选（模糊匹配）   |
| `phone`          | String  | 否   | -              | 手机号筛选（模糊匹配） |
| `gmtCreateStart` | String  | 否   | -              | 创建时间开始           |
| `gmtCreateEnd`   | String  | 否   | -              | 创建时间结束           |

### 排序参数说明

**支持的排序字段**:

- `userId` - 用户ID
- `username` - 用户名
- `email` - 邮箱
- `phone` - 手机号
- `gmtCreate` - 创建时间
- `gmtModified` - 修改时间

**排序格式**:

- 单字段: `字段名,方向` (例如: `gmtCreate,desc`)
- 多字段: `字段1,方向1;字段2,方向2` (例如: `gmtCreate,desc;username,asc`)
- 方向: `asc`(升序) 或 `desc`(降序)

### 时间格式

时间参数使用格式: `yyyy-MM-dd HH:mm:ss`
例如: `2024-01-01 00:00:00`

## 请求示例

### 1. 基本分页查询

```bash
curl -X GET "http://localhost:8080/user/users?page=1&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. 带筛选条件的查询

```bash
curl -X GET "http://localhost:8080/user/users?page=1&size=10&username=test&email=example.com" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. 带排序的查询

```bash
curl -X GET "http://localhost:8080/user/users?page=1&size=10&sort=gmtCreate,desc" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. 时间范围查询

```bash
curl -X GET "http://localhost:8080/user/users?page=1&size=10&gmtCreateStart=2024-01-01%2000:00:00&gmtCreateEnd=2024-12-31%2023:59:59" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 5. 复合查询

```bash
curl -X GET "http://localhost:8080/user/users?page=1&size=20&sort=gmtCreate,desc;username,asc&username=admin&gmtCreateStart=2024-01-01%2000:00:00" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 响应格式

### 成功响应 (200 OK)

```json
{
  "code": "200",
  "message": "查询用户列表成功",
  "data": {
    "users": [
      {
        "userId": 1143224085170356224,
        "username": "test01",
        "email": "test01@example.com",
        "phone": "13800138001",
        "gmtCreate": "2024-12-01T10:30:00",
        "gmtModified": "2024-12-15T15:45:00"
      },
      {
        "userId": 1143224085170356225,
        "username": "test02",
        "email": "test02@example.com",
        "phone": "13800138002",
        "gmtCreate": "2024-12-02T11:20:00",
        "gmtModified": "2024-12-16T16:30:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "isFirst": true,
    "isLast": false,
    "hasPrevious": false,
    "hasNext": true
  }
}
```

### 权限不足响应 (403 Forbidden)

```json
{
  "code": "403",
  "message": "权限不足，需要管理员或超级管理员权限",
  "data": null
}
```

### 未认证响应 (401 Unauthorized)

```json
{
  "code": "401",
  "message": "用户未认证",
  "data": null
}
```

### 参数错误响应 (400 Bad Request)

```json
{
  "code": "400",
  "message": "页码必须大于0",
  "data": null
}
```

## 响应字段说明

### 分页信息字段

| 字段名            | 类型    | 说明           |
| ----------------- | ------- | -------------- |
| `currentPage`   | Integer | 当前页码       |
| `pageSize`      | Integer | 每页大小       |
| `totalElements` | Long    | 总记录数       |
| `totalPages`    | Integer | 总页数         |
| `isFirst`       | Boolean | 是否为第一页   |
| `isLast`        | Boolean | 是否为最后一页 |
| `hasPrevious`   | Boolean | 是否有上一页   |
| `hasNext`       | Boolean | 是否有下一页   |

### 用户信息字段

| 字段名          | 类型   | 说明         |
| --------------- | ------ | ------------ |
| `userId`      | Long   | 用户ID       |
| `username`    | String | 用户名       |
| `email`       | String | 邮箱地址     |
| `phone`       | String | 手机号码     |
| `gmtCreate`   | String | 创建时间     |
| `gmtModified` | String | 最后修改时间 |

## 性能说明

1. **分片查询**: 使用ShardingSphere处理跨库跨表查询
2. **索引优化**: 利用数据库索引提升查询性能
3. **分页限制**: 每页最大100条记录，防止大量数据查询
4. **筛选优化**: 支持多条件组合筛选，减少查询范围

## 安全特性

1. **权限验证**: 严格的角色权限控制
2. **JWT认证**: 基于令牌的身份验证
3. **参数校验**: 防止SQL注入和参数攻击
4. **敏感信息过滤**: 不返回密码等敏感字段

## 错误处理

系统会自动处理以下错误情况：

- 权限不足
- 参数格式错误
- 数据库查询异常
- 服务调用失败

所有错误都会返回标准的错误响应格式，包含错误码和详细的错误信息。
