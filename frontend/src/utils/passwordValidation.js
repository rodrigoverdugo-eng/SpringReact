export const PASSWORD_RULES = [
  { id: 'length',    label: 'Al menos 8 caracteres',            test: (p) => p.length >= 8 },
  { id: 'uppercase', label: 'Al menos una letra mayúscula',     test: (p) => /[A-Z]/.test(p) },
  { id: 'lowercase', label: 'Al menos una letra minúscula',     test: (p) => /[a-z]/.test(p) },
  { id: 'number',    label: 'Al menos un número',               test: (p) => /[0-9]/.test(p) },
  { id: 'special',   label: 'Al menos un símbolo (!@#$%^&*…)',  test: (p) => /[^A-Za-z0-9]/.test(p) },
];

export function validatePassword(password) {
  const errors = PASSWORD_RULES.filter((r) => !r.test(password)).map((r) => r.label);
  return { isValid: errors.length === 0, errors };
}
