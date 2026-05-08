package com.konxma.linkvault.viewmodel;

import com.konxma.linkvault.infrastructure.SessionManager;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.service.UserService;
import javafx.beans.property.*;
import java.util.concurrent.CompletableFuture;

public class LoginViewModel {
  private final StringProperty email = new SimpleStringProperty("");
  private final StringProperty password = new SimpleStringProperty("");
  private final StringProperty errorMessage = new SimpleStringProperty("");
  private final BooleanProperty isLoading = new SimpleBooleanProperty(false);

  // Нова властивість для запам'ятовування
  private final BooleanProperty rememberMe = new SimpleBooleanProperty(false);

  private final UserService userService;
  private final SessionManager sessionManager;

  public LoginViewModel() {
    this.userService = new UserService();
    this.sessionManager = new SessionManager();

    // Якщо є збережений email, підставляємо його автоматично
    String savedEmail = sessionManager.getSavedEmail();
    if (savedEmail != null) {
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
    isLoading.set(true);
    errorMessage.set("Перевірка...");

    return CompletableFuture.supplyAsync(() ->
        userService.authenticate(email.get(), password.get())
    ).whenComplete((user, throwable) -> {
      isLoading.set(false);
      if (user != null) {
        // Зберігаємо вибір користувача про запам'ятовування
        sessionManager.saveSession(user.getEmail(), rememberMe.get());
        errorMessage.set("Успіх!");
      } else {
        errorMessage.set("Помилка входу.");
      }
    });
  }

  public CompletableFuture<Boolean> register() {
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
    }).whenComplete((s, t) -> isLoading.set(false));
  }
}