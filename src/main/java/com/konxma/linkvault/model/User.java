package com.konxma.linkvault.model;

public class User {
  // Інкапсуляція: всі поля приватні
  private int userId;
  private String username;
  private String email;
  private String passwordHash;

  // Конструктор
  public User(int userId, String username, String email, String passwordHash) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  // Публічні гетери та сетери для доступу до полів
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
}