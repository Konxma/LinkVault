package com.konxma.linkvault.service;

import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.CategoryRepository;
import com.konxma.linkvault.repository.UnitOfWork;
import com.konxma.linkvault.repository.UserRepository;

public class UserService {

  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;

  public UserService() {
    this.userRepository = new UserRepository();
    this.categoryRepository = new CategoryRepository();
  }

  public void registerNewUser(User user) throws Exception {
    try (UnitOfWork uow = new UnitOfWork()) {
      try {
        // Зберігаємо самого користувача
        userRepository.save(user);

        // Одразу створюємо для нього базову категорію
        Category defaultCategory = new Category();
        defaultCategory.setUserId(user.getUserId());
        defaultCategory.setName("Мої перші закладки");
        defaultCategory.setDescription("Створено автоматично");
        categoryRepository.save(defaultCategory);

        // Якщо обидві дії успішні, підтверджуємо транзакцію
        uow.commit();
        System.out.println("Користувача та базову категорію успішно збережено!");

      } catch (Exception e) {
        // Відкочуємо всі зміни у разі помилки
        uow.rollback();
        System.out.println("Помилка під час реєстрації, всі зміни відхилено.");
        throw e;
      }
    }
  }

  public User authenticate(String email, String passwordHash) {
    User user = userRepository.findByEmail(email);
    if (user != null && user.getPasswordHash().equals(passwordHash)) {
      return user;
    }
    return null;
  }
}