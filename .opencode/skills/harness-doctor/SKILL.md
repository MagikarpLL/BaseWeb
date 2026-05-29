---
name: harness-doctor
description: >
  Harness Doctor Skills - 状态诊断与修复技能。
  负责扫描代码、文档、Git 分支与状态文件的一致性，自动诊断并修复项目状态问题。
  通过 `harness-state` 技能管理状态恢复。
---

# Harness Doctor Skills

状态诊断与修复技能，负责确保 `.harness/state.json` 与实际开发进度保持一致。

## 核心设计理念

**"状态文件应该是实际开发进度的真实反映"**

当状态文件与实际情况不一致时，本技能会：
1. **诊断**：扫描多个维度发现不一致
2. **报告**：生成详细的诊断报告
3. **修复**：自动或半自动修正状态文件

---

## 诊断维度

### 1. 任务状态一致性检查

**检查项：**
- Git 分支是否存在（如 `feature/M1-01-*`）
- Git 分支是否已合并到 `develop`
- 任务对应的代码文件是否存在
- `state.tasks.completed` 是否与 Git 合并记录一致
- `state.tasks.queue` 是否包含已完成的任务（应移除）

**示例问题：**
```
❌ M1-01 在 queue 中，但 Git 分支 feature/M1-01-backend-init 已合并到 develop
✅ M1-02 在 completed 中，Git 分支已合并，状态一致
⚠️  M1-03 在 queue 中，但未找到对应的 Git 分支
```

### 2. 迭代计划同步检查

**检查项：**
- `docs/plan/01-迭代计划.md` 中的任务列表
- `state.tasks.queue` 是否包含所有待办任务
- `state.tasks.completed` 是否包含所有已完成任务
- 是否有新任务添加到计划但未同步到状态

**示例问题：**
```
❌ docs/plan 中有 M1-04，但 state.tasks.queue 中没有
✅ M1-01, M1-02 在 completed 中，与计划一致
⚠️  state.tasks.failed 中有 M1-05，但计划中未标记为失败
```

### 3. 阶段状态验证

**检查项：**
- `state.phase` 是否与当前实际工作阶段匹配
- 如果 phase = "implement"，检查是否有未完成的任务
- 如果 phase = "spec"，检查 Spec 文档是否已确认
- 如果 phase = "paused"，检查 pause_reason 是否设置

**示例问题：**
```
❌ state.phase = "implement"，但 tasks.queue 为空（应该切换到下一迭代或 completed）
✅ state.phase = "spec"，且 spec.confirmed = false，状态一致
⚠️  state.phase = "paused"，但 pause_reason 未设置
```

### 4. Checkpoint 完整性检查

**检查项：**
- 各阶段的 checkpoint 是否存在必要字段
- `implement.checkpoint.step` 是否为有效枚举
- `implement.checkpoint.attempt` 是否超过 max_attempts
- timestamp 是否合理（不过期）

**示例问题：**
```
❌ implement.checkpoint.step = "invalid_step"（不是有效枚举）
✅ requirement.checkpoint.step = "done"，confirmed = true，状态一致
⚠️  implement.checkpoint.timestamp 是 30 天前（可能过期）
```

### 5. 文档存在性检查

**检查项：**
- `state.checkpoints.requirement.doc` 指向的文件是否存在
- `state.checkpoints.spec.spec_doc` 指向的文件是否存在
- `docs/verification/` 中的验证文档是否与任务对应

**示例问题：**
```
❌ requirement.doc = "docs/requirements/xxx.md" 文件不存在
✅ spec.spec_doc = "docs/spec/yyy.md" 文件存在
⚠️  任务 M1-09 缺少对应的 verification 文档
```

---

## 状态结构

通过 `harness-state` 技能管理以下状态：

```json
{
  "doctor": {
    "last_scan": "ISO timestamp",
    "issues_found": 0,
    "issues_fixed": 0,
    "report": {
      "critical": [],
      "warnings": [],
      "suggestions": []
    },
    "checkpoint": {
      "step": "scan|analyze|fix|done",
      "timestamp": null
    }
  }
}
```

---

## 命令

### /harness-doctor scan

全面扫描项目状态，生成诊断报告。

**动作：**
1. 调用 `harness-state get` 读取当前状态
2. 执行多维度检查：
   - 任务状态一致性（Git + 代码 + 状态文件）
   - 迭代计划同步（docs/plan vs state.tasks）
   - 阶段状态验证（phase 合理性）
   - Checkpoint 完整性
   - 文档存在性
3. 分类问题：
   - **Critical**：必须修复（如状态文件损坏）
   - **Warning**：建议修复（如任务队列不同步）
   - **Suggestion**：优化建议（如清理过期的 checkpoint）
4. 调用 `harness-state set-checkpoint doctor` 记录扫描结果
5. 调用 `harness-state add-message I 完成状态诊断，发现 N 个问题`
6. 返回详细诊断报告

**用法：** `/harness-doctor scan`

**输出示例：**
```markdown
## 🔍 Harness 状态诊断报告

**扫描时间**: 2024-01-15 10:30:00
**当前阶段**: implement
**当前迭代**: M1

### ❌ Critical Issues (2)
1. M1-01 在 queue 中，但 Git 分支已合并到 develop
   - 建议: 调用 /harness-state complete-task M1-01

2. state.phase = "implement"，但 tasks.queue 为空
   - 建议: 调用 /harness-impl start M2 或 /harness-state set phase completed

### ⚠️ Warnings (3)
1. docs/plan 中有 M1-04，但 state.tasks.queue 中没有
   - 建议: 调用 /harness-impl scan 同步任务列表

2. implement.checkpoint.timestamp 是 30 天前
   - 建议: 检查是否需要重置 checkpoint

3. 任务 M1-09 缺少 verification 文档
   - 建议: 创建 docs/verification/M1/M1-09-xxx.md

### 💡 Suggestions (1)
1. state.messages 数组有 150+ 条记录，建议清理旧日志
   - 建议: 调用 /harness-state reset 或手动清理

### 📊 统计
- Critical: 2
- Warnings: 3
- Suggestions: 1
- Total: 6
```

---

### /harness-doctor fix

自动修复发现的问题（保守模式）。

**动作：**
1. 调用 `harness-state get-checkpoint doctor` 获取上次扫描结果
2. 如果没有扫描结果，先执行 `/harness-doctor scan`
3. 自动修复**明确的**问题：
   - ✅ Git 分支已合并 → 移动任务从 queue 到 completed
   - ✅ docs/plan 有新任务 → 添加到 state.tasks.queue
   - ✅ phase 与 queue 不匹配 → 调整 phase
   - ❌ 不确定的问题 → 跳过，等待用户确认
4. 调用 `harness-state set-checkpoint doctor` 记录修复结果
5. 调用 `harness-state add-message S 自动修复了 N 个问题`
6. 返回修复报告

**用法：** `/harness-doctor fix`

**输出示例：**
```markdown
## 🔧 自动修复报告

**修复时间**: 2024-01-15 10:35:00

### ✅ Fixed (3)
1. M1-01: 从 queue 移动到 completed（Git 分支已合并）
2. M1-04: 添加到 queue（从 docs/plan 同步）
3. state.phase: 从 "implement" 改为 "completed"（queue 为空）

### ⏸️ Skipped (2)
1. M1-09 缺少 verification 文档
   - 原因: 需要用户确认是否创建
   - 建议: 手动调用 /harness-impl verify M1-09

2. implement.checkpoint.timestamp 过期
   - 原因: 不确定是否需要重置
   - 建议: 手动调用 /harness-impl reset

### 📊 统计
- Fixed: 3
- Skipped: 2
- Remaining Issues: 2
```

---

### /harness-doctor fix-all

强制修复所有问题（包括不确定的，需用户确认）。

**动作：**
1. 执行 `/harness-doctor scan`
2. 对每个问题请求用户确认
3. 根据用户选择执行修复
4. 返回详细修复报告

**用法：** `/harness-doctor fix-all`

---

### /harness-doctor status

查看上次诊断结果。

**动作：**
1. 调用 `harness-state get-checkpoint doctor` 读取诊断状态
2. 返回摘要：
   - 上次扫描时间
   - 发现的问题数
   - 已修复的问题数
   - 剩余问题列表
   - 下一步建议

**用法：** `/harness-doctor status`

---

### /harness-doctor reset

重置诊断状态。

**动作：**
1. 调用 `harness-state reset-phase doctor`
2. 调用 `harness-state add-message I 诊断状态已重置`
3. 返回重置结果

**用法：** `/harness-doctor reset`

---

## 工作流程

### 标准诊断流程

```
用户: /harness-doctor scan
AI:   [执行多维度检查]
AI:   [生成诊断报告]
      发现 6 个问题（2 Critical, 3 Warnings, 1 Suggestion）

用户: /harness-doctor fix
AI:   [自动修复明确的问题]
      修复了 3 个问题，跳过 2 个不确定的

用户: /harness-doctor status
AI:   [显示剩余问题]
      剩余 2 个问题需要手动处理
```

### 定期维护流程

```
# 每周执行一次
/harness-doctor scan
/harness-doctor fix

# 如果发现 Critical 问题
/harness-doctor fix-all
```

---

## 自动修复策略

| 问题类型 | 修复策略 | 是否需要确认 |
|----------|----------|------------|
| Git 分支已合并但任务在 queue | 移动到 completed | ❌ 自动 |
| docs/plan 有新任务 | 添加到 queue | ❌ 自动 |
| phase 与 queue 不匹配 | 调整 phase | ❌ 自动 |
| Checkpoint 字段缺失 | 补充默认值 | ❌ 自动 |
| 文档文件不存在 | 提示创建 | ✅ 需确认 |
| Verification 文档缺失 | 提示创建 | ✅ 需确认 |
| Timestamp 过期 | 提示重置 | ✅ 需确认 |
| Messages 过多 | 提示清理 | ✅ 需确认 |

---

## 与其他技能的关系

- **harness-state**：通过此技能读写状态文件
- **harness-impl**：doctor 可以修复 impl 的状态问题
- **harness**（主技能）：doctor 可以作为独立的诊断工具，也可以被主技能定时调用

---

## 注意事项

- **保守原则**：默认只修复明确的问题，不确定的问题跳过
- **备份建议**：在执行 `/harness-doctor fix` 前，建议备份 `.harness/state.json`
- **Git 依赖**：任务状态检查依赖 Git 命令，确保 Git 可用
- **文档路径**：所有文档路径相对于项目根目录
- **幂等性**：多次执行 `/harness-doctor fix` 不会产生副作用
