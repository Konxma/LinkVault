package com.konxma.linkvault.model;

/**
 * Сутність тегу для зручного пошуку та фільтрації посилань.
 * Відображає запис із таблиці tags у базі даних.
 */
public class Tag {

  private int tagId;
  private String name;

  /**
   * Конструктор для створення об'єкта тегу.
   *
   * @param tagId унікальний ідентифікатор тегу.
   * @param name назва тегу.
   */
  public Tag(int tagId, String name) {
    this.tagId = tagId;
    this.name = name;
  }

  /** @return ідентифікатор тегу */
  public int getTagId() { return tagId; }

  /** @param tagId новий ідентифікатор тегу */
  public void setTagId(int tagId) { this.tagId = tagId; }

  /** @return назва тегу */
  public String getName() { return name; }

  /** @param name нова назва тегу */
  public void setName(String name) { this.name = name; }

  /**
   * Перевизначений метод для відображення тегу як тексту.
   *
   * @return назва тегу.
   */
  @Override
  public String toString() {
    return name;
  }
}
