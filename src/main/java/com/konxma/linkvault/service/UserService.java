package com.konxma.linkvault.service;

import com.konxma.linkvault.model.User;
import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.repository.UserRepository;
import com.konxma.linkvault.repository.CategoryRepository;
import com.konxma.linkvault.repository.UnitOfWork;
import com.konxma.linkvault.infrastructure.PasswordHasher;
import com.konxma.linkvault.infrastructure.EmailSender;

public class UserService {
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final EmailSender emailSender;

  public UserService() {
    this.userRepository = new UserRepository();
    this.categoryRepository = new CategoryRepository();
    this.emailSender = new EmailSender();
  }

  public void registerNewUser(User user, String rawPassword) throws Exception {
    // Використовуємо правильний try-with-resources (вирішує жовте попередження)
    try (UnitOfWork uow = new UnitOfWork()) {
      try {
        // 1. Хешуємо пароль ПЕРЕД збереженням
        String hashedPassword = PasswordHasher.hashPassword(rawPassword);
        user.setPasswordHash(hashedPassword);

        // 2. Зберігаємо користувача
        User savedUser = userRepository.save(user);

        // 3. Створюємо базову категорію
        Category defaultCategory = new Category();
        defaultCategory.setUserId(savedUser.getUserId());
        defaultCategory.setName("Загальна папка");
        defaultCategory.setDescription("Мої перші посилання");
        categoryRepository.save(defaultCategory);

        // 4. Підтверджуємо транзакцію
        uow.commit();

        // 5. Відправляємо лист підтвердження
        emailSender.sendConfirmationEmail(savedUser.getEmail(), savedUser.getUsername());

      } catch (Exception e) {
        uow.rollback();
        throw e;
      }
    }
  }

  public User authenticate(String email, String rawPassword) {
    User user = userRepository.findByEmail(email);
    // Об'єднали if для чистоти коду (вирішує жовте попередження)
    if (user != null && PasswordHasher.verifyPassword(rawPassword, user.getPasswordHash())) {
      return user;
    }
    return null;
  }
}
