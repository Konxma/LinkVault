package com.konxma.linkvault.model;

/**
 * Сутність користувача системи.
 * Відображає запис із таблиці users у базі даних.
 */
public class User {

  private int userId;
  private String username;
  private String email;
  private String passwordHash;

  /**
   * Конструктор для створення об'єкта користувача.
   *
   * @param userId унікальний ідентифікатор.
   * @param username ім'я користувача.
   * @param email електронна адреса.
   * @param passwordHash захешований пароль.
   */
  public User(int userId, String username, String email, String passwordHash) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  /** @return унікальний ідентифікатор користувача */
  public int getUserId() { return userId; }

  /** @param userId новий ідентифікатор користувача */
  public void setUserId(int userId) { this.userId = userId; }

  /** @return ім'я користувача */
  public String getUsername() { return username; }

  /** @param username нове ім'я користувача */
  public void setUsername(String username) { this.username = username; }

  /** @return електронна адреса користувача */
  public String getEmail() { return email; }

  /** @param email нова електронна адреса */
  public void setEmail(String email) { this.email = email; }

  /** @return захешований пароль */
  public String getPasswordHash() { return passwordHash; }

  /** @param passwordHash новий хеш пароля */
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
