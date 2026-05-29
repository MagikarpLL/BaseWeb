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

### /harness-req help

显示帮助信息，列出所有可用命令和用法。

**动作：**
1. 返回技能概述和所有命令列表
2. 每个命令包含：名称、参数说明、简要描述、示例

**输出示例：**
```
# 📖 harness-req 技能帮助

## 技能概述
需求确认循环技能，负责与用户反复确认需求，更新需求文档。

## 可用命令

### 1. /harness-req help
显示此帮助信息

### 2. /harness-req start <doc_path>
开始或恢复需求确认（需要提供现有文档路径）
**示例**: `/harness-req start docs/requirements/01-博客列表.md`

### 3. /harness-req start-from-idea <brief_idea>
从一句话需求开始澄清循环（自动创建文档）
**示例**: `/harness-req start-from-idea "我想做一个博客系统"`

### 4. /harness-req clarify <question>
向用户提问以澄清需求
**示例**: `/harness-req clarify 博客需要支持哪些功能？`

### 5. /harness-req draft <doc_path> <content>
根据讨论结果起草或更新需求文档
**示例**: `/harness-req draft docs/requirements/01-博客列表.md "# 博客列表需求..."`

### 6. /harness-req request
请求用户确认当前需求文档
**示例**: `/harness-req request`

### 7. /harness-req confirm
确认需求文档已通过
**示例**: `/harness-req confirm`

### 8. /harness-req status
查看当前需求确认状态
**示例**: `/harness-req status`

### 9. /harness-req reset
重置需求确认状态
**示例**: `/harness-req reset`

## 快速开始

**场景 1：已有需求文档**
```bash
/harness-req start docs/requirements/xxx.md
```

**场景 2：只有一句话想法**
```bash
/harness-req start-from-idea "我想做一个博客系统"
```
```

**用法：** `/harness-req help`

---

### /harness-req start `<doc_path>`

开始或恢复需求确认（需要提供文档路径）。

**动作：**
1. 调用 `harness-state get` 读取状态
2. 检查 `requirement.checkpoint.step` 是否为 `done`，如果是则报错
3. 调用 `harness-state set phase requirement`
4. 调用 `harness-state set-checkpoint requirement` 设置 doc 和 step=clarify
5. 调用 `harness-state add-message I 开始需求确认：<doc_path>`
6. 返回开始信息

**用法：** `/harness-req start docs/requirements/03-博客管理模块/03-01-博客列表.md`

---

### /harness-req start-from-idea `<brief_idea>`

从一句话需求开始需求澄清循环（自动创建文档）。

**适用场景：**
- 你只有一个模糊的想法，如"我想做一个博客系统"
- 没有现成的需求文档
- 需要 AI 通过反复提问来完善需求

**动作：**
1. 调用 `harness-state get` 读取状态
2. **自动生成需求文档路径**：
   - 根据想法内容推断模块名称
   - 创建路径：`docs/requirements/auto-generated/YYYYMMDD-HHMMSS-需求.md`
   - 例如：`docs/requirements/auto-generated/20240115-103000-博客系统.md`
3. **创建初始需求文档**：
   - 写入用户的原始想法作为概述
   - 标记为"待澄清"状态
4. 调用 `harness-state set phase requirement`
5. 调用 `harness-state set-checkpoint requirement` 设置 doc 和 step=clarify
6. 调用 `harness-state add-pending <doc_path>` 添加到待处理列表
7. 调用 `harness-state add-message I 从想法开始需求澄清：<brief_idea>`
8. **立即进入澄清循环**：
   - AI 分析想法的关键点
   - 提出第一个澄清问题
   - 调用 `/harness-req clarify <question>`

**用法：** 
``bash
/harness-req start-from-idea "我想做一个个人博客系统，支持文章发布和评论"
```

**输出示例：**
```
## 📝 需求澄清开始

**原始想法**: "我想做一个个人博客系统，支持文章发布和评论"

**生成的文档路径**: docs/requirements/auto-generated/20240115-103000-博客系统.md

**初始文档已创建**，现在开始澄清需求...

---

### ❓ 第一个澄清问题

为了更好地理解你的需求，我需要了解：

1. **目标用户**：这个博客系统主要是给你自己使用，还是面向公众开放注册？
2. **核心功能**：除了文章发布和评论，还需要哪些功能？（如分类、标签、搜索、草稿箱等）
3. **技术偏好**：你有偏好的技术栈吗？（如前端 Vue/React，后端 Java/Node.js）
4. **部署方式**：计划如何部署？（如云服务器、本地部署、静态托管）

请回答这些问题，或者告诉我你最关心的是什么，我会继续深入询问.
```

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

### 场景 1：从一句话需求开始（推荐）

```bash
# 用户只有一个模糊的想法
用户: /harness-req start-from-idea "我想做一个个人博客系统，支持文章发布和评论"

# AI 自动创建文档并开始澄清循环
AI:   [创建 docs/requirements/auto-generated/20240115-103000-博客系统.md]
AI:   /harness-req clarify 这个博客系统是给你自己使用，还是面向公众开放注册？

# 用户回答
用户: 主要是给我自己使用，但希望别人可以访问和评论

# AI 继续提问
AI:   /harness-req clarify 除了文章发布和评论，还需要哪些功能？比如：
      - 分类和标签管理
      - 草稿箱功能
      - 搜索功能
      - SEO 优化
      - 数据统计（浏览量、评论数等）

# 用户回答
用户: 需要分类、标签、草稿箱，SEO 也很重要

# AI 继续深入
AI:   /harness-req clarify 关于技术栈，你有偏好吗？
      - 前端：Vue / React / 纯 HTML
      - 后端：Java Spring Boot / Node.js / Python
      - 数据库：MySQL / PostgreSQL / MongoDB

# 用户回答
用户: 前端用 Vue，后端用 Java Spring Boot，数据库用 MySQL

# AI 起草需求文档
AI:   /harness-req draft docs/requirements/auto-generated/20240115-103000-博客系统.md <完整的需求文档内容>

# AI 请求确认
AI:   /harness-req request

# 用户确认后
用户: /harness-req confirm

# 完成！现在可以进入 Spec 阶段
AI:   需求文档已确认，可以开始生成技术规格了
```

### 场景 2：已有需求文档

```bash
用户: /harness-req start docs/requirements/03-博客管理模块/03-01-博客列表.md
AI:   /harness-req clarify 博客列表需要支持哪些排序方式？
用户: /harness-req clarify 需要支持按发布时间倒序、按浏览量排序
AI:   /harness-req draft docs/requirements/03-博客管理模块/03-01-博客列表.md <content>
AI:   /harness-req request
用户: /harness-req confirm
```

### 自动恢复流程

当主技能 `harness` 扫描到 `requirement.confirmed = false` 时：
1. 调用 `harness-state get-checkpoint requirement`
2. 根据 `checkpoint.step` 继续执行

---

## 需求澄清的最佳实践

### 1. 从模糊到清晰的过程

```
原始想法: "我想做一个博客系统"
    ↓
第一轮澄清: 确定目标用户、核心功能
    ↓
第二轮澄清: 确定技术栈、部署方式
    ↓
第三轮澄清: 确定细节功能（分类、标签、搜索等）
    ↓
第四轮澄清: 确定非功能需求（性能、安全、SEO）
    ↓
最终文档: 完整的需求规格说明书
```

### 2. AI 的提问策略

AI 会按照以下优先级提问：

1. **业务层面**：
   - 目标用户是谁？
   - 解决什么问题？
   - 核心价值是什么？

2. **功能层面**：
   - 需要哪些核心功能？
   - 功能的优先级？
   - 有哪些边界情况？

3. **技术层面**：
   - 技术栈偏好？
   - 性能要求？
   - 安全要求？

4. **运营层面**：
   - 如何部署？
   - 如何维护？
   - 如何扩展？

### 3. 用户的回答技巧

- **尽量具体**：不要说"需要好用"，要说"页面加载时间不超过 2 秒"
- **提供上下文**：说明为什么需要某个功能
- **设定优先级**：区分"必须有"、"应该有"、"可以有"
- **举例说明**：用具体的例子描述期望的行为

---

## 注意事项

- 所有文件路径相对于项目根目录
- 状态变更通过 `harness-state` 技能执行
- 确认后的文档移动/归档由主技能处理
- 分支策略参照：[docs/spec/06-项目规范/05-分支规范.md](../../spec/06-项目规范/05-分支规范.md)
- **从想法开始时**，AI 会自动创建文档路径，无需手动指定
- **澄清循环**会持续进行，直到需求足够清晰可以进入 Spec 阶段
