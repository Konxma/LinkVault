package com.konxma.linkvault.service;

import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {

  // Застосування принципу Dependency Injection (DI) - передаємо залежність через конструктор
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // Функція реєстрації з валідацією
  public boolean registerUser(String username, String email, String password) {
    // Валідація бізнес-логіки: перевірка на порожні поля та формат email
    if (username == null || username.trim().isEmpty() ||
        email == null || !email.contains("@") ||
        password == null || password.length() < 3) {
      System.out.println("Помилка валідації: невірний формат даних.");
      return false;
    }

    // Перевірка, чи не зайнятий вже такий email
    if (userRepository.getUserByEmail(email) != null) {
      System.out.println("Користувач з таким email вже існує.");
      return false;
    }

    // Хешуємо пароль перед збереженням
    String hashedPassword = hashPassword(password);

    // Створюємо нового користувача (userId = 0, бо БД сама згенерує ID через SERIAL)
    User newUser = new User(0, username, email, hashedPassword);

    return userRepository.createUser(newUser);
  }

  // Функція аутентифікації (входу)
  public User authenticateUser(String email, String password) {
    User user = userRepository.getUserByEmail(email);

    if (user != null) {
      // Хешуємо введений пароль і порівнюємо з тим, що в базі
      String hashedInputPassword = hashPassword(password);
      if (user.getPasswordHash().equals(hashedInputPassword)) {
        return user; // Паролі співпали, повертаємо об'єкт користувача
      }
    }
    return null; // Невірний email або пароль
  }

  // Допоміжний метод для безпечного хешування паролів (SHA-256)
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