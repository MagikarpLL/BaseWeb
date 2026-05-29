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

### /harness-doctor help

显示帮助信息，列出所有可用命令和用法。

**动作：**
1. 返回技能概述和所有命令列表

**输出示例：**
```
# 📖 harness-doctor 技能帮助

## 技能概述
状态诊断与修复技能，负责扫描代码、文档、Git 分支与状态文件的一致性，自动诊断并修复项目状态问题。

## 可用命令

### 1. /harness-doctor help
显示此帮助信息

### 2. /harness-doctor scan
全面扫描项目状态，生成诊断报告
**示例**: `/harness-doctor scan`

### 3. /harness-doctor fix
自动修复发现的问题（保守模式）
**示例**: `/harness-doctor fix`

### 4. /harness-doctor fix-all
强制修复所有问题（包括不确定的，需用户确认）
**示例**: `/harness-doctor fix-all`

### 5. /harness-doctor generate-missing-verifications
自动生成缺失的 verification 文档（基于 Spec 文档）
**示例**: `/harness-doctor generate-missing-verifications`

### 6. /harness-doctor generate-missing-tests
自动生成缺失的测试代码文件（基于 verification 文档）
**示例**: `/harness-doctor generate-missing-tests`

### 7. /harness-doctor status
查看上次诊断结果
**示例**: `/harness-doctor status`

### 8. /harness-doctor reset
重置诊断状态
**示例**: `/harness-doctor reset`

## 快速开始

```bash
# 执行诊断
/harness-doctor scan

# 查看问题
/harness-doctor status

# 自动生成缺失的 verification 文档
/harness-doctor generate-missing-verifications

# 自动修复其他问题
/harness-doctor fix
```
```

**用法：** `/harness-doctor help`

---

### /harness-doctor scan

全面扫描项目状态，生成诊断报告。

**动作：**
1. 调用 `harness-state get` 读取当前状态
2. 执行多维度检查：

   **维度 1：任务状态一致性（Git + 代码 + 状态文件）**
   - 检查 `tasks.completed` 中的任务是否有对应的 Git 分支已合并
   - 检查 `tasks.queue` 中的任务是否真的未完成（检查代码/分支）
   - 检查 `tasks.failed` 中的任务是否有明确的失败原因记录

   **维度 2：迭代计划同步（docs/plan vs state.tasks）**
   - 读取 `docs/plan/01-迭代计划.md`，提取所有任务 ID（如 M1-01, M5-03 等）
   - 对比 `state.tasks.queue` + `state.tasks.completed` + `state.tasks.failed`
   - 找出差异：
     - Plan 中有但状态中没有的任务 → Warning
     - 状态中有但 Plan 中没有的任务 → Critical

   **维度 3：阶段状态验证（phase 合理性）**
   - 如果 `phase = "implement"` 但 `tasks.queue` 为空 → Critical
   - 如果 `phase = "requirement"` 但 `requirement.confirmed = true` → Warning
   - 如果 `phase = "spec"` 但 `spec.confirmed = true` → Warning

   **维度 4：Checkpoint 完整性**
   - 检查各阶段的 checkpoint 是否有必填字段
   - 检查 timestamp 是否过期（超过 7 天）
   - 检查 implement.checkpoint.attempt 是否接近 max_attempts

   **维度 5：文档存在性（核心修复点）**
   
   **5.1 需求文档检查：**
   - 检查 `requirement.doc` 指向的文件是否存在
   - 检查 `confirmed_docs` 中的需求文档是否存在
   
   **5.2 Spec 文档检查：**
   - 检查 `spec.spec_doc` 指向的文件是否存在
   - 检查 `confirmed_docs` 中的 Spec 文档是否存在
   
   **5.3 Verification 文档检查（重点）：**
   
   **步骤 1：检查 completed 任务的 verification 文档**
   - 遍历 `tasks.completed` 中的所有任务 ID
   - 对每个已完成任务，检查是否存在对应的 verification 文档：
     - 路径模式：`docs/verification/{iteration}/{task_id}-*.md`
     - 例如：M5-06 应该有 `docs/verification/M5/M5-06-*.md`
   - 如果缺失 → **Critical Issue**
   
   **步骤 2：智能检测 queue 中任务的实际完成状态**
   - 遍历 `tasks.queue` 中的所有任务 ID
   - 对每个待办任务，执行多维度检查判断是否实际已完成：
     
     **2.1 Git 分支检查：**
     - 检查是否存在已合并到 develop/main 的分支：`feature/{task_id}-*`
     - 如果分支已合并 → 任务可能已完成但未更新状态
     
     **2.2 代码文件检查：**
     - 根据任务描述推断应该创建的文件
     - 检查这些文件是否存在且非空
     - 例如：M5-01 "vue-i18n 依赖安装" → 检查 package.json 是否有 vue-i18n
     
     **2.3 Spec 文档关联检查：**
     - 读取 Plan 文档，获取任务对应的 Spec 文档
     - 检查 Spec 文档中定义的交付物是否存在
     
     **2.4 Verification 文档存在性：**
     - 检查是否存在 verification 文档
     - 如果 verification 文档存在但任务还在 queue → 强烈暗示任务已完成
   
   - **判定规则：**
     - 如果满足以下任一条件，判定为"实际已完成"：
       - ✅ Git 分支已合并 + verification 文档存在
       - ✅ Git 分支已合并 + 关键交付物文件存在
       - ✅ verification 文档存在 + 关键交付物文件存在
     - 判定结果 → **Critical Issue: 任务实际已完成但状态未同步**
   
   **步骤 3：检查 queue 任务的 verification 文档（正常情况）**
   - 对未被判定为"实际已完成"的 queue 任务：
     - 如果不存在 verification 文档 → **正常**（无需报告）
     - 如果存在 verification 文档 → **Suggestion**（可能是预创建的）
   
   **步骤 4：Verification 文档与 Plan 对齐检查（新增）**
   - 读取 `docs/plan/01-迭代计划.md`，提取当前迭代的所有任务定义
   - 遍历当前迭代的 completed 任务
   - 对每个任务执行对齐检查：
     
     **4.1 文档存在性：**
     - 检查是否存在 `docs/verification/{iteration}/{task_id}-*.md`
     - 如果缺失 → Critical Issue
     
      **4.2 内容完整性：**
      - 读取 verification 文档
      - 对比 Plan 中定义的交付物和验收标准
      - 检查是否包含所有必要的验证项
      
      **4.3 执行命令存在性检查（核心检查）：**
      - 检查 verification 文档中的每个测试用例是否包含"执行命令"字段
      - 如果测试用例只有描述没有执行命令 → **Critical**（无法实际运行验证）
      - 执行命令必须是有效的 shell 命令（如 `npm run build`、`mvn test`）
      
      **4.4 覆盖度计算：**
      - coverage = (verification 覆盖的验收标准数) / (Plan 定义的验收标准数)
      - coverage < 100% → Warning（部分覆盖）
      - coverage = 0% → Critical（完全未覆盖）
      
      **4.5 记录检查结果：**
      - 对齐的任务 → Success（不报告）
      - 不对齐的任务 → 根据严重程度分类

   **5.4 Plan 文档检查：**
   - 检查 `docs/plan/01-迭代计划.md` 是否存在
   - 检查当前迭代的任务是否在 Plan 中有定义

3. 分类问题：
   - **Critical**：必须修复（如状态文件损坏、verification 文档缺失）
   - **Warning**：建议修复（如任务队列不同步）
   - **Suggestion**：优化建议（如清理过期的 checkpoint）

4. 调用 `harness-state set-checkpoint doctor` 记录扫描结果
5. 调用 `harness-state add-message I 完成状态诊断，发现 N 个问题`
6. 返回详细诊断报告

**用法：** `/harness-doctor scan`

**输出示例：**
```
## 🔍 Harness 状态诊断报告

**扫描时间**: 2024-01-15 10:30:00
**当前阶段**: implement
**当前迭代**: M5

### ❌ Critical Issues (6) - 状态不同步

**检测到 queue 中的任务实际已完成，但状态未同步：**

1. M5-01: vue-i18n 依赖安装
   - 检测依据: Git 分支 feature/M5-01-vue-i18n 已合并 + package.json 包含 vue-i18n
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

2. M5-02: 翻译文件创建
   - 检测依据: locales/en.json 和 zh-CN.json 存在且非空
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

3. M5-03: i18n 配置
   - 检测依据: i18n/index.ts 和 useLocale.ts 文件存在
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

4. M5-04: LanguageSwitch 组件
   - 检测依据: LanguageSwitch.vue 组件存在 + Header 已集成
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

5. M5-05: 前台页面文本替换
   - 检测依据: HomeView/AboutView/ToolsView 等页面已使用 $t() 函数
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

6. M5-07: Admin Settings 语言设置页面
   - 检测依据: SettingsView 语言 Tab 已实现
   - 当前状态: tasks.queue
   - 建议状态: tasks.completed
   - 修复命令: `/harness-doctor fix`（自动同步）

### ⚠️ Warnings (3)

**Verification 文档与 Plan 对齐问题：**

1. M5-06 verification 文档内容不完整
   - Plan 定义的验收标准: 4 条
   - Verification 覆盖的验收标准: 2 条
   - 覆盖率: 50%
   - 缺失验证:
     - ❌ Element Plus 组件显示中文
     - ❌ 管理后台所有界面文本已翻译
   - 建议: 补充缺失的测试用例或重新生成文档

2. docs/plan 中有 M5-08，但 state.tasks 中没有
   - 建议: 调用 /harness-impl scan 同步任务列表

3. state.messages 数组有 150+ 条记录，建议清理旧日志
   - 建议: 调用 /harness-state reset 或手动清理

### ✅ Success (4)
以下任务的 verification 文档与 Plan 完全对齐：
- M5-01: vue-i18n 依赖安装（覆盖率 100%）
- M5-02: 翻译文件创建（覆盖率 100%）
- M5-04: LanguageSwitch 组件（覆盖率 100%）
- M5-05: 前台页面文本替换（覆盖率 100%）

### 📊 统计
- Critical (State Sync): 6
- Warnings (Alignment): 1
- Suggestions: 2
- Success (Aligned): 4
- Total Issues: 9

### 🔧 快速修复
运行以下命令自动修复所有问题：
```bash
/harness-doctor fix
```
这将：
1. 同步 6 个任务的状态（queue → completed）
2. 生成缺失的 verification 文档（M5-01, M5-03, M5-07）
3. 提示补充 M5-06 的 verification 文档
4. 清理其他可自动修复的问题
```

---

### /harness-doctor fix

自动修复发现的问题（保守模式）。

**动作：**
1. 调用 `harness-state get-checkpoint doctor` 获取上次扫描结果
2. 如果没有扫描结果，先执行 `/harness-doctor scan`
3. 自动修复**明确的**问题：

   **3.1 任务状态一致性修复：**
   - ✅ Git 分支已合并 → 移动任务从 queue 到 completed
   - ✅ docs/plan 有新任务 → 添加到 state.tasks.queue
   
   **3.2 阶段状态修复：**
   - ✅ phase 与 queue 不匹配 → 调整 phase
   
   **3.3 智能状态同步（核心修复）：**
   
   **场景 A：queue 中的任务实际已完成**
   - 检测条件（满足任一即可）：
     - Git 分支已合并 + verification 文档存在
     - Git 分支已合并 + 关键交付物文件存在
     - verification 文档存在 + 关键交付物文件存在
   
   - **自动修复动作：**
     1. 调用 `/harness-state complete-task <task_id>` 将任务从 queue 移到 completed
     2. 检查是否存在 verification 文档：
        - 如果不存在 → 调用 `/harness-doctor generate-missing-verifications` 生成
        - 如果存在 → 跳过
     3. 记录日志：`S 自动同步任务状态: {task_id} (queue → completed)`
   
   **场景 B：completed 任务缺少 verification 文档**
   - 检查任务是否有对应的 Spec 文档
   - 如果有 Spec → 调用 `/harness-doctor generate-missing-verifications` 生成
   - 如果没有 Spec → 提示需要先完成 Spec 阶段
   
   **3.4 Verification 文档完整性与对齐检查（新增）：**
   
   **步骤 1：读取 Plan 文档中的所有任务定义**
   - 解析 `docs/plan/01-迭代计划.md`
   - 提取当前迭代（从 state.iteration 获取）的所有任务
   - 获取每个任务的：
     - 任务 ID（如 M5-01）
     - 任务描述
     - 交付物列表
     - 验收标准
   
    **步骤 2：检查 verification 文档与 Plan 的对齐情况**
    - 遍历当前迭代的所有 completed 任务
    - 对每个任务执行对齐检查：
      
      **2.1 文档存在性检查：**
      - 检查是否存在 `docs/verification/{iteration}/{task_id}-*.md`
      - 如果缺失 → 记录为 Critical Issue
      
      **2.2 文档内容对齐检查：**
      - 读取 verification 文档内容
      - 对比 Plan 中定义的交付物和验收标准
      - 检查 verification 文档是否包含：
        - ✅ 所有 Plan 中定义的交付物验证
        - ✅ 所有 Plan 中定义的验收标准测试
        - ✅ 实际测试结果和截图（如果有）
      
      **2.3 测试代码存在性检查（核心检查）：**
      - 检查 verification 文档中引用的测试文件是否实际存在
      - 前端单元测试：`frontend/tests/unit/{task_id}*.test.ts`
      - 前端 E2E 测试：`frontend/tests/e2e/{task_id}*.spec.ts`
      - 后端测试：`backend/src/test/java/com/example/personalwebsite/{layer}/{Entity}Test.java`
      - 如果测试文件不存在 → **Critical**（测试代码缺失）
      
      **2.4 执行命令存在性检查：**
      - 检查 verification 文档中的每个测试用例是否包含"执行命令"字段
      - 如果测试用例只有描述没有执行命令 → **Critical**（无法实际运行验证）
      - 执行命令必须是有效的 shell 命令（如 `npm run build`、`mvn test`）
      
      **2.5 覆盖度检查：**
      - 计算 coverage = (verification 中覆盖的验收标准数) / (Plan 中定义的验收标准数)
      - 如果 coverage < 100% → Warning（部分覆盖）
      - 如果 coverage = 0% → Critical（完全未覆盖）
   
   **步骤 3：自动修复不对齐的问题**
   
   **场景 A：verification 文档缺失**
   - 调用 `/harness-doctor generate-missing-verifications` 生成
   - 基于 Plan 中的任务定义和 Spec 文档
   
    **场景 B：测试代码缺失但 verification 文档存在**
    - 检查 verification 中引用的测试文件是否存在
    - 如果不存在 → **Critical**（测试代码缺失）
    - 调用 `/harness-doctor generate-missing-tests` 生成测试代码
    
    **场景 C：verification 文档存在但内容不完整**
    - 分析缺失的部分：
      - 缺少哪些交付物的验证？
      - 缺少哪些验收标准的测试？
    - 生成补充内容建议：
      - 输出需要补充的测试用例列表
      - 提示用户手动补充或重新生成
    
    **场景 D：verification 文档与 Plan 完全对齐**
    - 记录为 Success
    - 无需修复

   **3.5 Verification 文档批量生成：**
   - 在执行完所有状态同步后
   - 调用 `/harness-doctor generate-missing-verifications` 一次性生成所有缺失的文档
   - 确保所有 completed 任务都有对应的 verification 文档

4. 调用 `harness-state set-checkpoint doctor` 记录修复结果
5. 调用 `harness-state add-message S 自动修复了 N 个问题`
6. 返回修复报告

**用法：** `/harness-doctor fix`

**输出示例：**
```
## 🔧 自动修复报告

**修复时间**: 2024-01-15 10:35:00

### ✅ Fixed (9)

#### 状态同步 (6)
1. M5-01: 从 queue 移动到 completed
   - 检测依据: Git 分支 feature/M5-01-vue-i18n 已合并 + verification 文档存在
   - 动作: 调用 /harness-state complete-task M5-01

2. M5-02: 从 queue 移动到 completed
   - 检测依据: Git 分支 feature/M5-02-locales 已合并 + locales/ 目录存在
   - 动作: 调用 /harness-state complete-task M5-02

3. M5-03: 从 queue 移动到 completed
   - 检测依据: i18n/index.ts 文件存在 + useLocale.ts 文件存在
   - 动作: 调用 /harness-state complete-task M5-03

4. M5-04: 从 queue 移动到 completed
   - 检测依据: LanguageSwitch.vue 组件存在 + Header 已集成
   - 动作: 调用 /harness-state complete-task M5-04

5. M5-05: 从 queue 移动到 completed
   - 检测依据: 前台页面已使用 $t() 函数
   - 动作: 调用 /harness-state complete-task M5-05

6. M5-07: 从 queue 移动到 completed
   - 检测依据: SettingsView 语言 Tab 已实现
   - 动作: 调用 /harness-state complete-task M5-07

#### Verification 文档生成 (3)
7. M5-01: 生成 verification 文档
   - 路径: docs/verification/M5/M5-01-vue-i18n依赖安装.md
   - 基于 Spec: docs/spec/11-国际化模块/11-01-语言切换功能-spec.md
   - Plan 对齐检查: ✅ 覆盖率 100%

8. M5-03: 生成 verification 文档
   - 路径: docs/verification/M5/M5-03-i18n配置.md
   - 基于 Spec: docs/spec/11-国际化模块/11-01-语言切换功能-spec.md
   - Plan 对齐检查: ✅ 覆盖率 100%

9. M5-07: 生成 verification 文档
   - 路径: docs/verification/M5/M5-07-Admin设置页面.md
   - 基于 Spec: docs/spec/11-国际化模块/11-01-语言切换功能-spec.md
   - Plan 对齐检查: ✅ 覆盖率 100%

#### Verification 文档对齐修复 (1)
10. M5-06: 补充 verification 文档内容
    - 原有覆盖率: 50% (2/4 验收标准)
    - 补充内容:
      - ✅ Element Plus 组件显示中文验证
      - ✅ 管理后台所有界面文本翻译检查
    - 修复后覆盖率: 100%
    - 动作: 追加测试用例到现有文档

### ⚠️ Skipped (0)
无跳过的问题，所有问题已自动修复。

### ✅ Alignment Summary
Verification 文档与 Plan 对齐情况：
- M5-01: ✅ 100% 对齐
- M5-02: ✅ 100% 对齐
- M5-03: ✅ 100% 对齐（新生成）
- M5-04: ✅ 100% 对齐
- M5-05: ✅ 100% 对齐
- M5-06: ✅ 100% 对齐（已补充）
- M5-07: ✅ 100% 对齐（新生成）

### 💡 Suggestions
1. state.messages 有 150+ 条记录，可考虑清理
   - 命令: `/harness-state reset`（会清空所有消息）

### 📊 统计
- State Synced: 6
- Verifications Generated: 3
- Verifications Aligned: 1
- Skipped: 0
- Total Issues Fixed: 10
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

### /harness-doctor generate-missing-verifications

自动生成缺失的 verification 文档（基于 Spec 文档）。

**适用场景：**
- 多个已完成任务缺少 verification 文档
- 需要批量生成验证文档

**重要更新**：生成的 verification 文档必须包含**实际可执行的命令**，而非静态检查描述。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 遍历 `tasks.completed` 中的所有任务
3. 对每个任务检查是否存在 verification 文档
4. 如果缺失：
   - 查找对应的 Spec 文档（从 plan 或 state 中获取）
   - 根据 Spec 中的验收标准生成 verification 文档
   - **必须包含执行命令**（根据交付物类型推断）：
     - 前端代码 → `cd frontend && npm run build` + `npm run test:e2e`
     - 后端代码 → `cd backend && mvn test`
     - 组件 → `cd frontend && npm run test:e2e -- --grep "<组件名>"`
   - 保存到 `docs/verification/{iteration}/{task_id}-{描述}.md`
   - 调用 `harness-state add-message I 生成 verification 文档: {task_id}`
5. 返回生成报告

**Verification 文档生成格式要求**：

```markdown
### TC-01: {测试名称}

**执行命令**:
```bash
cd {frontend|backend} && {实际命令}
```

**预期结果**:
- 退出码 = 0
- 输出包含成功标志
```

**用法：** `/harness-doctor generate-missing-verifications`

**输出示例：**
```
## 📝 Verification 文档生成报告

**生成时间**: 2024-01-15 10:40:00

### ✅ Generated (6)
1. M5-01: docs/verification/M5/M5-01-vue-i18n依赖安装.md
   - 基于 Spec: docs/spec/11-国际化模块/11-01-语言切换功能-spec.md
   - 验收标准: 3 条 → 测试用例: 3 个

2. M5-02: docs/verification/M5/M5-02-翻译文件创建.md
   - 基于 Spec: docs/spec/11-国际化模块/11-01-语言切换功能-spec.md
   - 验收标准: 2 条 → 测试用例: 2 个

... (M5-03, M5-04, M5-05, M5-07 同样生成)

### ⚠️ Skipped (1)
1. M5-06: verification 文档已存在
   - 路径: docs/verification/M5/M5-06-管理后台文本替换.md

### 📊 统计
- Generated: 6
- Skipped: 1
- Total Completed Tasks: 7
```

**注意：**
- 此命令会覆盖现有的 verification 文档（如果有）
- 生成的文档可能需要手动调整测试细节
- 建议生成后运行 `/harness-impl verify <task_id>` 进行实际验证

---

### /harness-doctor generate-missing-tests

自动生成缺失的测试代码文件（基于 verification 文档）。

**适用场景：**
- verification 文档存在但测试代码文件缺失
- 需要批量生成测试代码

**动作：**
1. 调用 `harness-state get` 读取状态
2. 遍历 `tasks.completed` 中的所有任务
3. 对每个任务检查 verification 文档中引用的测试文件是否存在
4. 如果测试文件缺失：
   - 读取对应的 verification 文档
   - 分析测试用例类型（单元测试/E2E）
   - 生成测试代码文件：
     - 前端单元测试：`frontend/tests/unit/{task_id}.test.ts`
     - 前端 E2E 测试：`frontend/tests/e2e/{task_id}.spec.ts`
     - 后端测试：`backend/src/test/java/.../{task_id}Test.java`
   - 保存测试文件
   - 调用 `harness-state add-message I 生成测试代码: {task_id}`
5. 返回生成报告

**测试代码生成格式要求**：

```typescript
// 单元测试示例
import { describe, it, expect } from 'vitest';

describe('{TaskName}', () => {
  it('should {expected behavior}', async () => {
    // Arrange
    // Act
    // Assert
  });
});
```

```typescript
// E2E 测试示例
import { test, expect } from '@playwright/test';

test.describe('{ComponentName}', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should {expected behavior}', async ({ page }) => {
    // Arrange
    // Act
    // Assert
  });
});
```

**用法：** `/harness-doctor generate-missing-tests`

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
