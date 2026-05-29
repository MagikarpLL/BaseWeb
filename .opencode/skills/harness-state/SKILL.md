---
name: harness-state
description: >
  Harness 状态管理技能 - 负责状态文件的读写操作。
  提供统一的状态读取、写入、更新、消息记录接口。
  所有其他 harness 子技能应通过此技能管理状态。
---

# Harness State Skills

状态管理技能，负责 `.harness/state.json` 的读写操作。

## 状态文件结构

完整结构定义在 `harness` 主技能中：

```json
{
  "version": "1.0",
  "phase": "idle|requirement|spec|implement|paused|completed",
  "iteration": "M1",
  "current_task": null,
  "last_session_id": null,
  "checkpoints": {
    "requirement": { 
      "doc": null, 
      "confirmed": false, 
      "step": null, 
      "clarify_count": 0,
      "last_question": null,
      "timestamp": null 
    },
    "spec": { 
      "requirement_doc": null, 
      "spec_doc": null, 
      "confirmed": false, 
      "step": null,
      "clarify_count": 0,
      "last_question": null,
      "timestamp": null 
    },
    "implement": { 
      "iteration": null, 
      "task_id": null, 
      "step": "scan|implement|verify|commit|done|paused",
      "attempt": 0, 
      "max_attempts": 5,
      "last_error": null,
      "verification_result": null,
      "timestamp": null 
    },
    "doctor": {
      "last_scan": null,
      "issues_found": 0,
      "issues_fixed": 0,
      "report": {
        "critical": [],
        "warnings": [],
        "suggestions": []
      },
      "step": "scan|analyze|fix|done",
      "timestamp": null
    }
  },
  "tasks": { 
    "queue": [], 
    "completed": [], 
    "failed": [] 
  },
  "pending_docs": [],
  "confirmed_docs": [],
  "messages": [],
  "pause_reason": null,
  "last_activity": "ISO timestamp"
}
```

**重要变更：**
- **移除了独立的 [verify](file://f:\Workspace\VibeCoding\personal-painpoints\Base\frontend\src\components\blog\CaptchaInput.vue#L89-L93) 阶段**：验证现在是 `implement` 阶段的内部闭环
- **新增 `pause_reason` 字段**：用于区分暂停原因（`user_requested` / `auto_failed` / `manual_review`）
- **增强 `implement.checkpoint`**：包含 `verification_result` 和 `last_error` 字段，支持验证闭环
- **新增 `doctor` checkpoint**：用于状态诊断与修复的结果记录

---

## 命令

### /harness-state help

显示帮助信息，列出所有可用命令和用法。

**动作：**
1. 返回技能概述和所有命令列表

**输出示例：**
```
# 📖 harness-state 技能帮助

## 技能概述
状态管理技能，负责 `.harness/state.json` 的读写操作，提供统一的状态读取、写入、更新、消息记录接口。

## 可用命令

### 1. /harness-state help
显示此帮助信息

### 2. /harness-state get
读取完整状态文件
**示例**: `/harness-state get`

### 3. /harness-state init
初始化状态文件（仅当文件不存在时）
**示例**: `/harness-state init`

### 4. /harness-state set <field_path> <value>
设置指定字段的值
**示例**: `/harness-state set phase implement`

### 5. /harness-state update <json_patch>
使用 JSON Patch 更新状态
**示例**: `/harness-state update [{"op":"replace","path":"/phase","value":"spec"}]`

### 6. /harness-state add-message <type> <content>
添加消息到 messages 数组
**示例**: `/harness-state add-message I 开始实现任务`

### 7. /harness-state get-phase
获取当前阶段
**示例**: `/harness-state get-phase`

### 8. /harness-state get-checkpoint <phase>
获取指定阶段的 checkpoint
**示例**: `/harness-state get-checkpoint implement`

### 9. /harness-state set-checkpoint <phase> <json_checkpoint>
设置指定阶段的 checkpoint
**示例**: `/harness-state set-checkpoint requirement {"step":"clarify"}`

### 10. /harness-state reset
重置状态文件
**示例**: `/harness-state reset`

### 11. /harness-state reset-phase <phase>
重置指定阶段的状态
**示例**: `/harness-state reset-phase implement`

### 12. /harness-state add-pending <doc_path>
添加文档到待处理列表
**示例**: `/harness-state add-pending docs/requirements/xxx.md`

### 13. /harness-state confirm-doc <doc_path>
确认文档（从待处理移到已确认）
**示例**: `/harness-state confirm-doc docs/requirements/xxx.md`

### 14. /harness-state complete-task <task_id>
标记任务为完成
**示例**: `/harness-state complete-task M1-09`

### 15. /harness-state fail-task <task_id>
标记任务为失败
**示例**: `/harness-state fail-task M1-09`

### 16. /harness-state set-pause-reason <reason>
设置暂停原因
**示例**: `/harness-state set-pause-reason auto_failed`

### 17. /harness-state is-valid
检查状态文件是否有效
**示例**: `/harness-state is-valid`

## 快速开始

```bash
# 读取状态
/harness-state get

# 设置阶段
/harness-state set phase implement
```
```

**用法：** `/harness-state help`

---

### /harness-state get

读取完整状态文件。

**动作：**
1. 读取 `.harness/state.json`
2. 返回完整状态 JSON

**用法：** `/harness-state get`

---

### /harness-state init

初始化状态文件（仅当文件不存在时）。

**动作：**
1. 检查 `.harness/state.json` 是否存在
2. 如果不存在，创建默认状态
3. 如果已存在，返回错误
4. 返回初始化结果

**用法：** `/harness-state init`

---

### /harness-state set `<field_path>` `<value>`

设置指定字段的值。

**动作：**
1. 读取当前状态
2. 使用 dot notation 设置 `field_path` 的值
3. 写回状态文件
4. 返回更新结果

**字段路径：**
- `phase` - 当前阶段（枚举：`idle`, `requirement`, `spec`, `implement`, `paused`, `completed`）
- `iteration` - 当前迭代
- `current_task` - 当前任务
- `last_session_id` - 最后 session ID
- `pause_reason` - 暂停原因（`user_requested` / `auto_failed` / `manual_review`）
- `checkpoints.requirement.step` - requirement 阶段步骤
- `checkpoints.requirement.doc` - requirement 文档路径
- `checkpoints.spec.step` - spec 阶段步骤
- `checkpoints.implement.step` - implement 阶段步骤（`scan`/`implement`/`verify`/`commit`/`done`/`paused`）
- `checkpoints.implement.attempt` - 修复尝试次数
- `checkpoints.implement.verification_result` - 验证结果（`passed`/`failed`/`null`）
- `checkpoints.implement.last_error` - 最后一次错误描述
- 等等...

**用法：**
- `/harness-state set phase implement`
- `/harness-state set checkpoints.implement.step verify`
- `/harness-state set pause_reason auto_failed`

---

### /harness-state update `<json_patch>`

使用 JSON Patch 更新状态。

**动作：**
1. 读取当前状态
2. 应用 JSON Patch（RFC 6902）更新
3. 写回状态文件
4. 返回更新结果

**JSON Patch 示例：**
```json
[
  { "op": "replace", "path": "/phase", "value": "implement" },
  { "op": "replace", "path": "/pause_reason", "value": "auto_failed" },
  { "op": "add", "path": "/messages/-", "value": { "time": "2024-01-15T10:00:00Z", "type": "I", "content": "开始实现" } }
]
```

**用法：** `/harness-state update [{"op":"replace","path":"/phase","value":"implement"}]`

---

### /harness-state add-message `<type>` `<content>`

添加消息到 messages 数组。

**动作：**
1. 读取当前状态
2. 添加消息对象：`{ "time": "ISO timestamp", "type": "<type>", "content": "<content>" }`
3. 写回状态
4. 返回添加结果

**消息类型：**
- `I` - Info（信息）
- `Q` - Question（提问）
- `A` - Answer（回答）
- `W` - Warning（警告）
- `E` - Error（错误）
- `S` - Success（成功）

**用法：**
- `/harness-state add-message I 开始实现任务 M1-09`
- `/harness-state add-message Q 博客需要支持哪些功能？`
- `/harness-state add-message E 自动修复失败，超过最大尝试次数`

---

### /harness-state get-phase

获取当前阶段。

**动作：**
1. 读取状态
2. 返回 `phase` 字段值

**用法：** `/harness-state get-phase`

---

### /harness-state get-checkpoint `<phase>`

获取指定阶段的 checkpoint。

**动作：**
1. 读取状态
2. 返回 `checkpoints.<phase>` 内容

**用法：**
- `/harness-state get-checkpoint requirement`
- `/harness-state get-checkpoint implement`

---

### /harness-state set-checkpoint `<phase>` `<json_checkpoint>`

设置指定阶段的 checkpoint。

**动作：**
1. 读取状态
2. 替换 `checkpoints.<phase>` 为新值
3. 写回状态
4. 返回更新结果

**用法：** 
- `/harness-state set-checkpoint requirement {"step":"clarify","clarify_count":1}`
- `/harness-state set-checkpoint implement {"step":"verify","verification_result":"failed","last_error":"编译错误"}`

---

### /harness-state reset

重置状态文件（保留 version 和基本结构）。

**动作：**
1. 读取状态
2. 重置为初始状态：
   - `phase` = "idle"
   - `iteration` = null
   - `current_task` = null
   - `pause_reason` = null
   - `checkpoints` 各阶段置 null
   - `tasks` 清空
   - `pending_docs` 清空
   - `confirmed_docs` 清空
   - `messages` 清空
3. 写回状态
4. 返回重置结果

**用法：** `/harness-state reset`

---

### /harness-state reset-phase `<phase>`

重置指定阶段的状态。

**动作：**
1. 读取状态
2. 重置 `checkpoints.<phase>` 为初始值
3. 如果是 `requirement`/`spec`，同时重置 `confirmed` 为 false
4. 写回状态

**用法：** 
- `/harness-state reset-phase implement`
- `/harness-state reset-phase doctor`

---

### /harness-state add-pending `<doc_path>`

添加文档到待处理列表。

**动作：**
1. 读取状态
2. 如果 `doc_path` 不在 `pending_docs` 中，添加
3. 写回状态

**用法：** `/harness-state add-pending docs/requirements/03-博客管理模块/03-01-博客列表.md`

---

### /harness-state confirm-doc `<doc_path>`

确认文档（从待处理移到已确认）。

**动作：**
1. 读取状态
2. 从 `pending_docs` 移除
3. 添加到 `confirmed_docs`
4. 写回状态

**用法：** `/harness-state confirm-doc docs/requirements/03-博客管理模块/03-01-博客列表.md`

---

### /harness-state complete-task `<task_id>`

标记任务为完成。

**动作：**
1. 读取状态
2. 从 `tasks.queue` 移除
3. 添加到 `tasks.completed`
4. 写回状态

**用法：** `/harness-state complete-task M1-09`

---

### /harness-state fail-task `<task_id>`

标记任务为失败。

**动作：**
1. 读取状态
2. 从 `tasks.queue` 移除
3. 添加到 `tasks.failed`
4. 写回状态

**用法：** `/harness-state fail-task M1-09`

---

### /harness-state set-pause-reason `<reason>`

设置暂停原因。

**动作：**
1. 读取状态
2. 设置 `pause_reason` 字段
3. 写回状态

**暂停原因枚举：**
- `user_requested`: 用户主动暂停
- `auto_failed`: 自动修复失败（超过最大尝试次数）
- `manual_review`: 需要人工审核

**用法：** 
- `/harness-state set-pause-reason user_requested`
- `/harness-state set-pause-reason auto_failed`

---

### /harness-state is-valid

检查状态文件是否有效。

**动作：**
1. 读取状态
2. 验证必要字段存在
3. 验证 phase 值为有效枚举（`idle`, `requirement`, `spec`, `implement`, `paused`, `completed`）
4. 返回验证结果

**用法：** `/harness-state is-valid`

---

## 子技能调用约定

其他 harness 子技能调用状态管理：

```
task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-state"],
  prompt="读取当前状态: get"
)

task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-state"],
  prompt="更新状态: update [{\"op\":\"replace\",\"path\":\"/phase\",\"value\":\"implement\"}]"
)

task(
  category="unspecified-high",
  session_id="{session_id}",
  load_skills=["harness-state"],
  prompt="设置暂停原因: set-pause-reason auto_failed"
)
```

---

## 注意事项

- 状态文件路径：`.harness/state.json`
- 读取优先：任何状态变更前必须先读取
- 原子写入：更新后立即写回
- 时间戳格式：ISO 8601
- **阶段枚举（已更新）**：`idle`, `requirement`, `spec`, `implement`, `paused`, `completed`（移除了 [verify](file://f:\Workspace\VibeCoding\personal-painpoints\Base\frontend\src\components\blog\CaptchaInput.vue#L89-L93））
- **暂停原因枚举**：`user_requested`, `auto_failed`, `manual_review`
- `implement` 阶段的 [verify](file://f:\Workspace\VibeCoding\personal-painpoints\Base\frontend\src\components\blog\CaptchaInput.vue#L89-L93) 步骤是内部闭环，不提升为独立阶段
- **doctor checkpoint**：用于记录诊断结果，不属于 phase 枚举
