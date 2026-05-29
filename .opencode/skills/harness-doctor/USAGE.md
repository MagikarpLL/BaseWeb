# Harness Doctor 使用指南

## 🎯 什么时候需要使用 harness-doctor？

### 场景 1：状态文件与实际进度不一致

**症状：**
- 你手动完成了某些任务，但 `/harness-impl status` 显示它们仍在 queue 中
- Git 分支已经合并，但状态文件未更新
- 迭代计划文件更新了，但任务队列没有同步

**解决：**
```bash
/harness-doctor scan    # 诊断问题
/harness-doctor fix     # 自动修复
```

---

### 场景 2：项目长时间未维护后恢复

**症状：**
- 上次工作是几周前，忘记做到哪里了
- 状态文件的 timestamp 很旧
- 不确定哪些任务已完成

**解决：**
```bash
/harness-doctor scan    # 全面扫描
# 查看诊断报告，了解当前状态
/harness-doctor fix     # 修复明显的问题
/harness-impl status    # 确认当前进度
```

---

### 场景 3：切换迭代前的健康检查

**症状：**
- M1 即将完成，准备开始 M2
- 想确保 M1 的所有任务都正确标记为完成

**解决：**
```bash
/harness-doctor scan    # 检查 M1 状态
/harness-doctor fix     # 修复遗留问题
/harness-impl start M2  # 安全地启动 M2
```

---

### 场景 4：遇到奇怪的错误

**症状：**
- `/harness-impl next` 返回空，但你知道还有任务
- phase 显示 "implement"，但无法继续工作
- checkpoint 状态看起来不对

**解决：**
```bash
/harness-doctor scan    # 找出根本原因
/harness-doctor fix     # 自动修复
# 如果还有问题，查看详细报告并手动处理
```

---

## 📋 常用命令速查

### 基础诊断

```bash
# 快速扫描（推荐每周执行一次）
/harness-doctor scan

# 查看诊断结果
/harness-doctor status

# 自动修复明确的问题
/harness-doctor fix
```

### 高级用法

```bash
# 强制修复所有问题（会请求确认）
/harness-doctor fix-all

# 重置诊断状态（重新开始）
/harness-doctor reset
```

### 通过主技能调用

```bash
# 使用主技能的快捷命令
/harness doctor        # 等同于 /harness-doctor scan
/harness doctor-fix    # 等同于 /harness-doctor fix
```

---

## 🔍 诊断报告解读

### Critical Issues（必须修复）

```markdown
❌ M1-01 在 queue 中，但 Git 分支已合并到 develop
```

**含义：** 任务实际上已完成，但状态文件未更新  
**影响：** 可能导致重复实现该任务  
**修复：** `/harness-doctor fix` 会自动移动到 completed

---

### Warnings（建议修复）

```markdown
⚠️ docs/plan 中有 M1-04，但 state.tasks.queue 中没有
```

**含义：** 迭代计划更新了，但状态未同步  
**影响：** 新任务不会被执行  
**修复：** `/harness-doctor fix` 会自动添加到 queue

---

### Suggestions（优化建议）

```markdown
💡 state.messages 数组有 150+ 条记录，建议清理旧日志
```

**含义：** 日志过多可能影响性能  
**影响：** 状态文件变大，读取变慢  
**修复：** 手动清理或忽略（不影响功能）

---

## 🛠️ 实际案例

### 案例 1：Git 分支已合并但状态未更新

**问题：**
```bash
# 查看 Git 分支
git branch -a | grep M1-01
# 输出: (无结果，说明分支已删除)

# 查看状态
/harness-impl status
# 输出: Pending Tasks: [M1-01, M1-02]
```

**诊断：**
```bash
/harness-doctor scan
# 输出:
# ❌ Critical: M1-01 在 queue 中，但 Git 分支不存在
```

**修复：**
```bash
/harness-doctor fix
# 输出:
# ✅ Fixed: M1-01 从 queue 移动到 completed

# 验证
/harness-impl status
# 输出: Pending Tasks: [M1-02]
```

---

### 案例 2：迭代计划更新但未同步

**问题：**
```bash
# 你在 docs/plan/01-迭代计划.md 中添加了 M1-05
# 但 /harness-impl next 仍然只返回 M1-02, M1-03, M1-04
```

**诊断：**
```bash
/harness-doctor scan
# 输出:
# ⚠️ Warning: docs/plan 中有 M1-05，但 state.tasks.queue 中没有
```

**修复：**
```bash
/harness-doctor fix
# 输出:
# ✅ Fixed: M1-05 添加到 queue

# 验证
/harness-impl scan
/harness-impl next
# 输出: M1-05
```

---

### 案例 3：Phase 状态不正确

**问题：**
```bash
/harness status
# 输出: Phase: implement

/harness-impl status
# 输出: Pending Tasks: [] (队列为空)
```

**诊断：**
```bash
/harness-doctor scan
# 输出:
# ❌ Critical: state.phase = "implement"，但 tasks.queue 为空
```

**修复：**
```bash
/harness-doctor fix
# 输出:
# ✅ Fixed: state.phase 从 "implement" 改为 "completed"

# 现在可以启动新迭代
/harness-impl start M2
```

---

## 💡 最佳实践

### 1. 定期维护

```bash
# 每周五下午执行
/harness-doctor scan
/harness-doctor fix
```

### 2. 迭代转换时

```bash
# 完成 M1 后
/harness-doctor scan
/harness-doctor fix
/harness-impl start M2
```

### 3. 遇到问题时

```bash
# 第一步：诊断
/harness-doctor scan

# 第二步：查看报告
/harness-doctor status

# 第三步：修复
/harness-doctor fix

# 第四步：验证
/harness-impl status
```

### 4. 备份状态文件

```bash
# 在执行 fix 之前备份
cp .harness/state.json .harness/state.json.backup

# 如果修复出错，可以恢复
cp .harness/state.json.backup .harness/state.json
```

---

## ⚠️ 注意事项

1. **保守原则**：`/harness-doctor fix` 只修复明确的问题，不确定的会跳过
2. **Git 依赖**：任务状态检查需要 Git 可用，确保在项目根目录执行
3. **幂等性**：多次执行不会产生副作用，可以放心重试
4. **备份建议**：重要操作前备份 `.harness/state.json`
5. **手动介入**：对于复杂问题，可能需要手动编辑状态文件

---

## 🆘 常见问题

### Q: `/harness-doctor fix` 没有修复所有问题？

**A:** 这是正常行为。Doctor 采用保守策略，只修复明确的问题。对于不确定的问题，会跳过并建议你手动处理。你可以：
1. 查看 `/harness-doctor status` 了解哪些问题被跳过
2. 使用 `/harness-doctor fix-all` 强制修复（会请求确认）
3. 手动编辑 `.harness/state.json`

### Q: 诊断报告太多，如何清理？

**A:** 
```bash
# 重置诊断状态
/harness-doctor reset

# 或者手动清理 messages
/harness-state update '[{"op":"replace","path":"/messages","value":[]}]'
```

### Q: 如何禁用自动诊断？

**A:** Doctor 不会自动执行，只有在你主动调用时才会运行。如果你想完全禁用，可以不配置定时任务。

### Q: Doctor 会影响性能吗？

**A:** 扫描过程会读取多个文件和执行 Git 命令，通常在几秒内完成。对日常开发影响很小。

---

## 📚 相关文档

- [Harness 主技能](../harness/SKILL.md)
- [Harness Implementation](../harness-impl/SKILL.md)
- [Harness State](../harness-state/SKILL.md)
