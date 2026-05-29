# M7-03 - OG/Twitter Card 优化 - 验证文档

> 对应任务: [01-迭代计划.md](../plan/01-迭代计划.md#任务-M7-03)
> 涉及 Spec: [12-03-社交分享优化规格.md](../spec/12-博客增强模块/12-03-社交分享优化规格.md)

## 验证目标

验证 OG/Twitter Card 标签完整：og:url, og:type, og:title, og:description, og:image, article:section, article:tag, twitter:card, twitter:site。

## 交付物检查

| 交付物 | 路径 | 存在 |
|--------|------|------|
| BlogDetail.vue OG 标签 | frontend/src/views/public/BlogDetail.vue | ✅ |
| useHead composable | frontend/src/composables/useHead.ts | ✅ |

## 测试用例

### TC-01: 前端编译检查

**执行命令**:
```bash
cd frontend && npm run build
```

**预期结果**:
- 退出码 = 0
- 无 TypeScript 错误

### TC-02: OG 标签静态检查

**执行命令**:
```bash
grep -n "og:url\|og:type\|og:title\|og:description\|og:image\|twitter:card" frontend/src/views/public/BlogDetail.vue
```

**预期结果**:
- 找到所有 OG 标签定义
- 代码中包含 og:url, og:type, og:title, og:description, og:image, twitter:card

## 验证报告（由 AI 填写）

**验证时间**：{YYYY-MM-DD HH:mm:ss}
**验证结果**：✅ PASSED / ❌ FAILED

### TC-01: 前端编译检查
- **命令**: `cd frontend && npm run build`
- **退出码**: {0|非0}
- **运行时间**: {Xs}
- **状态**: ✅ PASSED / ❌ FAILED
- **输出摘要**: {命令输出的关键部分}

### TC-02: OG 标签静态检查
- **命令**: `grep -n "og:url\|og:type\|og:title\|og:description\|og:image\|twitter:card" frontend/src/views/public/BlogDetail.vue`
- **退出码**: {0|非0}
- **运行时间**: {Xs}
- **状态**: ✅ PASSED / ❌ FAILED
- **输出摘要**: {命令输出的关键部分}

## 验收标准

- [ ] TC-01 前端编译检查通过
- [ ] TC-02 OG 标签静态检查通过
- [ ] og:url 标签已实现
- [ ] og:type 标签已实现
- [ ] og:title 标签已实现
- [ ] og:description 标签已实现
- [ ] og:image 标签已实现
- [ ] twitter:card 标签已实现
- [ ] 所有测试通过

## 注意事项

- **执行命令是必须的**：每个测试用例必须包含可执行的命令
- **不能只做静态检查**：读取代码检查结构不算验证，必须实际运行
- **失败时提供实际输出**：错误信息应包含实际命令的输出，便于调试