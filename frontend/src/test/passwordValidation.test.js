import { describe, it, expect } from 'vitest'
import { validatePassword, PASSWORD_RULES } from '../utils/passwordValidation'

describe('validatePassword', () => {
  it('rechaza contraseña vacía (todos los errores)', () => {
    const { isValid, errors } = validatePassword('')
    expect(isValid).toBe(false)
    expect(errors).toHaveLength(PASSWORD_RULES.length)
  })

  it('rechaza contraseña sin mayúscula', () => {
    const { isValid, errors } = validatePassword('abcdef1!')
    expect(isValid).toBe(false)
    expect(errors).toContain('Al menos una letra mayúscula')
  })

  it('rechaza contraseña sin minúscula', () => {
    const { isValid, errors } = validatePassword('ABCDEF1!')
    expect(isValid).toBe(false)
    expect(errors).toContain('Al menos una letra minúscula')
  })

  it('rechaza contraseña sin número', () => {
    const { isValid, errors } = validatePassword('Abcdefg!')
    expect(isValid).toBe(false)
    expect(errors).toContain('Al menos un número')
  })

  it('rechaza contraseña sin símbolo', () => {
    const { isValid, errors } = validatePassword('Abcdefg1')
    expect(isValid).toBe(false)
    expect(errors).toContain('Al menos un símbolo (!@#$%^&*…)')
  })

  it('rechaza contraseña demasiado corta', () => {
    const { isValid, errors } = validatePassword('Ab1!')
    expect(isValid).toBe(false)
    expect(errors).toContain('Al menos 8 caracteres')
  })

  it('acepta contraseña que cumple todos los requisitos', () => {
    const { isValid, errors } = validatePassword('Secure1!')
    expect(isValid).toBe(true)
    expect(errors).toHaveLength(0)
  })

  it('acepta contraseña con símbolos varios', () => {
    const { isValid } = validatePassword('MyP@ssw0rd#2024')
    expect(isValid).toBe(true)
  })
})

describe('PASSWORD_RULES', () => {
  it('contiene exactamente 5 reglas', () => {
    expect(PASSWORD_RULES).toHaveLength(5)
  })

  it('cada regla tiene id, label y función test', () => {
    PASSWORD_RULES.forEach((rule) => {
      expect(rule).toHaveProperty('id')
      expect(rule).toHaveProperty('label')
      expect(typeof rule.test).toBe('function')
    })
  })
})
