---
name: harness-spec
description: >
  Harness Spec Skills - Spec 生成与确认技能。
  负责人工或自动触发的规格文档生成循环：分析需求 → 生成 Spec → 请求确认 → 确认通过。
  支持断点继续，通过 `harness-state` 技能管理状态恢复。
---

# Harness Spec Skills

Spec 生成与确认技能，负责根据需求文档生成技术规格文档。

## 状态结构

通过 `harness-state` 技能管理以下状态：

```json
{
  "spec": {
    "requirement_doc": "docs/requirements/03-博客管理模块/03-01-博客列表.md",
    "spec_doc": "docs/spec/03-博客管理模块/03-01-博客列表-spec.md",
    "confirmed": false,
    "iteration": "M1",
    "checkpoint": {
      "step": "analyze|generate|request|confirm|done",
      "clarify_count": 0,
      "last_question": null
    }
  }
}
```

---

## 命令

### /harness-spec start `<requirement_doc>`

开始或恢复 Spec 生成。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 检查是否已确认，如果是则报错
3. 确定 Spec 输出路径（根据规范从 requirement 路径推断）
4. 调用 `harness-state set phase spec`
5. 调用 `harness-state set-checkpoint spec` 设置文档路径和 step=analyze
6. 调用 `harness-state add-message I 开始生成 Spec：<requirement_doc>`
7. 返回开始信息

**用法：** `/harness-spec start docs/requirements/03-博客管理模块/03-01-博客列表.md`

---

### /harness-spec analyze

分析需求文档，提取关键信息。

**动作：**
1. 调用 `harness-state get-checkpoint spec` 获取 requirement_doc
2. 读取需求文档内容，解析：
   - 功能概述
   - 页面布局（如有）
   - 功能详情（字段、规则）
   - 验收标准
3. 调用 `harness-state set-checkpoint spec` 更新 step=analyze
4. 调用 `harness-state add-message I 分析需求文档完成`
5. 返回分析结果

**用法：** `/harness-spec analyze`

---

### /harness-spec generate `<content>`

生成或更新 Spec 文档。

**动作：**
1. 使用 `write` 工具创建/覆盖文件
2. 调用 `harness-state get` 读取状态
3. 调用 `harness-state set-checkpoint spec` 更新 step=generate
4. 调用 `harness-state add-message I 生成 Spec 文档`
5. 返回生成结果

**用法：** `/harness-spec generate # 博客列表规格文档\n\n## 1. 概述...`

---

### /harness-spec generate-from-template

使用模板生成 Spec 文档。

**动作：**
1. 读取 `docs/templates/spec-template.md`
2. 读取需求文档
3. 填充模板
4. 写入 Spec 文件
5. 调用 `harness-state set-checkpoint spec` 更新状态
6. 调用 `harness-state add-message I 使用模板生成 Spec`
7. 返回生成的文档预览

**用法：** `/harness-spec generate-from-template`

---

### /harness-spec clarify `<question>`

向用户提问以澄清技术细节。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state add-message Q <question>` 记录问题
3. 调用 `harness-state set-checkpoint spec` 更新 `last_question` 和 `clarify_count`
4. 向用户提问

**用法：** `/harness-spec clarify 是否需要支持草稿状态？`

---

### /harness-spec request

请求用户确认 Spec 文档。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证 `spec.spec_doc` 已设置
3. 调用 `harness-state set-checkpoint spec` 更新 step=request
4. 调用 `harness-state add-message I 请求确认 Spec 文档`
5. 提示用户确认

**用法：** `/harness-spec request`

---

### /harness-spec confirm

确认 Spec 文档已通过。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 验证当前 step 必须是 "request"
3. 调用 `harness-state set-checkpoint spec` 更新：
   - `confirmed` = true
   - `step` = "done"
   - `timestamp` = 当前时间
4. 调用 `harness-state confirm-doc <spec_doc>`
5. 调用 `harness-state add-message S Spec 文档已确认`
6. 返回确认结果

**用法：** `/harness-spec confirm`

---

### /harness-spec link-requirement `<requirement_doc>`

关联需求文档（当 Spec 涉及多个需求时）。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 调用 `harness-state set-checkpoint spec` 更新 `requirement_doc`
3. 调用 `harness-state add-message I 关联需求文档：<requirement_doc>`
4. 返回更新结果

**用法：** `/harness-spec link-requirement docs/requirements/03-博客管理模块/03-02-博客详情.md`

---

### /harness-spec status

查看当前 Spec 生成状态。

**动作：**
1. 调用 `harness-state get-checkpoint spec` 读取 checkpoint
2. 返回摘要：
   - 关联需求文档
   - Spec 输出路径
   - 确认状态
   - 当前步骤
   - 下一步建议

**用法：** `/harness-spec status`

---

### /harness-spec reset

重置 Spec 生成状态。

**动作：**
1. 调用 `harness-state reset-phase spec`
2. 调用 `harness-state add-message I Spec 生成状态已重置`
3. 返回重置结果

**用法：** `/harness-spec reset`

---

## 工作流程

### 人工触发流程

```
用户: /harness-spec start docs/requirements/03-博客管理模块/03-01-博客列表.md
AI:   /harness-spec analyze
AI:   /harness-spec generate-from-template
AI:   /harness-spec clarify 技术细节...
AI:   /harness-spec generate <更新后的内容>
AI:   /harness-spec request
用户: /harness-spec confirm
```

### 自动恢复流程

当主技能 `harness` 扫描到 `spec.confirmed = false` 时：
1. 调用 `harness-state get-checkpoint spec`
2. 根据 `checkpoint.step` 继续执行

---

## 规范要求

- Spec 文档必须引用对应的 Requirement
- 不得在 Spec 中重复业务需求描述
- 验收标准必须完整且可测试
- 分支策略参照：[docs/spec/06-项目规范/05-分支规范.md](../../spec/06-项目规范/05-分支规范.md)