# M5-01 - vue-i18n 依赖安装 + 项目结构 - 验证文档

## 任务概述
安装 vue-i18n@9 依赖并建立国际化项目结构

## 交付物
- package.json 包含 vue-i18n@9 依赖
- `frontend/src/locales/` 目录
- `frontend/src/i18n/` 目录
- `frontend/src/composables/useLocale.ts` 文件

## 验收标准

1. ✅ vue-i18n@9 依赖已安装在 frontend 项目中
2. ✅ locales 目录存在，包含 en.json 和 zh-CN.json
3. ✅ i18n 目录存在，包含 index.ts 配置文件
4. ✅ composables 目录存在 useLocale.ts

## 测试用例

### 测试 1: package.json 依赖验证

**步骤**:
1. 打开 `frontend/package.json`
2. 检查 `dependencies` 中是否包含 `vue-i18n`

**预期结果**:
- `vue-i18n` 版本为 `^9.x.x`

### 测试 2: 目录结构验证

**步骤**:
1. 检查以下路径是否存在：
   - `frontend/src/locales/`
   - `frontend/src/i18n/`
   - `frontend/src/composables/`

**预期结果**:
- 所有目录都存在

### 测试 3: 关键文件存在性验证

**步骤**:
1. 检查以下文件是否存在且非空：
   - `frontend/src/locales/en.json`
   - `frontend/src/locales/zh-CN.json`
   - `frontend/src/i18n/index.ts`
   - `frontend/src/composables/useLocale.ts`

**预期结果**:
- 所有文件都存在且包含有效内容