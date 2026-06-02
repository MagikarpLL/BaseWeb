# Spec 文档模板

> 新建 Spec 文档时，复制本模板并填入内容。
> 详见编写规范: [CONVENTIONS.md](../CONVENTIONS.md)

---

# {编号}-{名称}

> 对应 Requirement: [{Requirement 文件名}](../requirements/{路径})

## {N}.1 概述 / 设计原则

> 本 Spec 的核心决策、原则和约束。

|| 原则 | 说明 |
||------|------|
|| 原则 1 | 描述 |
|| 原则 2 | 描述 |

---

## {N}.2 具体规格

> 按 Spec 主题组织章节。以下为常见子章节示例，按需选用。

### 数据模型

> 如果本 Spec 涉及数据库变更。

```sql
CREATE TABLE example (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### API 端点

> 如果本 Spec 涉及新增或修改 API。

|| 接口 | 方法 | 说明 | 认证 |
||------|------|------|------|
|| `GET /api/example` | GET | 说明 | 否 |

**请求/响应示例**：

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

### 配置项

> 如果本 Spec 涉及配置或环境变量。

|| 配置项 | 环境变量 | 默认值 | 说明 |
||------|----------|--------|------|
|| 示例 | `EXAMPLE_KEY` | `default` | 说明 |

### 关键逻辑

> 如果本 Spec 涉及业务规则或算法。

```text
步骤 1: ...
步骤 2: ...
步骤 3: ...
```

### 代码示例

> 如果本 Spec 涉及核心代码结构。

```java
// Java 示例
```

```typescript
// TypeScript 示例
```

---

## {N}.3 验收标准

- [ ] 规格 1 实现正确
- [ ] 规格 2 实现正确
- [ ] 与 Requirement 的对应关系完整
