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

class UserServiceTest {

  private UserRepository userRepositoryMock;
  private UserService userService;

  @BeforeEach
  void setUp() {
    // Створюємо "фейковий" (мок) репозиторій за допомогою Mockito
    userRepositoryMock = Mockito.mock(UserRepository.class);

    // Передаємо його в наш сервіс (Dependency Injection)
    userService = new UserService(userRepositoryMock);
  }

  @Test
  void registerUser_ShouldReturnFalse_WhenEmailIsInvalid() {
    boolean result = userService.registerUser("testuser", "invalidemail.com", "password123");
    assertFalse(result, "Реєстрація має бути відхилена через невірний формат email");
  }

  @Test
  void registerUser_ShouldReturnFalse_WhenPasswordIsTooShort() {
    boolean result = userService.registerUser("testuser", "test@mail.com", "12");
    assertFalse(result, "Реєстрація має бути відхилена через занадто короткий пароль");
  }

  @Test
  void registerUser_ShouldReturnFalse_WhenUserAlreadyExists() {
    User existingUser = new User(1, "exist", "exist@mail.com", "hash");
    when(userRepositoryMock.getUserByEmail("exist@mail.com")).thenReturn(existingUser);

    boolean result = userService.registerUser("newuser", "exist@mail.com", "password123");
    assertFalse(result, "Реєстрація має бути відхилена, бо email вже зайнятий");
  }

  @Test
  void registerUser_ShouldReturnTrue_WhenDataIsValid() {
    when(userRepositoryMock.getUserByEmail(anyString())).thenReturn(null);
    when(userRepositoryMock.createUser(any(User.class))).thenReturn(true);

    boolean result = userService.registerUser("gooduser", "good@mail.com", "strongpass");
    assertTrue(result, "Реєстрація має пройти успішно з правильними даними");
  }
}