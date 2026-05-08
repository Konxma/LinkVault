package com.konxma.linkvault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService();
  }

  @Test
  void registerUser_ShouldThrowException_WhenUserIsNull() {
    // Додано другий фіктивний аргумент "testPass", щоб метод скомпілювався
    assertThrows(Exception.class, () -> userService.registerNewUser(null, "testPass"),
        "Очікується помилка (Exception) при спробі передати порожній об'єкт у сервіс");
  }
}
