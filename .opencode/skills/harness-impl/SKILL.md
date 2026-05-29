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

### /harness-impl help

显示帮助信息，列出所有可用命令和用法。

**动作：**
1. 返回技能概述和所有命令列表

**输出示例：**
```
# 📖 harness-impl 技能帮助

## 技能概述
自动实现与验证技能，负责根据迭代计划自动实现任务并进行验证。
内部包含完整的验证闭环（code -> test -> repair）。

## 可用命令

### 1. /harness-impl help
显示此帮助信息

### 2. /harness-impl start <iteration>
开始指定迭代的实现
**示例**: `/harness-impl start M1`

### 3. /harness-impl scan
扫描并返回待实现任务列表
**示例**: `/harness-impl scan`

### 4. /harness-impl next
获取下一个待实现任务
**示例**: `/harness-impl next`

### 5. /harness-impl implement <task_id>
实现指定任务（自动生成 Verification 文档并触发验证）
**示例**: `/harness-impl implement M1-09`

### 6. /harness-impl verify <task_id>
验证任务实现是否符合规格（通常由 implement 自动触发）
**示例**: `/harness-impl verify M1-09`

### 7. /harness-impl auto-repair <error_description>
自动修复实现中的错误（通常由 verify 自动触发）
**示例**: `/harness-impl auto-repair "编译错误：找不到符号"`

### 8. /harness-impl commit <task_id> [--continue]
提交任务实现并创建 PR，可选自动继续下一个任务
**示例**: `/harness-impl commit M1-09` 或 `/harness-impl commit M1-09 --continue`

### 9. /harness-impl pause
暂停实现流程
**示例**: `/harness-impl pause`

### 10. /harness-impl resume
从暂停点恢复实现
**示例**: `/harness-impl resume`

### 11. /harness-impl status
查看当前实现状态
**示例**: `/harness-impl status`

### 12. /harness-impl reset
重置实现状态
**示例**: `/harness-impl reset`

## 快速开始

```bash
# 启动迭代
/harness-impl start M1

# 实现任务（会自动验证和修复）
/harness-impl implement M1-09
```
```

**用法：** `/harness-impl help`

---

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

实现指定任务（包含测试代码编写、Verification 文档生成和自动验证）。

**核心原则**：代码与测试必须同步实现，测试代码是交付物的一部分。

**动作：**
1. 读取 `docs/plan/01-迭代计划.md` 找到任务详情
2. 读取涉及的 Requirement 和 Spec 文档
3. 创建分支 `feature/{task_id}-{描述}`
4. **分析 Spec 中的验收标准，确定需要的测试类型**：
   - 前端组件/页面 → 编写 E2E 测试 (.spec.ts)
   - Composables/Utils → 编写单元测试 (.test.ts)
   - 后端 API/Service → 编写 JUnit 测试
5. **编写测试代码**（与实现代码同步）：
   - 创建 `frontend/tests/unit/{task_id}.test.ts`（如有单元测试）
   - 创建 `frontend/tests/e2e/{task_id}.spec.ts`（如有 E2E 测试）
   - 创建 `backend/src/test/java/.../{task_id}Test.java`（如有后端测试）
   - 测试代码必须能通过 `npm run test` / `mvn test` 验证
6. **生成 Verification 文档**（如果不存在）：
   - 根据 Spec 中的验收标准
   - 创建 `docs/verification/{iteration}/{task_id}-{描述}.md`
   - **必须包含每个测试的"执行命令"字段**
   - 执行命令引用实际存在的测试文件
7. **按照 Spec 实现代码**
8. 调用 `harness-state add-message I 实现任务：<task_id>`
9. 调用 `harness-state set-checkpoint implement` 更新 step=implement
10. 写回状态
11. **自动触发验证**：
    - 调用 `/harness-impl verify <task_id>`
    - 如果验证失败，自动进入 auto-repair 循环

**重要说明**：
- 测试代码是实现的一部分，必须与代码同步编写
- **不能事后补测试** - 测试应该在 implement 阶段就完成
- Verification 文档中的执行命令必须指向实际存在的测试文件
- 验证失败会自动触发 auto-repair（最多 5 次）
- 只有验证通过后，才会返回成功

**测试代码位置约定**：

| 测试类型 | 前端路径 | 后端路径 |
|----------|----------|----------|
| 单元测试 | `frontend/tests/unit/{task_id}.test.ts` | `backend/src/test/java/com/example/personalwebsite/{layer}/{Entity}Test.java` |
| E2E 测试 | `frontend/tests/e2e/{task_id}.spec.ts` | - |
| 集成测试 | - | `backend/src/test/java/com/example/personalwebsite/{layer}/{Entity}IT.java` |

**后端测试包结构**（与 src/main/java 同级包结构）：
```
backend/src/test/java/com/example/personalwebsite/
├── controller/
│   └── {Entity}ControllerTest.java
├── service/
│   └── {Entity}ServiceTest.java
├── mapper/
│   └── {Entity}MapperTest.java
├── model/
│   └── {Entity}Test.java
└── util/
    └── {UtilClass}Test.java
```

**用法：** `/harness-impl implement M1-09`

---

### /harness-impl verify `<task_id>`

**内部验证步骤**：执行多层级验证（由 implement 命令自动触发，也可手动调用）。

**核心原则**：验证必须是**实际运行**，而非静态检查。

**动作：**
1. **检查 Verification 文档是否存在**：
   - 如果不存在，提示需要先运行 `/harness-impl implement <task_id>` 生成
   - 或者手动创建 `docs/verification/{iteration}/{task_id}-{描述}.md`
2. 读取对应的 Verification 文档
3. 读取对应的 Spec 文档
4. **执行实际验证**（按顺序执行每个测试用例）：
   
   **4.1 解析测试用例命令**：
   - 读取 verification 文档中的"执行命令"字段
   - 解析出需要运行的命令（如 `npm run build`、`mvn test`）
   
   **4.2 运行测试命令**：
   - **前端**：`cd frontend && npm run <command>` 
   - **后端**：`cd backend && mvn <command>`
   - 捕获 stdout/stderr 输出
   - 记录运行时间
   
   **4.3 结果判定**：
   - 退出码 = 0 → 通过
   - 退出码 ≠ 0 → 失败，记录错误输出
   
   **4.4 记录验证结果**：
   - 每个测试用例记录：命令、输出摘要、退出码、运行时间、pass/fail
   - 生成实际验证报告（包含命令输出）
   
5. 调用 `harness-state set-checkpoint implement` 记录 `verification_result`
6. 写回状态
7. 返回验证报告（**包含实际命令输出**）
8. **如果验证失败**：
   - 记录错误信息到 `last_error`（包含实际错误输出）
   - 递增 `attempt` 计数器
   - 如果 `attempt < max_attempts`，自动调用 `/harness-impl auto-repair <error_description>`
   - 如果 `attempt >= max_attempts`，暂停并报告失败

**Verification 文档格式要求**：

Verification 文档中的测试用例必须包含**可执行的命令**，格式如下：

```markdown
### TC-01: i18n 配置验证

**执行命令**: `cd frontend && npm run build`

**预期结果**:
- 退出码 = 0
- 无编译错误

### TC-02: 语言切换 E2E 测试

**执行命令**: `cd frontend && npm run test:e2e -- --grep "LanguageSwitch"`

**预期结果**:
- 退出码 = 0
- 测试通过数量 > 0
```

**验证报告格式**：

```markdown
## 验证报告

**任务**: M5-03 i18n 配置
**验证时间**: 2024-01-15 10:30:00
**状态**: ✅ PASSED

### TC-01: i18n 配置验证
- **命令**: `cd frontend && npm run build`
- **退出码**: 0
- **运行时间**: 45s
- **状态**: ✅ PASSED
- **输出摘要**: 无错误输出

### TC-02: 语言切换 E2E 测试
- **命令**: `cd frontend && npm run test:e2e -- --grep "LanguageSwitch"`
- **退出码**: 0
- **运行时间**: 23s
- **状态**: ✅ PASSED
- **输出摘要**: 3 passed, 0 failed
```

**重要说明：**
- 验证必须是**实际运行**，不能只是读取代码检查
- verification 文档中的"执行命令"字段是**必须**的
- 如果文档中只有静态检查没有执行命令，提示用户更新文档
- 此命令通常由 `implement` 命令自动调用
- 验证失败会自动触发 `auto-repair`，形成闭环
- 这是实现阶段的核心验证环节

**用法：** 
- 自动调用：`/harness-impl implement M1-09`（内部自动触发 verify）
- 手动调用：`/harness-impl verify M1-09`（用于调试或重新验证）

---

### /harness-impl auto-repair `<error_description>`

自动修复实现中的错误（由 verify 命令自动触发）。

**动作：**
1. 调用 `harness-state get-checkpoint implement` 获取当前状态
2. 递增 `implement.checkpoint.attempt`
3. 检查是否超过最大尝试次数（5 次）
4. 如果超过：
   - 调用 `harness-state fail-task <task_id>` 移入 failed
   - 调用 `harness-state set-checkpoint implement` 更新 step=paused
   - 调用 `harness-state set-pause-reason auto_failed`
   - 调用 `harness-state add-message E 任务失败：<task_id> - <error_description>`
   - 返回失败报告
5. 如果未超过：
   - 分析错误原因
   - 生成修复方案
   - 应用修复
   - **自动重新验证**（调用 `/harness-impl verify <task_id>`）
   - 调用 `harness-state add-message I 修复尝试：attempt=<n>`
6. 写回状态

**重要说明：**
- 此命令通常由 `verify` 命令在验证失败时自动调用
- 修复后会自动重新验证，形成闭环
- 最多尝试 5 次，超限后暂停等待人工介入

**用法：** 
- 自动调用：验证失败时由 verify 自动触发
- 手动调用：`/harness-impl auto-repair "编译错误：找不到符号 PostMapper"`

---

### /harness-impl commit `<task_id>` [--continue]

提交任务实现并创建 PR。

**前置条件：**
- 任务必须已通过内部验证（`verification_result = passed`）
- 所有策略检查必须通过

**参数说明：**
- `task_id`: 要提交的任务 ID
- `--continue`: （可选）提交后自动检测并继续下一个任务或迭代

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

9. **如果指定了 --continue 参数，执行自动流转：**
   
   **9.1 检查当前迭代是否还有未完成的任务：**
   - 读取 `state.tasks.queue`
   - 如果 queue 不为空：
     - 获取下一个任务 ID
     - 返回提示："✅ 任务已提交！检测到队列中还有 {N} 个待办任务"
     - 建议命令：`/harness-impl implement <next_task_id>` 或 `/harness-impl next`
     - 如果用户确认，自动调用 `/harness-impl implement <next_task_id>`
   
   **9.2 如果当前迭代已完成（queue 为空）：**
   - 检查 Plan 中是否有下一个迭代
   - 如果有下一个迭代：
     - 返回提示："🎉 M5 迭代完成！所有 7 个任务均已正确实现"
     - 建议命令：`/harness-impl start M6` 启动下一迭代
     - 如果用户确认，自动调用 `/harness-impl start M6`
   - 如果没有下一个迭代：
     - 返回提示："🎉 所有迭代已完成！项目进入完成状态"
     - 建议命令：`/harness-state set phase completed`
     - 如果用户确认，自动标记项目为完成

10. 返回 PR 信息和下一步建议

**用法：** 
- 仅提交：`/harness-impl commit M1-09`
- 提交并继续：`/harness-impl commit M1-09 --continue`

**输出示例（带 --continue）：**
```


```

