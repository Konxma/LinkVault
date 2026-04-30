package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

  // Отримуємо наш власний пул з'єднань
  private final DatabaseConnection pool = DatabaseConnection.getInstance();

  public boolean createUser(User user) {
    String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
    // Беремо з'єднання з пулу
    Connection conn = pool.getConnection();

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getPasswordHash());

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      System.out.println("Помилка при збереженні користувача: " + e.getMessage());
      return false;
    } finally {
      // ОБОВ'ЯЗКОВО повертаємо з'єднання назад у пул!
      pool.releaseConnection(conn);
    }
  }

  public User getUserByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    Connection conn = pool.getConnection();

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash")
        );
      }
    } catch (SQLException e) {
      System.out.println("Помилка при пошуку користувача: " + e.getMessage());
    } finally {
      pool.releaseConnection(conn);
    }
    return null;
  }
}