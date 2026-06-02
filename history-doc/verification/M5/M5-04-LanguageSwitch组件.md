# M5-04 - LanguageSwitch 组件 + Header 集成 - 验证文档

## 任务概述
创建 LanguageSwitch 组件并在 Header 中集成

## 交付物
- `frontend/src/components/LanguageSwitch.vue` - 语言切换组件
- Header 组件已集成 LanguageSwitch

## 验收标准

1. ✅ LanguageSwitch.vue 组件存在且功能完整
2. ✅ 组件显示当前语言（🌐 + 语言代码）
3. ✅ 点击展开下拉菜单，可选择 English 或 中文
4. ✅ 切换后立即调用 setLocale() 生效
5. ✅ 组件样式与 Element Plus Dropdown 一致
6. ✅ Header 中正确集成了 LanguageSwitch 组件

## 测试用例

### 测试 1: LanguageSwitch 组件存在性验证

**步骤**:
1. 检查 `frontend/src/components/LanguageSwitch.vue` 是否存在

**预期结果**:
- 文件存在

### 测试 2: 组件功能验证

**步骤**:
1. 读取 LanguageSwitch.vue 源码
2. 检查是否：
   - 导入并使用了 useLocale composable
   - 显示当前语言图标和代码
   - 提供下拉菜单选择语言
   - 调用 setLocale() 处理语言切换

**预期结果**:
- 组件包含完整的语言切换功能

### 测试 3: Header 集成验证

**步骤**:
1. 查找 Header 组件文件
2. 检查是否导入了 LanguageSwitch 组件
3. 检查是否在模板中使用了 LanguageSwitch

**预期结果**:
- Header 正确集成了 LanguageSwitch 组件

### 测试 4: 样式验证

**步骤**:
1. 检查 LanguageSwitch 的样式是否使用 Element Plus Dropdown 风格

**预期结果**:
- 样式与 Element Plus Dropdown 一致