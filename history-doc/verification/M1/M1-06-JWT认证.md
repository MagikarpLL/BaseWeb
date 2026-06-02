# M1-06 - JWT 认证实现

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m1-06-jwt-认证实现)
> 涉及 Spec: [04-认证与安全.md](../../spec/04-认证与安全.md)

## 验证目标

JWT 认证实现：登录/登出/刷新/拦截器正常工作。

## 测试用例

### TC-01: 用户登录

**前置条件：**
- 存在管理员账户

**测试步骤：**
1. POST `/api/auth/login` 请求体 `{"username": "admin", "password": "admin123"}`
2. 验证返回 Token

**预期结果：**
- 返回 Access Token 和 Refresh Token
- Access Token 有效期 15 分钟

### TC-02: Token 验证

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 使用 Access Token 访问受保护资源 `/api/admin/posts`
2. 验证返回 200

**预期结果：**
- 请求成功
- Token 有效

### TC-03: Token 过期处理

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 使用过期或无效 Token 访问受保护资源
2. 验证返回 401

**预期结果：**
- 返回 401 Unauthorized
- message 提示 Token 无效或过期

### TC-04: Token 刷新

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 使用 Refresh Token 调用 POST `/api/auth/refresh`
2. 验证返回新的 Access Token

**预期结果：**
- 返回新的 Access Token
- Refresh Token 可继续使用

### TC-05: 用户登出

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 调用 POST `/api/auth/logout`
2. 使用原 Token 访问受保护资源

**预期结果：**
- 登出成功
- 原 Token 失效（返回 401）

### TC-06: 拦截器验证

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 未携带 Token 访问 `/api/admin/posts`
2. 验证被拦截

**预期结果：**
- 返回 401
- 未到达业务逻辑

## 验收标准

- [ ] TC-01: 用户登录通过
- [ ] TC-02: Token 验证通过
- [ ] TC-03: Token 过期处理通过
- [ ] TC-04: Token 刷新通过
- [ ] TC-05: 用户登出通过
- [ ] TC-06: 拦截器验证通过
- [ ] 所有测试用例通过