package com.konxma.linkvault.service;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Шар бізнес-логіки для роботи з даними користувачів.
 * Відповідає за валідацію, безпечне хешування паролів та аутентифікацію.
 */
public class UserService {

  private final UserRepository userRepository;

  /**
   * Конструктор сервісу, що реалізує патерн Dependency Injection (DI).
   *
   * @param userRepository об'єкт доступу до даних користувачів.
   */
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Реєструє нового користувача в системі після валідації вхідних даних.
   *
   * @param username ім'я користувача.
   * @param email електронна адреса.
   * @param password пароль у відкритому вигляді.
   * @return true, якщо реєстрація успішна, інакше false.
   */
  public boolean registerUser(String username, String email, String password) {
    if (username == null || username.trim().isEmpty() ||
        email == null || !email.contains("@") ||
        password == null || password.length() < 3) {
      System.out.println("Помилка валідації: невірний формат даних.");
      return false;
    }

    if (userRepository.getUserByEmail(email) != null) {
      System.out.println("Користувач з таким email вже існує.");
      return false;
    }

    String hashedPassword = hashPassword(password);
    User newUser = new User(0, username, email, hashedPassword);
    return userRepository.createUser(newUser);
  }

  /**
   * Перевіряє облікові дані та авторизує користувача.
   *
   * @param email електронна адреса.
   * @param password введений пароль.
   * @return об'єкт UserDTO для безпечної передачі даних в UI, або null у разі помилки.
   */
  public UserDTO authenticateUser(String email, String password) {
    User user = userRepository.getUserByEmail(email);
    if (user != null) {
      String hashedInputPassword = hashPassword(password);
      if (user.getPasswordHash().equals(hashedInputPassword)) {
        return new UserDTO(user.getUserId(), user.getUsername(), user.getEmail());
      }
    }
    return null;
  }

  /**
   * Виконує криптографічне хешування рядка за алгоритмом SHA-256.
   *
   * @param password пароль для хешування.
   * @return рядок у форматі HEX, що містить хеш пароля.
   */
  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(password.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Помилка хешування пароля", e);
    }
  }
}
