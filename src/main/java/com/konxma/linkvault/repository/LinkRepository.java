package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LinkRepository {

  // Отримуємо наш власний пул з'єднань
  private final DatabaseConnection pool = DatabaseConnection.getInstance();

  // --- ОПЕРАЦІЇ З КАТЕГОРІЯМИ ---
  public boolean addCategory(Category category) {
    String sql = "INSERT INTO categories (user_id, name, description) VALUES (?, ?, ?)";
    Connection conn = pool.getConnection(); // Беремо з'єднання з пулу
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, category.getUserId());
      pstmt.setString(2, category.getName());
      pstmt.setString(3, category.getDescription());
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("Помилка при збереженні категорії: " + e.getMessage());
      return false;
    } finally {
      pool.releaseConnection(conn); // Обов'язково повертаємо в пул
    }
  }

  public List<Category> getCategoriesByUserId(int userId) {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT * FROM categories WHERE user_id = ?";
    Connection conn = pool.getConnection();
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        categories.add(new Category(
            rs.getInt("category_id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("description")
        ));
      }
    } catch (SQLException e) {
      System.out.println("Помилка при завантаженні категорій: " + e.getMessage());
    } finally {
      pool.releaseConnection(conn);
    }
    return categories;
  }

  // --- ОПЕРАЦІЇ З ПОСИЛАННЯМИ ТА ТЕГАМИ ---
  public boolean addLinkWithTags(Link link, String tagsString) {
    String insertLinkSql = "INSERT INTO links (category_id, url, title) VALUES (?, ?, ?)";
    Connection conn = pool.getConnection();

    try (PreparedStatement pstmt = conn.prepareStatement(insertLinkSql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setInt(1, link.getCategoryId());
      pstmt.setString(2, link.getUrl());
      pstmt.setString(3, link.getTitle());

      if (pstmt.executeUpdate() > 0) {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int newLinkId = generatedKeys.getInt(1);
            saveTagsForLink(conn, newLinkId, tagsString); // Передаємо поточне з'єднання
            return true;
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("Помилка при збереженні посилання: " + e.getMessage());
    } finally {
      pool.releaseConnection(conn);
    }
    return false;
  }

  // Зверни увагу: тут ми передаємо Connection як параметр, щоб не брати нове з пулу всередині транзакції
  private void saveTagsForLink(Connection conn, int linkId, String tagsString) {
    if (tagsString == null || tagsString.trim().isEmpty()) return;

    String[] tagNames = tagsString.split(",");
    for (String tagName : tagNames) {
      tagName = tagName.trim();
      if (tagName.isEmpty()) continue;

      int tagId = getOrCreateTag(conn, tagName);
      if (tagId != -1) {
        linkTagToLink(conn, linkId, tagId);
      }
    }
  }

  private int getOrCreateTag(Connection conn, String tagName) {
    String selectSql = "SELECT tag_id FROM tags WHERE name = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
      pstmt.setString(1, tagName);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) return rs.getInt("tag_id");
    } catch (SQLException e) {
      System.out.println("Помилка пошуку тегу: " + e.getMessage());
    }

    String insertSql = "INSERT INTO tags (name) VALUES (?)";
    try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setString(1, tagName);
      pstmt.executeUpdate();
      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      System.out.println("Помилка створення тегу: " + e.getMessage());
    }
    return -1;
  }

  private void linkTagToLink(Connection conn, int linkId, int tagId) {
    String sql = "INSERT INTO link_tags (link_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, linkId);
      pstmt.setInt(2, tagId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Помилка зв'язування тегу: " + e.getMessage());
    }
  }

  public List<Link> getLinksByCategoryId(int categoryId) {
    List<Link> links = new ArrayList<>();
    String sql = "SELECT * FROM links WHERE category_id = ?";
    Connection conn = pool.getConnection();
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, categoryId);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        Link link = new Link(
            rs.getInt("link_id"),
            rs.getInt("category_id"),
            rs.getString("url"),
            rs.getString("title")
        );
        // Щоб не брати нове з'єднання для кожного посилання, передаємо поточне
        link.setTags(getTagsForLink(conn, link.getLinkId()));
        links.add(link);
      }
    } catch (SQLException e) {
      System.out.println("Помилка при завантаженні посилань: " + e.getMessage());
    } finally {
      pool.releaseConnection(conn);
    }
    return links;
  }

  // Перевантажений метод для внутрішнього використання (з переданим Connection)
  private List<Tag> getTagsForLink(Connection conn, int linkId) {
    List<Tag> tags = new ArrayList<>();
    String sql = "SELECT t.tag_id, t.name FROM tags t " +
        "INNER JOIN link_tags lt ON t.tag_id = lt.tag_id " +
        "WHERE lt.link_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, linkId);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        tags.add(new Tag(rs.getInt("tag_id"), rs.getString("name")));
      }
    } catch (SQLException e) {
      System.out.println("Помилка при завантаженні тегів: " + e.getMessage());
    }
    return tags;
  }

  public boolean deleteLink(int linkId) {
    String sql = "DELETE FROM links WHERE link_id = ?";
    Connection conn = pool.getConnection();
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, linkId);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("Помилка при видаленні посилання: " + e.getMessage());
      return false;
    } finally {
      pool.releaseConnection(conn);
    }
  }
}