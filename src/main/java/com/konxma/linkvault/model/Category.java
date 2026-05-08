package com.konxma.linkvault.model;

public class Category {
  private int categoryId;
  private int userId;
  private String name;
  private String description;

  public Category() {}

  public Category(int categoryId, int userId, String name, String description) {
    this.categoryId = categoryId;
    this.userId = userId;
    this.name = name;
    this.description = description;
  }

  // Геттери та сеттери
  public int getCategoryId() { return categoryId; }
  public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  // ВИПРАВЛЕННЯ: Тепер JavaFX показуватиме лише ім'я у списку
  @Override
  public String toString() {
    return name != null ? name : "Без назви";
  }
}