package com.konxma.linkvault.model;

public class User {
  private int userId;
  private String username;
  private String email;
  private String passwordHash;

  // Порожній конструктор (саме він вирішує помилку "Expected 4 arguments but found 0")
  public User() {
  }

  // Конструктор з усіма аргументами
  public User(int userId, String username, String email, String passwordHash) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  @Override
  public String toString() {
    return "User{" +
        "userId=" + userId +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
