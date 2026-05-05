package com.konxma.linkvault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Клас для базового модульного тестування сервісу користувачів.
 */
class UserServiceTest {

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService();
  }

  @Test
  void registerUser_ShouldThrowException_WhenUserIsNull() {
    assertThrows(Exception.class, () -> userService.registerNewUser(null),
        "Очікується помилка (Exception) при спробі передати порожній об'єкт у сервіс");
  }
}
