# M2-09 - 工具集 API

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m2-09-工具集-api)
> 涉及 Spec: [02-API契约.md](../../spec/02-API契约.md)

## 验证目标

前台工具集 API 返回工具列表和工具详情。

## 测试用例

### TC-01: 获取工具列表

**前置条件：**
- 无

**测试步骤：**
1. GET `/api/public/tools`
2. 验证返回工具列表

**预期结果：**
- 返回工具数组
- 每项包含 id、name、slug、description、category

### TC-02: 获取工具详情

**前置条件：**
- 存在工具数据

**测试步骤：**
1. GET `/api/public/tools/{slug}`
2. 验证返回工具详情

**预期结果：**
- 返回包含 name、description、category、content 字段

## 验收标准

- [ ] TC-01: 获取工具列表通过
- [ ] TC-02: 获取工具详情通过
- [ ] 所有测试用例通过