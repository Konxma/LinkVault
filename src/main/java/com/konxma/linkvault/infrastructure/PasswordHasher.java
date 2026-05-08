package com.konxma.linkvault.infrastructure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {

  // Метод для перетворення звичайного пароля на нечитабельний хеш
  public static String hashPassword(String rawPassword) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawPassword.getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Помилка хешування пароля", e);
    }
  }

  // Метод для перевірки, чи збігається введений пароль із хешем у базі
  public static boolean verifyPassword(String rawPassword, String hashedPassword) {
    return hashPassword(rawPassword).equals(hashedPassword);
  }
}