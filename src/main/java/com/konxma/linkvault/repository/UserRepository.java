package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class UserRepository {
  private final EntityMapper<User> userMapper = new EntityMapper<>(User.class);
  private final IdentityMap<User> identityMap = new IdentityMap<>();

  public User save(User user) {
    // Використовуємо універсальний SQL без специфічного "RETURNING"
    String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        // Додаємо вказівку RETURN_GENERATED_KEYS
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getPasswordHash());
      pstmt.executeUpdate();

      // Отримуємо згенерований базою даних ID
      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) {
        user.setUserId(rs.getInt(1)); // 1 - це номер колонки з ID
        identityMap.put(user.getUserId(), user);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return user;
  }

  public User findById(int id) {
    User cachedUser = identityMap.get(id);
    if (cachedUser != null) {
      return cachedUser;
    }
    String sql = "SELECT * FROM users WHERE user_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();
      List<User> users = userMapper.mapResultSetToList(rs);
      if (!users.isEmpty()) {
        User user = users.get(0);
        identityMap.put(user.getUserId(), user);
        return user;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public User findByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email);
      ResultSet rs = pstmt.executeQuery();
      List<User> users = userMapper.mapResultSetToList(rs);
      if (!users.isEmpty()) {
        User user = users.get(0);
        identityMap.put(user.getUserId(), user);
        return user;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void clearCache() {
    identityMap.clear();
  }
}
