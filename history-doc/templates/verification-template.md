# Verification 文档模板

> 新建 Verification 文档时，复制本模板并填入内容。
> **核心原则**：
> 1. Verification 中的测试必须是**实际可执行的命令**
> 2. 测试代码文件是交付物的一部分，必须同步编写
> 详见编写规范: [CONVENTIONS.md](../CONVENTIONS.md)

---

# {任务编号} - {任务名称}

> 对应任务: [01-迭代计划.md](../plan/01-迭代计划.md#任务-{task-id})
> 涉及 Spec: [{Spec 文件}](../spec/{路径})

## 验证目标

描述本次验证的核心目标（1-2 句话）。

## 交付物清单

| 类型 | 前端路径 | 后端路径 |
|------|----------|----------|
| 实现代码 | src/... | src/main/java/com/example/personalwebsite/... |
| 单元测试 | tests/unit/{task_id}.test.ts | src/test/java/com/example/personalwebsite/{layer}/{Entity}Test.java |
| E2E/集成测试 | tests/e2e/{task_id}.spec.ts | src/test/java/.../{Entity}IT.java |

## 测试用例

### TC-01: {测试用例名称}

**测试代码文件**: `tests/unit/{task_id}.test.ts`

**执行命令**:
```bash
cd frontend && npm run test:run -- --run tests/unit/{task_id}.test.ts
```

**预期结果**:
- 退出码 = 0
- 测试通过数量 > 0

### TC-02: {E2E 测试名称}

**测试代码文件**: `tests/e2e/{task_id}.spec.ts`

**执行命令**:
```bash
cd frontend && npm run test:e2e -- {task_id}
```

**预期结果**:
- 退出码 = 0
- 测试通过数量 > 0

## 验证报告（由 AI 填写）

**验证时间**：{YYYY-MM-DD HH:mm:ss}
**验证结果**：✅ PASSED / ❌ FAILED

### TC-01: {测试用例名称}
- **命令**: `cd frontend && npm run test:run -- --run tests/unit/{task_id}.test.ts`
- **退出码**: {0|非0}
- **运行时间**: {Xs}
- **状态**: ✅ PASSED / ❌ FAILED
- **输出摘要**: {命令输出的关键部分}

### TC-02: {E2E 测试名称}
- **命令**: `cd frontend && npm run test:e2e -- tests/e2e/{task_id}.spec.ts`
- **退出码**: {0|非0}
- **运行时间**: {Xs}
- **状态**: ✅ PASSED / ❌ FAILED
- **输出摘要**: {命令输出的关键部分}

## 验收标准

- [ ] TC-01 通过
- [ ] TC-02 通过
- [ ] 所有测试通过

## 注意事项

- **测试代码必须同步编写**：不能事后补测试
- **执行命令是必须的**：每个测试用例必须包含可执行的命令
- **测试文件必须实际存在**：verification 文档中的测试文件路径必须指向实际存在的文件