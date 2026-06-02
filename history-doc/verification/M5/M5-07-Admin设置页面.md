# M5-07 - Admin Settings 语言设置页面 - 验证文档

## 任务概述
在管理后台 Settings 页面添加独立的语言设置 Tab

## 交付物
- `frontend/src/views/admin/Settings.vue` 包含 Language Tab
- Language Tab 提供语言切换下拉框
- 语言切换后正确调用 setLocale() 并保存到 localStorage

## 验收标准

1. ✅ Settings.vue 包含 Language 设置 Tab
2. ✅ Language Tab 包含语言切换下拉框（English / 中文）
3. ✅ 切换语言后调用 setLocale() 立即生效
4. ✅ 语言偏好保存到 localStorage
5. ✅ 使用 useLocale composable 获取 t() 函数用于界面翻译

## 测试用例

### 测试 1: Settings.vue Language Tab 存在性验证

**步骤**:
1. 读取 `frontend/src/views/admin/Settings.vue`
2. 检查是否包含 ElTabPane 且 label 为 Language 或包含语言相关文本

**预期结果**:
- Settings.vue 包含 Language Tab

### 测试 2: 语言切换下拉框验证

**步骤**:
1. 检查 Language Tab 中是否包含 ElSelect 组件
2. 检查选项是否包含 English 和 中文

**预期结果**:
- 下拉框包含两种语言选项

### 测试 3: 语言切换功能验证

**步骤**:
1. 检查是否导入了 useLocale composable
2. 检查是否调用了 setLocale() 函数
3. 检查是否在切换后显示成功消息

**预期结果**:
- 语言切换功能完整实现

### 测试 4: 翻译集成验证

**步骤**:
1. 检查 Settings.vue 是否使用 $t() 函数
2. 检查 admin.* 相关的翻译 key 是否在 locales 文件中

**预期结果**:
- Settings 页面正确使用翻译系统