package com.konxma.linkvault.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Сутність збереженого посилання (закладки).
 * Відображає запис із таблиці links у базі даних та містить пов'язані теги.
 */
public class Link {

  private int linkId;
  private int categoryId;
  private String url;
  private String title;
  private List<Tag> tags;

  /**
   * Конструктор для створення об'єкта посилання.
   *
   * @param linkId унікальний ідентифікатор посилання.
   * @param categoryId ідентифікатор категорії, до якої належить посилання.
   * @param url веб-адреса.
   * @param title назва закладки.
   */
  public Link(int linkId, int categoryId, String url, String title) {
    this.linkId = linkId;
    this.categoryId = categoryId;
    this.url = url;
    this.title = title;
    this.tags = new ArrayList<>();
  }

  /** @return ідентифікатор посилання */
  public int getLinkId() { return linkId; }

  /** @param linkId новий ідентифікатор посилання */
  public void setLinkId(int linkId) { this.linkId = linkId; }

  /** @return ідентифікатор категорії */
  public int getCategoryId() { return categoryId; }

  /** @param categoryId новий ідентифікатор категорії */
  public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

  /** @return URL-адреса */
  public String getUrl() { return url; }

  /** @param url нова URL-адреса */
  public void setUrl(String url) { this.url = url; }

  /** @return назва закладки */
  public String getTitle() { return title; }

  /** @param title нова назва закладки */
  public void setTitle(String title) { this.title = title; }

  /** @return список тегів, прив'язаних до посилання */
  public List<Tag> getTags() { return tags; }

  /** @param tags новий список тегів */
  public void setTags(List<Tag> tags) { this.tags = tags; }

  /**
   * Додає один тег до списку тегів цього посилання.
   *
   * @param tag об'єкт тегу для додавання.
   */
  public void addTag(Tag tag) {
    this.tags.add(tag);
  }

  /**
   * Конвертує список об'єктів тегів у єдиний рядок для відображення в таблиці інтерфейсу.
   *
   * @return рядок з назвами тегів, розділеними комою.
   */
  public String getTagsAsString() {
    if (tags == null || tags.isEmpty()) return "";
    List<String> tagNames = new ArrayList<>();
    for (Tag tag : tags) {
      tagNames.add(tag.getName());
    }
    return String.join(", ", tagNames);
  }
}
