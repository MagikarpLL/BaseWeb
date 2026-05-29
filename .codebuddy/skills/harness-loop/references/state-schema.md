# 状态文件 Schema 定义

> Harness Loop 的状态文件 `docs/.harness-state.json` 结构定义

## 完整结构

```json
{
  "version": 1,
  "project": "personal-website",
  "current_phase": "require|spec|implement|verify|done",
  "current_milestone": "M1|M2|M3|M4",
  "current_task": "M1-06",
  "completed_tasks": ["M1-01", "M1-02"],
  "phase_progress": {
    "require": {
      "status": "completed|in_progress|pending",
      "items": [
        {
          "id": "req-001",
          "description": "用户认证功能",
          "confirmed": true,
          "doc_path": "docs/requirements/04-用户与认证模块/04-01-用户认证.md"
        }
      ],
      "confirmed_items": 3,
      "total_items": 3
    },
    "spec": {
      "status": "completed|in_progress|pending",
      "specs_generated": [
        "docs/spec/04-认证与安全.md"
      ],
      "specs_total": 2,
      "confirmed": true
    },
    "implement": {
      "status": "completed|in_progress|pending",
      "task_progress": {
        "M1-06": {
          "sub_steps": ["Controller", "Service", "JwtUtil"],
          "completed_sub_steps": ["Controller", "Service"],
          "current_sub_step": "JwtUtil"
        }
      }
    },
    "verify": {
      "status": "completed|in_progress|pending",
      "results": [
        {
          "task": "M1-06",
          "acceptance_criteria": [
            { "item": "正确用户名密码可以登录成功", "status": "pass" },
            { "item": "错误用户名密码返回错误提示", "status": "fail", "reason": "返回500而非401" }
          ]
        }
      ],
      "verified_items": 5,
      "total_items": 8
    }
  },
  "blocked_reason": "",
  "last_updated": "2026-05-28T22:30:00Z",
  "history": [
    {
      "timestamp": "2026-05-28T22:00:00Z",
      "phase": "require",
      "action": "confirmed requirement req-001",
      "milestone": "M1"
    }
  ]
}
```

## 字段说明

### 顶层字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `version` | number | 是 | Schema 版本，当前为 1 |
| `project` | string | 是 | 项目名称 |
| `current_phase` | string | 是 | 当前阶段：require/spec/implement/verify/done |
| `current_milestone` | string | 是 | 当前迭代编号 |
| `current_task` | string | 否 | 当前正在处理的任务编号 |
| `completed_tasks` | string[] | 是 | 已完成的任务列表 |
| `phase_progress` | object | 是 | 各阶段详细进度 |
| `blocked_reason` | string | 是 | 阻塞原因，空字符串表示无阻塞 |
| `last_updated` | string | 是 | ISO 8601 时间戳 |
| `history` | array | 否 | 操作历史记录 |

### phase_progress 各阶段字段

#### require 阶段

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | pending / in_progress / completed |
| `items` | array | 需求条目列表 |
| `items[].id` | string | 需求唯一标识 |
| `items[].description` | string | 需求简述 |
| `items[].confirmed` | boolean | 是否已确认 |
| `items[].doc_path` | string | 需求文档路径 |
| `confirmed_items` | number | 已确认条数 |
| `total_items` | number | 总条数 |

#### spec 阶段

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | pending / in_progress / completed |
| `specs_generated` | string[] | 已生成的 Spec 文件路径 |
| `specs_total` | number | 需要生成的 Spec 总数 |
| `confirmed` | boolean | 用户是否已全部确认 |

#### implement 阶段

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | pending / in_progress / completed |
| `task_progress` | object | 任务子步骤进度 |
| `task_progress[taskId].sub_steps` | string[] | 子步骤列表 |
| `task_progress[taskId].completed_sub_steps` | string[] | 已完成的子步骤 |
| `task_progress[taskId].current_sub_step` | string | 当前子步骤 |

#### verify 阶段

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | pending / in_progress / completed |
| `results` | array | 验收结果列表 |
| `results[].task` | string | 任务编号 |
| `results[].acceptance_criteria` | array | 验收标准结果 |
| `results[].acceptance_criteria[].item` | string | 验收项描述 |
| `results[].acceptance_criteria[].status` | string | pass / fail / skip |
| `results[].acceptance_criteria[].reason` | string | 失败原因（仅 fail 时） |
| `verified_items` | number | 已验证条数 |
| `total_items` | number | 总条数 |
