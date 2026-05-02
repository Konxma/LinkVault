package com.konxma.linkvault.repository;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Клас для управління з'єднаннями з базою даних PostgreSQL.
 * Реалізує патерн Singleton для створення єдиного пулу з'єднань
 * та управління міграціями бази даних через інструмент Flyway.
 */
public class DatabaseConnection {

  private static final String URL = "jdbc:postgresql://localhost:5432/linkvault_db";
  private static final String USER = "postgres";
  private static final String PASSWORD = "postgres";
  private static final int POOL_SIZE = 10;

  private static DatabaseConnection instance;
  private final BlockingQueue<Connection> connectionPool;

  /**
   * Приватний конструктор, який ініціалізує пул з'єднань заданого розміру.
   */
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

  /**
   * Повертає єдиний екземпляр класу DatabaseConnection.
   *
   * @return екземпляр DatabaseConnection.
   */
  public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
      instance = new DatabaseConnection();
    }
    return instance;
  }

  /**
   * Отримує з'єднання з пулу. Якщо пул порожній, потік чекатиме.
   *
   * @return об'єкт Connection для роботи з базою даних.
   */
  public Connection getConnection() {
    try {
      return connectionPool.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Помилка отримання з'єднання", e);
    }
  }

  /**
   * Повертає активне з'єднання назад у пул для повторного використання.
   * Якщо з'єднання закрите, створюється нове.
   *
   * @param connection з'єднання, яке потрібно повернути.
   */
  public void releaseConnection(Connection connection) {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          connectionPool.offer(connection);
        } else {
          connectionPool.offer(DriverManager.getConnection(URL, USER, PASSWORD));
        }
      } catch (SQLException e) {
        System.out.println("Помилка повернення з'єднання: " + e.getMessage());
      }
    }
  }

  /**
   * Запускає міграції бази даних за допомогою Flyway перед стартом основної програми.
   */
  public static void migrateDatabase() {
    System.out.println("Запуск міграцій Flyway...");
    Flyway flyway = Flyway.configure()
        .dataSource(URL, USER, PASSWORD)
        .load();
    flyway.migrate();
    System.out.println("Міграції успішно завершені!");
  }
}
