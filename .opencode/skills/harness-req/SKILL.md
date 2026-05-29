---
name: harness-req
description: >
  Harness Requirement Skills - 需求确认循环技能。
  负责人工或自动触发的需求确认循环：提问 → 更新需求文档 → 请求确认 → 确认通过。
  支持断点继续，通过 `harness-state` 技能管理状态恢复。
---

# Harness Requirement Skills

需求确认循环技能，负责与用户反复确认需求，更新需求文档。

## 状态结构

通过 `harness-state` 技能管理以下状态：

```json
{
  "requirement": {
    "doc": "docs/requirements/03-博客管理模块/03-01-博客列表.md",
    "confirmed": false,
    "iteration": "M1",
    "checkpoint": {
      "step": "clarify|draft|request|confirm|done",
      "clarify_count": 0,
      "last_question": null
    }
  }
}
```

---

## 命令

### /harness-req start `<doc_path>`

开始或恢复需求确认。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 检查 `requirement.checkpoint.step` 是否为 `done`，如果是则报错
3. 调用 `harness-state set phase requirement`
4. 调用 `harness-state set-checkpoint requirement` 设置 doc 和 step=clarify
5. 调用 `harness-state add-message I 开始需求确认：<doc_path>`
6. 返回开始信息

**用法：** `/harness-req start docs/requirements/03-博客管理模块/03-01-博客列表.md`

---

### /harness-req clarify `<question>`

向用户提问以澄清需求。

**动作：**
1. 调用 `harness-state get` 读取状态，检查当前 step
2. 调用 `harness-state add-message Q <question>` 记录问题
3. 调用 `harness-state set-checkpoint requirement` 更新 `last_question` 和 `clarify_count`
4. 向用户提问

**用法：** `/harness-req clarify 博客需要支持哪些功能？`

---

### /harness-req draft `<doc_path>` `<content>`

根据讨论结果起草或更新需求文档。

**动作：**
1. 使用 `write` 工具创建/覆盖文件
2. 调用 `harness-state get` 读取状态
3. 调用 `harness-state set-checkpoint requirement` 更新 doc 和 step=draft
4. 调用 `harness-state add-message I 起草需求文档：<doc_path>`
5. 返回起草结果

**用法：** `/harness-req draft docs/requirements/03-博客管理模块/03-01-博客列表.md <content>`

---

### /harness-req request

请求用户确认当前需求文档。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证 `requirement.doc` 已设置
3. 调用 `harness-state set-checkpoint requirement` 更新 step=request
4. 调用 `harness-state add-message I 请求确认需求文档`
5. 提示用户确认

**用法：** `/harness-req request`

---

### /harness-req confirm

确认需求文档已通过。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证 `requirement.checkpoint.step` 必须是 "request"
3. 调用 `harness-state set-checkpoint requirement` 更新：
   - `confirmed` = true
   - `step` = "done"
   - `timestamp` = 当前时间
4. 调用 `harness-state confirm-doc <doc_path>` 移动文档到已确认
5. 调用 `harness-state add-message S 需求文档已确认`
6. 返回确认结果

**用法：** `/harness-req confirm`

---

### /harness-req status

查看当前需求确认状态。

**动作：**
1. 调用 `harness-state get-checkpoint requirement` 读取 checkpoint
2. 返回摘要：
   - 当前文档
   - 确认状态
   - 当前步骤
   - 提问数量
   - 下一步建议

**用法：** `/harness-req status`

---

### /harness-req reset

重置需求确认状态（用于重新开始）。

**动作：**
1. 调用 `harness-state reset-phase requirement`
2. 调用 `harness-state add-message I 需求确认状态已重置`
3. 返回重置结果

**用法：** `/harness-req reset`

---

## 工作流程

### 人工触发流程

```
用户: /harness-req start docs/requirements/03-博客管理模块/03-01-博客列表.md
AI:   /harness-req clarify 博客需要支持哪些功能？
用户: /harness-req clarify 需要支持列表、详情、编辑...
AI:   /harness-req draft docs/requirements/03-博客管理模块/03-01-博客列表.md <content>
AI:   /harness-req request
用户: /harness-req confirm
```

### 自动恢复流程

当主技能 `harness` 扫描到 `requirement.confirmed = false` 时：
1. 调用 `harness-state get-checkpoint requirement`
2. 根据 `checkpoint.step` 继续执行

---

## 注意事项

- 所有文件路径相对于项目根目录
- 状态变更通过 `harness-state` 技能执行
- 确认后的文档移动/归档由主技能处理
- 分支策略参照：[docs/spec/06-项目规范/05-分支规范.md](../../spec/06-项目规范/05-分支规范.md)