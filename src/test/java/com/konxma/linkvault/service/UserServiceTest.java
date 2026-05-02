package com.konxma.linkvault.service;

import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Клас для модульного тестування бізнес-логіки сервісу користувачів.
 * Використовує фреймворки JUnit 5 та Mockito для перевірки методів реєстрації.
 */
class UserServiceTest {

  private UserRepository userRepositoryMock;
  private UserService userService;

  /**
   * Налаштування середовища перед кожним тестом.
   * Створює заглушку (мок) для репозиторію та ініціалізує сервіс.
   */
  @BeforeEach
  void setUp() {
    userRepositoryMock = Mockito.mock(UserRepository.class);
    userService = new UserService(userRepositoryMock);
  }

  /**
   * Тестує, що реєстрація відхиляється, якщо електронна адреса має невірний формат.
   */
  @Test
  void registerUser_ShouldReturnFalse_WhenEmailIsInvalid() {
    boolean result = userService.registerUser("testuser", "invalidemail.com", "password123");
    assertFalse(result, "Реєстрація має бути відхилена через невірний формат email");
  }

  /**
   * Тестує, що реєстрація відхиляється, якщо пароль занадто короткий.
   */
  @Test
  void registerUser_ShouldReturnFalse_WhenPasswordIsTooShort() {
    boolean result = userService.registerUser("testuser", "test@mail.com", "12");
    assertFalse(result, "Реєстрація має бути відхилена через занадто короткий пароль");
  }

  /**
   * Тестує, що реєстрація відхиляється, якщо користувач з такою поштою вже існує.
   */
  @Test
  void registerUser_ShouldReturnFalse_WhenUserAlreadyExists() {
    User existingUser = new User(1, "exist", "exist@mail.com", "hash");
    when(userRepositoryMock.getUserByEmail("exist@mail.com")).thenReturn(existingUser);

    boolean result = userService.registerUser("newuser", "exist@mail.com", "password123");
    assertFalse(result, "Реєстрація має бути відхилена, бо email вже зайнятий");
  }

  /**
   * Тестує успішну реєстрацію з валідними даними.
   */
  @Test
  void registerUser_ShouldReturnTrue_WhenDataIsValid() {
    when(userRepositoryMock.getUserByEmail(anyString())).thenReturn(null);
    when(userRepositoryMock.createUser(any(User.class))).thenReturn(true);

    boolean result = userService.registerUser("gooduser", "good@mail.com", "strongpass");
    assertTrue(result, "Реєстрація має пройти успішно з правильними даними");
  }
}
