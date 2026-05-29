---
name: harness-impl
description: >
  Harness Implementation Skills - 自动实现与验证技能。
  负责根据 Plan 和 Spec 自动实现任务：扫描待办 → 实现代码 → 自动修复错误 → 验证 → Git 提交。
  内部包含完整的验证闭环（code -> test -> repair），支持断点继续，通过 `harness-state` 技能管理状态恢复。
---

# Harness Implementation Skills

自动实现与验证技能，负责根据迭代计划自动实现任务并进行验证。

## 核心设计理念

**验证是实现的内在约束，而非独立阶段。**

本技能内部包含完整的验证闭环：
```
实现代码 → 静态检查 → 单元测试 → E2E验证 → 策略检查
    ↑                                      ↓
    └──── 失败？自动修复（最多5次） ←─────┘
```

只有当所有验证通过后，任务才会被标记为完成并提交 PR。

## 状态结构

通过 `harness-state` 技能管理以下状态：

```json
{
  "implement": {
    "iteration": "M1",
    "current_task": "M1-09",
    "checkpoint": {
      "step": "scan|implement|verify|commit|done|paused",
      "task_id": "M1-09",
      "attempt": 0,
      "max_attempts": 5,
      "last_error": null,
      "verification_result": null
    },
    "tasks": {
      "queue": ["M1-01", "M1-02", ...],
      "completed": ["M1-01", "M1-02"],
      "failed": []
    }
  }
}
```

**注意：** `verify` 步骤是内部闭环的一部分，不是独立的阶段。

---

## 命令

### /harness-impl start `<iteration>`

开始指定迭代的实现。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 读取 `docs/plan/01-迭代计划.md` 获取任务列表
3. 确定起始任务（从 checkpoint 恢复或从头开始）
4. 调用 `harness-state set phase implement`
5. 调用 `harness-state set iteration <iteration>`
6. 调用 `harness-state set-checkpoint implement` 初始化 checkpoint
7. 调用 `harness-state add-message I 开始实现迭代：<iteration>`
8. 返回任务列表和起始任务

**用法：** `/harness-impl start M1`

---

### /harness-impl scan

扫描并返回待实现任务列表。

**动作：**
1. 读取 `docs/plan/01-迭代计划.md`
2. 调用 `harness-state get` 获取已完成任务
3. 计算待实现任务列表（排除已完成和失败的）
4. 返回任务列表（包含依赖、涉及文档）
5. 调用 `harness-state set-checkpoint implement` 更新 step=scan
6. 写回状态

**用法：** `/harness-impl scan`

---

### /harness-impl next

获取下一个待实现任务。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 从队列中获取下一个任务
3. 检查依赖是否满足
4. 返回任务详情
5. 调用 `harness-state set current_task <task_id>`
6. 调用 `harness-state set-checkpoint implement` 更新 step=implement
7. 写回状态

**用法：** `/harness-impl next`

---

### /harness-impl implement `<task_id>`

实现指定任务。

**动作：**
1. 读取 `docs/plan/01-迭代计划.md` 找到任务详情
2. 读取涉及的 Requirement 和 Spec 文档
3. 创建分支 `feature/{task_id}-{描述}`
4. 按照 Spec 实现代码
5. 调用 `harness-state add-message I 实现任务：<task_id>`
6. 调用 `harness-state set-checkpoint implement` 更新 step=implement
7. 写回状态

**用法：** `/harness-impl implement M1-09`

---

### /harness-impl verify `<task_id>`

**内部验证步骤**：执行多层级验证（不单独调用，由工作流自动触发）。

**动作：**
1. 读取对应的 Verification 文档（如有）
2. 读取对应的 Spec 文档
3. 执行验证：
   - **静态检查**：lint/compile/type check
   - **单元/集成测试**：运行项目测试套件
   - **端到端验证**：比对 `docs/verification/` 中的验收标准
   - **策略检查**：安全规范、代码规范、性能规范
4. 调用 `harness-state set-checkpoint implement` 记录 `verification_result`
5. 写回状态
6. 返回验证报告

**重要说明：**
- 此命令通常由工作流自动调用，用户无需手动执行
- 验证失败会自动触发 `auto-repair`
- 这是实现阶段的内部闭环，不是独立的外部阶段

**用法：** `/harness-impl verify M1-09`（通常由自动化流程调用）

---

### /harness-impl auto-repair `<error_description>`

自动修复实现中的错误。

**动作：**
1. 调用 `harness-state get-checkpoint implement` 获取当前状态
2. 递增 `implement.checkpoint.attempt`
3. 检查是否超过最大尝试次数（5 次）
4. 如果超过：
   - 调用 `harness-state fail-task <task_id>` 移入 failed
   - 调用 `harness-state set-checkpoint implement` 更新 step=paused
   - 调用 `harness-state add-message E 任务失败：<task_id> - <error_description>`
   - 返回失败报告（pause_reason: auto_failed）
5. 如果未超过：
   - 分析错误原因
   - 生成修复方案
   - 应用修复
   - **重新验证**（回到 verify 步骤）
   - 调用 `harness-state add-message I 修复尝试：attempt=<n>`
6. 写回状态

**用法：** `/harness-impl auto-repair 编译错误：找不到符号 PostMapper`

---

### /harness-impl commit `<task_id>`

提交任务实现并创建 PR。

**前置条件：**
- 任务必须已通过内部验证（`verification_result = passed`）
- 所有策略检查必须通过

**动作：**
1. 验证任务已通过验证
2. 提交代码到分支
3. 创建 Pull Request：
   - 目标分支：`develop`
   - 标题格式：`feat({scope}): {描述} - Closes #{task_id}`
   - Body 包含任务信息、涉及文档、变更内容、验证结果摘要
4. 记录 PR URL
5. 调用 `harness-state complete-task <task_id>`
6. 调用 `harness-state set-checkpoint implement` 更新 step=commit
7. 调用 `harness-state add-message S 任务已完成并提交 PR：<task_id>`
8. 写回状态
9. 返回 PR 信息

**用法：** `/harness-impl commit M1-09`

---

### /harness-impl pause

暂停实现流程（用于手动介入）。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state set-checkpoint implement` 更新 step=paused
3. 调用 `harness-state add-message W 实现流程已暂停`
4. 写回状态
5. 返回暂停报告

**用法：** `/harness-impl pause`

---

### /harness-impl resume

从暂停点恢复实现。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证当前 step 是 "paused"
3. 根据暂停原因决定恢复策略：
   - 如果是自动修复失败，提示用户需要先处理
   - 否则继续下一步
4. 确定下一步（继续实现/下一任务）
5. 恢复执行
6. 写回状态

**用法：** `/harness-impl resume`

---

### /harness-impl status

查看当前实现状态。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 返回摘要：
   - 当前迭代
   - 当前任务
   - 已完成任务数/总数
   - 失败任务数
   - 当前步骤
   - 尝试次数
   - 验证结果
   - 下一步建议

**用法：** `/harness-impl status`

---

### /harness-impl reset

重置实现状态。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state reset-phase implement`
3. 调用 `harness-state add-message I 实现状态已重置`
4. 返回重置结果

**用法：** `/harness-impl reset`

---

## 工作流程

### 自动执行流程（验证闭环）

```
主技能: /harness-impl start M1
AI:     /harness-impl scan
AI:     /harness-impl next
AI:     /harness-impl implement M1-09
│
├─→ [内部验证循环开始]
│   AI:     /harness-impl verify M1-09
│   ├─ 验证通过 → 跳出循环，进入 commit
│   └─ 验证失败 → 进入 auto-repair
│       AI:     /harness-impl auto-repair <错误描述>
│       │       递增 attempt，分析错误，应用修复
│       └─→ 回到 verify（重新验证）
│           [最多重复 5 次]
│           ├─ 成功 → 跳出循环
│           └─ 失败超限 → pause (auto_failed)
└─→ [内部验证循环结束]
│
AI:     /harness-impl commit M1-09
AI:     /harness-impl next
[继续下一任务或暂停]
```

### 断点继续

当主技能 `harness` 定时触发时：
1. 调用 `harness-state get-checkpoint implement`
2. 如果 `step = "paused"` 且 `attempt >= max_attempts`：
   - 报告失败并停止（pause_reason: auto_failed）
3. 如果 `step = "paused"` 且 `attempt < max_attempts`：
   - 继续自动修复循环
4. 如果有待完成任务：
   - 继续 `implement` → `verify` → `commit`（完整闭环）

---

## 验证文档

验证文档独立存放在 `docs/verification/` 目录：

```
docs/verification/
  M1/
    M1-01-后端项目初始化.md
    M1-02-前端项目初始化.md
    ...
  M2/
  M3/
  M4/
```

每个验证文档包含：
- 测试用例（可执行步骤）
- 预期结果
- 验收标准清单
- 验证方法说明

**注意：** 这些文档由 `harness-impl` 内部的 [verify](file://f:\Workspace\VibeCoding\personal-painpoints\Base\frontend\src\components\blog\CaptchaInput.vue#L89-L93) 步骤自动读取和执行，用户无需手动调用验证技能。

---

## 自动修复策略

| 错误类型 | 修复策略 |
|----------|----------|
| 编译错误 | 检查导入、包名、依赖、语法 |
| 测试失败 | 检查测试用例、mock 数据、断言逻辑 |
| Lint 错误 | 运行格式化工具、调整代码风格 |
| 链接错误 | 检查路由配置、组件引用、导入路径 |
| 策略违规 | 移除硬编码密钥、添加必要注释、优化性能 |
| 未知错误 | 日志分析 + 常见模式匹配 + 上下文推理 |

---

## Git 提交规范

分支命名：`feature/{task_id}-{简短描述}`
PR 标题：`feat({scope}): {描述} - Closes #{task_id}`

详见：[docs/spec/06-项目规范/05-分支规范.md](../../spec/06-项目规范/05-分支规范.md)

---

## 与其他技能的关系

- **harness-verify**：已废弃。验证功能已完全整合到 `harness-impl` 内部。
- **harness-state**：通过此技能管理所有状态变更。
- **harness**（主技能）：负责任务调度和阶段转换，`harness-impl` 专注于实现细节。
