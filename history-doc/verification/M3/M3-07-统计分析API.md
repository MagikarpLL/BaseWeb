# M3-07 - 统计分析 API

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m3-07-统计分析-api概览趋势页面来源)
> 涉及 Spec: [02-API契约.md](../../spec/02-API契约.md)

## 验证目标

统计分析 API 返回概览、趋势、页面、来源数据。

## 测试用例

### TC-01: 获取概览数据

**前置条件：**
- 管理员已登录
- 存在埋点数据

**测试步骤：**
1. GET `/api/admin/analytics/overview`
2. 验证返回统计数据

**预期结果：**
- 返回 total_pv、total_uv、today_pv、today_uv

### TC-02: 获取趋势数据

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. GET `/api/admin/analytics/trend?days=7`
2. 验证返回趋势数据

**预期结果：**
- 返回每日 PV/UV 数据
- 按日期升序排列

### TC-03: 获取页面排名

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. GET `/api/admin/analytics/pages?limit=10`
2. 验证返回页面排名

**预期结果：**
- 返回页面路径和 PV 列表
- 按 PV 降序排列

### TC-04: 获取来源统计

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. GET `/api/admin/analytics/sources`
2. 验证返回来源数据

**预期结果：**
- 返回 direct/search/social 等来源统计

## 验收标准

- [ ] TC-01: 获取概览数据通过
- [ ] TC-02: 获取趋势数据通过
- [ ] TC-03: 获取页面排名通过
- [ ] TC-04: 获取来源统计通过
- [ ] 所有测试用例通过