import { describe, it, expect } from 'vitest';

// Note: useLocale depends on vue-i18n which requires Vue component context.
// For full unit testing, use @vue/test-utils with a test Vue app.
// Here we do a simple build verification that the module can be imported.

describe('useLocale composable (build verification)', () => {
  it('useLocale module should be importable', async () => {
    // This test verifies the module can be imported without errors
    // Actual functionality is tested via E2E tests
    const module = await import('../../src/composables/useLocale');
    expect(module.useLocale).toBeDefined();
    expect(typeof module.useLocale).toBe('function');
  });
});