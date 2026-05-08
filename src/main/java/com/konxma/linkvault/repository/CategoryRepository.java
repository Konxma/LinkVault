package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

  // Створення (Create)
  public void save(Category category) {
    String sql = "INSERT INTO categories (user_id, name, description) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, category.getUserId());
      pstmt.setString(2, category.getName());
      pstmt.setString(3, category.getDescription());
      pstmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Читання (Read)
  public List<Category> findByUserId(int userId) {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT * FROM categories WHERE user_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    return categories;
  }

  // Оновлення (Update)
  public void update(Category category) {
    String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, category.getName());
      pstmt.setString(2, category.getDescription());
      pstmt.setInt(3, category.getCategoryId());
      pstmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Видалення (Delete)
  public void delete(int id) {
    String sql = "DELETE FROM categories WHERE category_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}