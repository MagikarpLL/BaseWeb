# M2-01 - 分类管理 API

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m2-01-分类管理-api)
> 涉及 Spec: [02-API契约.md](../../spec/02-API契约.md)

## 验证目标

管理员可增删改查分类，支持拖拽排序。

## 测试用例

### TC-01: 创建分类

**前置条件：**
- 管理员已登录

**测试步骤：**
1. POST `/api/admin/categories` 请求体 `{"name": "前端", "slug": "frontend"}`
2. 验证返回 200 和分类数据

**预期结果：**
- 返回分类 ID 和创建时间
- 数据库中分类已创建

### TC-02: 分类列表

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. GET `/api/admin/categories`
2. 验证返回包含刚创建的分类

**预期结果：**
- 列表包含 name、slug、sort_order 字段
- 按 sort_order 升序排列

### TC-03: 更新分类

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. PUT `/api/admin/categories/{id}` 请求体 `{"name": "前端开发", "sort_order": 10}`
2. 验证返回更新后的数据

**预期结果：**
- 名称已更新
- 排序值已更新

### TC-04: 删除分类

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. DELETE `/api/admin/categories/{id}`
2. 验证返回 204
3. GET `/api/admin/categories/{id}` 验证返回 404

**预期结果：**
- 删除成功
- 数据库中分类已软删除（deleted_at 有值）

### TC-05: 拖拽排序

**前置条件：**
- 存在多个分类

**测试步骤：**
1. PUT `/api/admin/categories/reorder` 请求体 `[{"id": 1, "sort_order": 1}, {"id": 2, "sort_order": 2}]`
2. GET `/api/admin/categories`
3. 验证排序顺序

**预期结果：**
- 返回 200
- 列表按新排序排列

## 验收标准

- [ ] TC-01: 创建分类通过
- [ ] TC-02: 分类列表通过
- [ ] TC-03: 更新分类通过
- [ ] TC-04: 删除分类通过
- [ ] TC-05: 拖拽排序通过
- [ ] 所有测试用例通过