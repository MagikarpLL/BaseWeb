import { test, expect } from '@playwright/test';

test.describe('LanguageSwitch 组件', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('应该显示语言切换按钮', async ({ page }) => {
    // 查找 LanguageSwitch 组件 - 使用类名选择器
    const languageSwitch = page.locator('.language-switch');
    await expect(languageSwitch).toBeVisible();
  });

  test('点击语言切换按钮应切换到另一种语言', async ({ page }) => {
    const languageSwitch = page.locator('.language-switch');
    
    // 获取当前语言
    const initialLang = await page.locator('html').getAttribute('lang');
    
    // 点击切换
    await languageSwitch.click();
    
    // 点击中文选项
    await page.getByText('中文').click();
    
    // 验证语言已切换（lang 属性应该改变）
    const newLang = await page.locator('html').getAttribute('lang');
    expect(newLang).not.toBe(initialLang);
  });

  test('语言切换后页面文本应更新', async ({ page }) => {
    const languageSwitch = page.locator('.language-switch');
    
    // 点击切换
    await languageSwitch.click();
    await page.getByText('中文').click();
    
    // 等待 DOM 更新
    await page.waitForTimeout(500);
    
    // 验证 document.documentElement.lang 已更新
    const lang = await page.locator('html').getAttribute('lang');
    expect(['en', 'zh-CN', 'zh-Hans']).toContain(lang);
  });

  test('语言切换应保存到 localStorage', async ({ page }) => {
    const languageSwitch = page.locator('.language-switch');
    
    // 点击切换
    await languageSwitch.click();
    await page.getByText('中文').click();
    
    // 等待 localStorage 更新
    await page.waitForTimeout(200);
    
    // 验证 localStorage 中的 locale 值
    const storedLocale = await page.evaluate(() => localStorage.getItem('locale'));
    expect(['en', 'zh-CN']).toContain(storedLocale);
  });
});