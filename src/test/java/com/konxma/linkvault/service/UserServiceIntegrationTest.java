package com.konxma.linkvault.service;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.repository.DatabaseConnection;
import com.konxma.linkvault.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Інтеграційне тестування бізнес-шару та шару даних.
 * Використовує in-memory базу даних H2 для ізольованого тестового середовища.
 */
class UserServiceIntegrationTest {

  private static Connection h2Connection;
  private MockedStatic<DatabaseConnection> mockedDbConnection;
  private UserService userService;

  @BeforeAll
  static void setupDatabase() throws Exception {
    // Налаштовуємо in-memory БД H2 в режимі сумісності з PostgreSQL
    h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE", "sa", "");
    try (Statement stmt = h2Connection.createStatement()) {
      stmt.execute("CREATE TABLE users (" +
          "user_id SERIAL PRIMARY KEY, " +
          "username VARCHAR(50) NOT NULL, " +
          "email VARCHAR(100) UNIQUE NOT NULL, " +
          "password_hash TEXT NOT NULL, " +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }
  }

  @AfterAll
  static void closeDatabase() throws Exception {
    if (h2Connection != null) {
      h2Connection.close();
    }
  }

  @BeforeEach
  void setUp() {
    // Підміняємо підключення до реальної БД на наше H2 in-memory підключення
    DatabaseConnection mockPool = Mockito.mock(DatabaseConnection.class);
    Mockito.when(mockPool.getConnection()).thenReturn(h2Connection);
    Mockito.doNothing().when(mockPool).releaseConnection(Mockito.any(Connection.class));

    mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
    mockedDbConnection.when(DatabaseConnection::getInstance).thenReturn(mockPool);

    // Ініціалізуємо реальні класи для інтеграційного тесту
    UserRepository userRepository = new UserRepository();
    userService = new UserService(userRepository);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockedDbConnection.close(); // Закриваємо статичний мок
    try (Statement stmt = h2Connection.createStatement()) {
      stmt.execute("TRUNCATE TABLE users"); // Очищаємо таблицю після кожного тесту
    }
  }

  @Test
  void registerAndAuthenticateUser_IntegrationFlow() {
    // Тестуємо реєстрацію (UserService -> UserRepository -> H2 Database)
    boolean isRegistered = userService.registerUser("integration_user", "int@test.com", "secure123");
    assertTrue(isRegistered, "Користувач повинен успішно зареєструватися в in-memory БД");

    // Тестуємо авторизацію щойно створеного користувача
    UserDTO loggedInUser = userService.authenticateUser("int@test.com", "secure123");
    assertNotNull(loggedInUser, "Авторизація повинна пройти успішно");
    assertEquals("integration_user", loggedInUser.getUsername());
  }
}
