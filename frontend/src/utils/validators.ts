export interface ValidationRule {
  required?: boolean
  min?: number
  max?: number
  pattern?: RegExp
  message?: string
}

export interface ValidationRules {
  [key: string]: ValidationRule[]
}

export function validate(data: Record<string, unknown>, rules: ValidationRules): Record<string, string> {
  const errors: Record<string, string> = {}

  for (const field in rules) {
    const fieldRules = rules[field]
    const value = data[field]

    for (const rule of fieldRules) {
      if (rule.required && (!value || (typeof value === 'string' && !value.trim()))) {
        errors[field] = rule.message || `${field} is required`
        break
      }

      if (value && typeof value === 'string') {
        if (rule.min && value.length < rule.min) {
          errors[field] = rule.message || `${field} must be at least ${rule.min} characters`
          break
        }

        if (rule.max && value.length > rule.max) {
          errors[field] = rule.message || `${field} must be at most ${rule.max} characters`
          break
        }

        if (rule.pattern && !rule.pattern.test(value)) {
          errors[field] = rule.message || `${field} is invalid`
          break
        }
      }
    }
  }

  return errors
}

export const emailRule: ValidationRule = {
  pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  message: 'Invalid email address'
}

export const passwordRule: ValidationRule = {
  min: 6,
  message: 'Password must be at least 6 characters'
}

export const urlRule: ValidationRule = {
  pattern: /^https?:\/\/.+/,
  message: 'Invalid URL'
}