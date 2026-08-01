package com.example.springreact.util;

public class PasswordValidator {

  private PasswordValidator() {}

  public static boolean isValid(String password) {
    if (password == null || password.length() < 8) return false;
    boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
    boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
    boolean hasDigit = password.chars().anyMatch(Character::isDigit);
    boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    return hasUpper && hasLower && hasDigit && hasSpecial;
  }

  public static final String ERROR_MESSAGE =
      "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo";
}
