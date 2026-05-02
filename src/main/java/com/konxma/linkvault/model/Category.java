package com.konxma.linkvault.model;

/**
 * Сутність категорії (папки) для групування закладок.
 * Відображає запис із таблиці categories у базі даних.
 */
public class Category {

  private int categoryId;
  private int userId;
  private String name;
  private String description;

  /**
   * Конструктор для створення об'єкта категорії.
   *
   * @param categoryId унікальний ідентифікатор категорії.
   * @param userId ідентифікатор власника (користувача).
   * @param name назва папки.
   * @param description опис папки.
   */
  public Category(int categoryId, int userId, String name, String description) {
    this.categoryId = categoryId;
    this.userId = userId;
    this.name = name;
    this.description = description;
  }

  /** @return ідентифікатор категорії */
  public int getCategoryId() { return categoryId; }

  /** @param categoryId новий ідентифікатор категорії */
  public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

  /** @return ідентифікатор власника */
  public int getUserId() { return userId; }

  /** @param userId новий ідентифікатор власника */
  public void setUserId(int userId) { this.userId = userId; }

  /** @return назва категорії */
  public String getName() { return name; }

  /** @param name нова назва категорії */
  public void setName(String name) { this.name = name; }

  /** @return опис категорії */
  public String getDescription() { return description; }

  /** @param description новий опис категорії */
  public void setDescription(String description) { this.description = description; }

  /**
   * Перевизначений метод для коректного відображення назви категорії у графічному інтерфейсі (ListView).
   *
   * @return назва категорії як рядок.
   */
  @Override
  public String toString() {
    return name;
  }
}
