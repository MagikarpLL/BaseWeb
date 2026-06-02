# M3-01 - 评论提交 API（含验证码）

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m3-01-评论提交-api含验证码)
> 涉及 Spec: [02-API契约.md](../../spec/02-API契约.md)

## 验证目标

访客可提交评论，需要验证码，评论需审核后才显示。

## 测试用例

### TC-01: 获取验证码

**前置条件：**
- 无

**测试步骤：**
1. GET `/api/public/comments/captcha`
2. 验证返回验证码图片和 ID

**预期结果：**
- 返回 captcha_id 和 captcha_image（Base64）

### TC-02: 提交评论

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. POST `/api/comments` 请求体 `{"post_id": 1, "content": "测试评论", "author": "访客", "captcha_id": "xxx", "captcha_code": "xxxx"}`
2. 验证返回

**预期结果：**
- 返回 200 或提交成功
- 状态为 pending（待审核）

### TC-03: 验证码错误

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. POST `/api/comments` 请求体 `{"captcha_code": "wrong"}`
2. 验证返回错误

**预期结果：**
- 返回 400 或类似错误码
- 提示验证码错误

## 验收标准

- [ ] TC-01: 获取验证码通过
- [ ] TC-02: 提交评论通过
- [ ] TC-03: 验证码错误处理通过
- [ ] 所有测试用例通过