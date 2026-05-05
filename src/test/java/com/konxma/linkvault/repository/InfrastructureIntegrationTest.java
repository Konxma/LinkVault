package com.konxma.linkvault.repository;

import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InfrastructureIntegrationTest {

  private UserRepository userRepository;
  private CategoryRepository categoryRepository;

  // Унікальний ідентифікатор, щоб тестові дані не конфліктували з реальними
  private static final String TEST_EMAIL_PREFIX = "test_integration_";
  private String currentTestEmail;
  private User testUser;

  @BeforeEach
  void setUp() {
    userRepository = new UserRepository();
    categoryRepository = new CategoryRepository();
    currentTestEmail = TEST_EMAIL_PREFIX + UUID.randomUUID() + "@test.com";
    userRepository.clearCache(); // Очищаємо IdentityMap перед кожним тестом
  }

  @AfterEach
  void tearDown() {
    // Очищення бази від тестових даних після виконання тесту
    if (testUser != null && testUser.getUserId() > 0) {
      try (Connection conn = DatabaseConnection.getInstance().getConnection();
          PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE email LIKE ?")) {
        pstmt.setString(1, TEST_EMAIL_PREFIX + "%");
        pstmt.executeUpdate();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  @Order(1)
  @DisplayName("Тестування UserRepository: Збереження, Мапінг (EntityMapper) та Кешування (IdentityMap)")
  void testUserRepositorySaveAndFind() {
    User newUser = new User();
    newUser.setUsername("TestUser");
    newUser.setEmail(currentTestEmail);
    newUser.setPasswordHash("hashed_password_123");

    testUser = userRepository.save(newUser);

    assertNotNull(testUser, "Користувач має бути збережений");
    assertTrue(testUser.getUserId() > 0, "ID користувача має бути більше 0");

    User foundByEmail = userRepository.findByEmail(currentTestEmail);
    assertNotNull(foundByEmail, "Мапер має успішно зібрати об'єкт з ResultSet");
    assertEquals("TestUser", foundByEmail.getUsername());

    User foundById = userRepository.findById(testUser.getUserId());
    assertNotNull(foundById, "Користувач має знаходитися за ID");
    assertSame(foundByEmail, foundById, "IdentityMap має повертати той самий об'єкт з кешу пам'яті");
  }

  @Test
  @Order(2)
  @DisplayName("Тестування CategoryRepository: Зв'язок сутностей (Foreign Key)")
  void testCategoryRepositorySave() {
    User user = new User();
    user.setUsername("CategoryOwner");
    user.setEmail(currentTestEmail);
    user.setPasswordHash("pass");
    testUser = userRepository.save(user);

    Category newCategory = new Category();
    newCategory.setUserId(testUser.getUserId());
    newCategory.setName("Тестова категорія");
    newCategory.setDescription("Опис для інтеграційного тесту");

    assertDoesNotThrow(() -> {
      categoryRepository.save(newCategory);
    }, "Збереження категорії не повинно викликати винятків SQL");
  }
}
