---
name: harness
description: >
  Harness 主技能 - 工程流程调度器。
  负责任务调度、状态管理、定时扫描和断点继续。
  协调 harness-req、harness-spec、harness-impl、harness-doctor 四个子技能工作。
---

# Harness 主技能

工程流程调度器，负责协调需求、规格、实现三个阶段的循环工作，并提供状态诊断与修复能力。

## 使用模式

| 模式 | 使用者 | 语法 | 示例 |
|------|--------|------|------|
| **人类** | 用户 | Slash commands | `/harness status` |
| **AI** | 自动化 | task() 调用子技能 | `load_skills=["harness-impl"]` |

---

## 状态文件

状态文件结构定义在 `harness-state` 技能中,完整定义请参阅 [harness-state SKILL.md](../harness-state/SKILL.md)。

**路径：** `.harness/state.json`

**核心字段：**
- `phase` - 当前阶段（idle/requirement/spec/implement/paused/completed）
- `iteration` - 当前迭代（M1/M2/M3/M4）
- `current_task` - 当前任务 ID
- `checkpoints` - 各阶段的检查点
- `tasks` - 任务队列（queue/completed/failed）
- `messages` - 操作日志

---

## 核心能力

### 1. 阶段管理

| 阶段 | 说明 | 转换条件 |
|------|------|----------|
| `idle` | 空闲状态 | 用户发起/定时触发 |
| `requirement` | 需求确认阶段 | 调用 harness-req |
| `spec` | 规格生成阶段 | 需求已确认 |
| `implement` | 实现与验证阶段 | 规格已确认。内部包含 `code -> test -> repair` 自动闭环 |
| `paused` | 暂停 | 错误超限/用户暂停 |
| `completed` | 完成 | 所有任务完成 |

**重要说明：**
- `implement` 阶段内部包含完整的验证闭环（静态检查 → 单元测试 → E2E 验证 → 自动修复）
- 只有当任务通过内部验证后，才会标记为完成并推进到下一任务
- 不再存在独立的 `verify` 阶段，验证是实现的内在约束

### 2. 定时扫描

当配置了定时任务触发时：
1. 读取状态文件
2. 根据当前 phase 和 checkpoint 决定下一步
3. 调用对应的子技能继续执行
4. 支持断点继续

### 3. 任务调度

调度四个子技能：
- `harness-req`: 需求确认循环
- `harness-spec`: 规格生成循环
- `harness-impl`: 自动实现+验证（内置验证闭环）
- `harness-doctor`: 状态诊断与修复（确保状态文件与实际进度一致）

---

## 命令

> 所有命令执行前应通过 `harness-state` 技能读取状态，完成后应通过 `harness-state` 技能写回状态。

### /harness status

获取当前工程状态。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 返回完整状态摘要：
   - 当前阶段
   - 迭代信息
   - 各阶段进度
   - 待完成任务
   - 最近活动

**用法：** `/harness status`

---

### /harness start `<phase>`

开始指定阶段的工作。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证转换合法性
3. 调用 `harness-state set phase <phase>` 更新阶段
4. 初始化对应阶段的状态
5. 返回阶段信息和下一步建议

**用法：**
- `/harness start requirement`
- `/harness start spec`
- `/harness start implement`

---

### /harness pause `<reason>`

暂停当前工作。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state set phase paused` 更新阶段
3. 记录暂停原因（区分类型）：
   - `user_requested`: 用户主动暂停
   - `auto_failed`: 自动修复失败（超过最大尝试次数）
   - `manual_review`: 需要人工审核
4. 调用 `harness-state add-message W <reason>` 记录暂停原因
5. 调用 `harness-state set-checkpoint <当前阶段> <当前checkpoint>` 记录检查点
6. 调用 `harness-state update` 写回状态

**用法：** 
- `/harness pause Token 用量接近上限`
- `/harness pause 自动修复失败，需要人工介入`

---

### /harness resume

从暂停点恢复。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证当前是暂停状态
3. 根据暂停原因决定恢复策略：
   - `user_requested`: 直接恢复原阶段
   - `auto_failed`: 提示用户需要先处理失败任务
4. 根据 checkpoint 恢复对应阶段
5. 返回恢复信息和下一步

**用法：** `/harness resume`

---

### /harness advance

推进到下一阶段。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 检查当前阶段是否可推进（前置条件已满足）
3. 调用 `harness-state set phase <下一阶段>` 更新阶段
4. 初始化新阶段状态
5. 写回状态

**用法：** `/harness advance`

---

### /harness goto `<phase>`

跳转到指定阶段（用于手动控制）。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证目标阶段的可行性
3. 调用 `harness-state set phase <phase>` 直接更新阶段
4. 写回状态

**用法：** `/harness goto spec`

---

### /harness reset

重置整个工程状态。

**动作：**
1. 确认用户意图
2. 调用 `harness-state reset` 重置所有状态
3. 返回重置结果

**用法：** `/harness reset`

---

### /harness set-iteration `<iteration>`

设置当前迭代。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state set iteration <iteration>` 更新迭代
3. 加载对应迭代的任务列表
4. 写回状态

**用法：** `/harness set-iteration M1`

---

### /harness list-tasks

列出当前迭代的任务。

**动作：**
1. 读取 `docs/plan/01-迭代计划.md`
2. 过滤当前迭代的任务
3. 返回任务列表（包含状态：待办/完成/失败）

**用法：** `/harness list-tasks`

---

### /harness show-checkpoint

显示当前检查点信息。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 返回各阶段 checkpoint 详情
3. 提示可以从哪个点恢复

**用法：** `/harness show-checkpoint`

---

### /harness log `<count>`

查看最近的工程日志。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 返回最后 `<count>` 条消息
3. 默认 10 条

**用法：** `/harness log 20`

---

### /harness doctor

执行状态诊断与修复。

**动作：**
1. 调用 `harness-doctor scan` 执行全面诊断
2. 返回诊断报告摘要
3. 如果有 Critical 问题，建议执行 `/harness-doctor fix`

**用法：** `/harness doctor`

---

### /harness doctor-fix

自动修复状态问题。

**动作：**
1. 调用 `harness-doctor fix` 执行自动修复
2. 返回修复报告
3. 如果有未修复的问题，提示用户手动处理

**用法：** `/harness doctor-fix`

---

## 定时触发配置

在 OpenCode 中配置定时任务：

```
{
  "scheduled": {
    "harness-scan": {
      "cron": "0 20 * * *",
      "command": "/harness scan",
      "description": "每天晚上 8 点自动扫描并继续实现"
    }
  }
}
```

详见 OpenCode 配置文档。

---

## 子技能调用约定

### 调用 harness-state

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-state"],
  prompt="读取当前状态: get"
)
```

### 调用 harness-req

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-req"],
  prompt="继续需求确认：从 checkpoint 恢复，上一步是 ask 用户..."
)
```

### 调用 harness-spec

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-spec"],
  prompt="继续 Spec 生成：从 checkpoint 恢复，上一步是..."
)
```

### 调用 harness-impl

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-impl"],
  prompt="继续实现任务：当前任务是 M1-09，上一步是..."
)
```

**注意：** `harness-impl` 内部已包含完整的验证闭环（implement → verify → auto-repair），无需单独调用验证子技能。

### 调用 harness-doctor

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-doctor"],
  prompt="执行状态诊断: scan"
)
```

---

## 断点继续逻辑

```
定时触发:
  调用 harness-state get
  │
  ├─ phase = "idle" → 等待人工触发
  │
  ├─ phase = "requirement" → 调用 harness-req 继续
  │
  ├─ phase = "spec" → 调用 harness-spec 继续
  │
  ├─ phase = "implement"
  │    │
  │    ├─ checkpoint.step = "paused"
  │    │    ├─ attempt >= max_attempts → 报告失败，停止（pause_reason: auto_failed）
  │    │    └─ attempt < max_attempts → 继续 auto-repair
  │    │
  │    └─ 有待完成任务 → 继续 implement → verify → commit
  │         （验证闭环在 harness-impl 内部自动完成）
  │
  ├─ phase = "paused"
  │    ├─ pause_reason = "user_requested" → 等待用户手动 resume
  │    ├─ pause_reason = "auto_failed" → 提示需要人工介入
  │    └─ pause_reason = "manual_review" → 等待用户审核后 resume
  │
  └─ [可选] 定期执行健康检查
       └─ 调用 harness-doctor scan → 发现 Critical 问题 → 调用 harness-doctor fix
```

---

## 定期维护建议

为了确保状态文件的准确性，建议：

1. **每周执行一次诊断**：
   ```bash
   /harness doctor
   /harness doctor-fix
   ```

2. **迭代完成后执行诊断**：
   ```bash
   # 完成 M1 后
   /harness doctor
   /harness doctor-fix
   
   # 然后启动 M2
   /harness-impl start M2
   ```

3. **遇到异常时立即诊断**：
   ```bash
   # 如果发现任务状态不对
   /harness doctor
   /harness doctor-fix
   ```

---

## 实现阶段的验证闭环

`harness-impl` 内部的验证流程遵循多层级验证策略：

1. **静态验证**：Lint、Compile、Type Check
2. **单元/集成测试**：运行项目测试套件
3. **端到端验证**：比对 `docs/verification/` 中的验收标准
4. **策略检查**：安全规范、代码规范、性能规范
5. **自动修复**：验证失败时自动分析错误并修复（最多 5 次）

只有所有验证通过后，任务才会被标记为完成并提交 PR。

---

## 注意事项

- 所有状态变更前必须通过 `harness-state` 读取当前状态
- 子技能调用后必须更新 session_id 以保持连续性
- 定时触发时应通过 `harness-state add-message` 记录触发原因
- 超过 5 次修复尝试后必须停止并报告（pause_reason: auto_failed）
- `implement` 阶段的验证是强制性的，不可跳过
- 暂停时应明确记录暂停原因类型，便于恢复时决策
