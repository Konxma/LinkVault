package com.konxma.linkvault.dto;

/**
 * Data Transfer Object (DTO) для сутності користувача.
 * Використовується для безпечної передачі даних між шарами програми,
 * гарантуючи, що конфіденційна інформація (наприклад, хеш пароля) не потрапить у UI.
 */
public class UserDTO {
  private int userId;
  private String username;
  private String email;

  /**
   * Конструктор для створення об'єкта передачі даних користувача.
   *
   * @param userId унікальний ідентифікатор користувача в базі даних.
   * @param username ім'я користувача.
   * @param email електронна пошта користувача.
   */
  public UserDTO(int userId, String username, String email) {
    this.userId = userId;
    this.username = username;
    this.email = email;
  }

  /**
   * Повертає ідентифікатор користувача.
   *
   * @return userId
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Повертає ім'я користувача.
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Повертає електронну пошту користувача.
   *
   * @return email
   */
  public String getEmail() {
    return email;
  }
}
