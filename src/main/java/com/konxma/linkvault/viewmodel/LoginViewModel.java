package com.konxma.linkvault.viewmodel;

import com.konxma.linkvault.infrastructure.SessionManager;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.service.UserService;
import javafx.beans.property.*;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

public class LoginViewModel {
  private final StringProperty email = new SimpleStringProperty("");
  private final StringProperty password = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty("");
  private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
  private final BooleanProperty rememberMe = new SimpleBooleanProperty(false);

  private final UserService userService;
  private final SessionManager sessionManager;

  public LoginViewModel() {
    this.userService = new UserService();
    this.sessionManager = new SessionManager();

    String savedEmail = sessionManager.getSavedEmail();
    if (savedEmail != null && !savedEmail.isEmpty()) {
      this.email.set(savedEmail);
      this.rememberMe.set(true);
    }
  }

  public StringProperty emailProperty() { return email; }
  public StringProperty passwordProperty() { return password; }
  public StringProperty errorMessageProperty() { return errorMessage; }
  public BooleanProperty isLoadingProperty() { return isLoading; }
  public BooleanProperty rememberMeProperty() { return rememberMe; }

  public CompletableFuture<User> login() {
    // 1. Жорстка перевірка на порожні поля
    if (email.get() == null || email.get().trim().isEmpty() ||
        password.get() == null || password.get().trim().isEmpty()) {
      errorMessage.set("Помилка: заповніть email та пароль!");
      return CompletableFuture.completedFuture(null); // Блокуємо вхід
    }

    isLoading.set(true);
    errorMessage.set("Перевірка в базі даних...");

    return CompletableFuture.supplyAsync(() ->
        userService.authenticate(email.get(), password.get())
    ).whenComplete((user, throwable) -> {
      // Оновлюємо інтерфейс тільки в головному потоці JavaFX
      Platform.runLater(() -> {
        isLoading.set(false);
        if (throwable != null) {
          errorMessage.set("Помилка з'єднання з базою.");
        } else if (user != null) {
          sessionManager.saveSession(user.getEmail(), rememberMe.get());
          errorMessage.set("Успіх! Завантаження...");
        } else {
          errorMessage.set("Неправильний email або пароль!");
        }
      });
    });
  }

  public CompletableFuture<Boolean> register() {
    // Перевірка для реєстрації
    if (email.get() == null || email.get().trim().isEmpty() ||
        password.get() == null || password.get().trim().isEmpty()) {
      errorMessage.set("Помилка: заповніть поля для реєстрації!");
      return CompletableFuture.completedFuture(false);
    }

    isLoading.set(true);
    errorMessage.set("Реєстрація...");
    return CompletableFuture.supplyAsync(() -> {
      try {
        User newUser = new User();
        newUser.setEmail(email.get());
        newUser.setUsername(email.get().split("@")[0]);
        userService.registerNewUser(newUser, password.get());
        return true;
      } catch (Exception e) { return false; }
    }).whenComplete((success, t) -> {
      Platform.runLater(() -> {
        isLoading.set(false);
        if (success != null && success) {
          errorMessage.set("Успішна реєстрація! Тепер увійдіть.");
        } else {
          errorMessage.set("Помилка: можливо, користувач вже існує.");
        }
      });
    });
  }
}