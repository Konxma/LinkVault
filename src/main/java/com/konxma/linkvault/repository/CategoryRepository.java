package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CategoryRepository {

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
}
