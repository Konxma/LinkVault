package com.konxma.linkvault.repository;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConnection {

  private static final String URL = "jdbc:postgresql://localhost:5432/linkvault_db";
  private static final String USER = "linkvault_admin";
  private static final String PASSWORD = "PLMQAZ109a";

  private static final int POOL_SIZE = 10;
  private static DatabaseConnection instance;
  private final BlockingQueue<Connection> connectionPool;

  // Приватний конструктор ініціалізує пул
  private DatabaseConnection() {
    connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
    try {
      for (int i = 0; i < POOL_SIZE; i++) {
        connectionPool.add(DriverManager.getConnection(URL, USER, PASSWORD));
      }
      System.out.println("Пул з'єднань (" + POOL_SIZE + " шт.) успішно створено!");
    } catch (SQLException e) {
      throw new RuntimeException("Помилка створення пулу з'єднань", e);
    }
  }

  // Отримання єдиного екземпляра (Singleton)
  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    return instance;
  }

  // Взяття з'єднання з пулу
  public Connection getConnection() {
    try {
      return connectionPool.take(); // Потік чекатиме, якщо пул порожній
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Помилка отримання з'єднання", e);
    }
  }

  // Повернення з'єднання назад у пул
  public void releaseConnection(Connection connection) {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          connectionPool.offer(connection);
        } else {
          // Якщо з'єднання "вмерло", створюємо нове на заміну
          connectionPool.offer(DriverManager.getConnection(URL, USER, PASSWORD));
        }
      } catch (SQLException e) {
        System.out.println("Помилка повернення з'єднання: " + e.getMessage());
      }
    }
  }

  // Метод для міграцій Flyway залишаємо без змін
  public static void migrateDatabase() {
    System.out.println("Запуск міграцій Flyway...");
    Flyway flyway = Flyway.configure()
        .dataSource(URL, USER, PASSWORD)
        .load();
    flyway.migrate();
    System.out.println("Міграції успішно завершені!");
  }
}