---
name: harness-verify
description: >
  Harness Verification Skills - 验证技能（已废弃）。
  
  ⚠️ **重要说明：此技能已废弃**
  
  验证功能已完全整合到 `harness-impl` 技能的内部闭环中。
  不再需要单独调用此技能进行验证。
  
  保留此文档仅用于历史参考。
---

# Harness Verification Skills（已废弃）

## ⚠️ 废弃声明

**此技能已于 2024 年废弃。**

### 原因
根据 Harness 工程思想的优化，验证被重新定位为**实现的内在约束**，而非独立阶段。因此：

1. **验证逻辑已迁移**：所有验证功能已整合到 `harness-impl` 技能的内部闭环中
2. **工作流简化**：移除了独立的 `verify` 阶段，简化了主技能的 phase 定义
3. **自动化增强**：验证现在在代码生成后自动触发，无需手动调用

### 替代方案
请使用 `/harness-impl verify <task_id>` 命令，该命令会在实现任务后自动执行验证。

或者更推荐的方式是：让 `harness-impl` 的自动化工作流自行处理验证（无需手动调用任何验证命令）。

---

## 历史文档（仅供参考）

以下内容保留了原有的设计思路，但**不再适用**。

### 原状态结构

```json
{
  "verify": {
    "spec_doc": "docs/spec/03-博客编辑.md",
    "current_criterion": 0,
    "passed": [],
    "failed": [],
    "checkpoint": {
      "step": "check|report|done",
      "criterion_index": 0,
      "timestamp": null
    }
  }
}
```

### 原有命令（已废弃）

- `/harness-verify start <spec_path>` - 已废弃
- `/harness-verify check <criterion>` - 已废弃
- `/harness-verify report` - 已废弃
- `/harness-verify status` - 已废弃
- `/harness-verify reset` - 已废弃

### 新的验证流程

现在的验证流程在 `harness-impl` 内部自动完成：

```
/harness-impl implement M1-09
    ↓
[自动触发验证]
    ↓
静态检查 → 单元测试 → E2E验证 → 策略检查
    ↓
[验证通过] → /harness-impl commit M1-09
[验证失败] → /harness-impl auto-repair → 重新验证
```

详见：[harness-impl SKILL.md](../harness-impl/SKILL.md)

---

## 迁移指南

如果你之前的工作流中使用了 `harness-verify`，请按以下方式迁移：

### 旧工作流
```bash
/harness-impl implement M1-09
/harness-verify start docs/spec/xxx.md
/harness-verify check criterion_1
/harness-verify report
/harness-impl commit M1-09
```

### 新工作流
```bash
/harness-impl implement M1-09
# 验证自动执行，无需手动调用
/harness-impl commit M1-09
```

或者使用自动化流程：
```bash
/harness start implement
# AI 会自动完成 implement → verify → commit 的完整闭环
```

---

## 注意事项

- **不要在新项目中使用此技能**
- 现有项目中如有引用此技能的配置，请更新为使用 `harness-impl`
- 如有疑问，请参考 [harness 主技能文档](../harness/SKILL.md)
