package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Репозиторій для роботи з даними користувачів.
 * Відповідає за додавання нових користувачів та їх пошук у базі даних.
 */
public class UserRepository {

  private final DatabaseConnection pool = DatabaseConnection.getInstance();

  /**
   * Створює нового користувача в базі даних.
   *
   * @param user об'єкт користувача, якого потрібно зберегти.
   * @return true у разі успішного створення, інакше false.
   */
  public boolean createUser(User user) {
    String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
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
      pool.releaseConnection(conn);
    }
  }

  /**
   * Шукає користувача за його електронною адресою.
   *
   * @param email електронна адреса для пошуку.
   * @return об'єкт User, якщо знайдено, або null, якщо користувач не існує.
   */
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
