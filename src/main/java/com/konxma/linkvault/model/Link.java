package com.konxma.linkvault.model;

import java.util.ArrayList;
import java.util.List;

public class Link {
  private int linkId;
  private int categoryId;
  private String url;
  private String title;
  // НОВЕ ПОЛЕ: Список тегів для цього посилання
  private List<Tag> tags;

  public Link(int linkId, int categoryId, String url, String title) {
    this.linkId = linkId;
    this.categoryId = categoryId;
    this.url = url;
    this.title = title;
    this.tags = new ArrayList<>(); // За замовчуванням список порожній
  }

  public int getLinkId() { return linkId; }
  public void setLinkId(int linkId) { this.linkId = linkId; }

  public int getCategoryId() { return categoryId; }
  public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  // НОВІ ГЕТЕРИ ТА СЕТЕРИ ДЛЯ ТЕГІВ
  public List<Tag> getTags() { return tags; }
  public void setTags(List<Tag> tags) { this.tags = tags; }

  // Зручний метод для додавання одного тегу
  public void addTag(Tag tag) {
    this.tags.add(tag);
  }

  // Метод, який повертає всі теги одним рядком (знадобиться для таблиці в UI)
  public String getTagsAsString() {
    if (tags == null || tags.isEmpty()) return "";
    List<String> tagNames = new ArrayList<>();
    for (Tag tag : tags) {
      tagNames.add(tag.getName());
    }
    return String.join(", ", tagNames);
  }
}