package com.konxma.linkvault.service;

import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTest {

  private static Connection h2Connection;
  private MockedStatic<DatabaseConnection> mockedDbConnection;
  private UserService userService;

  @BeforeAll
  static void setupDatabase() throws Exception {
    h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE", "sa", "");
    try (Statement stmt = h2Connection.createStatement()) {
      stmt.execute("CREATE TABLE users (user_id SERIAL PRIMARY KEY, username VARCHAR(50) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, password_hash TEXT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
      stmt.execute("CREATE TABLE categories (category_id SERIAL PRIMARY KEY, user_id INTEGER, name VARCHAR(100) NOT NULL, description TEXT)");
    }
  }

  @AfterAll
  static void closeDatabase() throws Exception {
    if (h2Connection != null) {
      h2Connection.close();
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    // Створюємо "шпигуна" (spy) для нашого з'єднання
    Connection spyConnection = Mockito.spy(h2Connection);
    // Забороняємо фізично закривати з'єднання під час тесту (ігноруємо виклик .close())
    Mockito.doNothing().when(spyConnection).close();

    DatabaseConnection mockPool = Mockito.mock(DatabaseConnection.class);
    Mockito.when(mockPool.getConnection()).thenReturn(spyConnection);
    Mockito.doNothing().when(mockPool).releaseConnection(Mockito.any(Connection.class));

    mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
    mockedDbConnection.when(DatabaseConnection::getInstance).thenReturn(mockPool);

    userService = new UserService();
  }

  @AfterEach
  void tearDown() throws Exception {
    mockedDbConnection.close();
    try (Statement stmt = h2Connection.createStatement()) {
      stmt.execute("TRUNCATE TABLE users");
      stmt.execute("TRUNCATE TABLE categories");
    }
  }

  @Test
  void registerAndAuthenticateUser_IntegrationFlow() {
    User newUser = new User();
    newUser.setUsername("integration_user");
    newUser.setEmail("int@test.com");
    newUser.setPasswordHash("secure123");

    assertDoesNotThrow(() -> userService.registerNewUser(newUser),
        "Користувач та базова категорія повинні успішно зберегтися в БД");

    User loggedInUser = userService.authenticate("int@test.com", "secure123");
    assertNotNull(loggedInUser, "Авторизація повинна пройти успішно");
    assertEquals("integration_user", loggedInUser.getUsername());
  }
}
