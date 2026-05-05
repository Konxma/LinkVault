package com.konxma.linkvault.repository;

import java.sql.Connection;
import java.sql.SQLException;

public class UnitOfWork implements AutoCloseable {

  private final Connection connection;

  public UnitOfWork() throws SQLException {
    // Отримуємо з'єднання і вимикаємо автоматичне збереження кожного запиту
    this.connection = DatabaseConnection.getInstance().getConnection();
    this.connection.setAutoCommit(false);
  }

  public Connection getConnection() {
    return connection;
  }

  public void commit() throws SQLException {
    // Зберігаємо всі зміни разом єдиною транзакцією
    connection.commit();
  }

  public void rollback() {
    try {
      if (connection != null) {
        // Відкочуємо всі зміни у разі будь-якої помилки
        connection.rollback();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws SQLException {
    if (connection != null) {
      // Повертаємо стандартні налаштування перед закриттям
      connection.setAutoCommit(true);
      connection.close();
    }
  }
}
