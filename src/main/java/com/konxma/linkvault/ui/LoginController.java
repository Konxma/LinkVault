package com.konxma.linkvault.ui;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

/**
 * Контролер вікна авторизації та реєстрації.
 * Відповідає за обробку введення користувача та взаємодію з UserService.
 * Операції з базою даних виконуються асинхронно для уникнення блокування UI.
 */
public class LoginController {

  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private Label errorLabel;
  @FXML private Button loginButton;
  @FXML private Button registerButton;

  private final UserService userService;

  public LoginController() {
    // ВИПРАВЛЕНО: Новий UserService більше не потребує передачі репозиторію в конструктор
    this.userService = new UserService();
  }

  /**
   * Обробляє подію натискання на кнопку входу.
   * Виконує перевірку даних у фоновому потоці.
   */
  @FXML
  private void handleLogin() {
    String email = emailField.getText();
    String password = passwordField.getText();

    loginButton.setDisable(true); // Блокуємо кнопку на час запиту
    errorLabel.setStyle("-fx-text-fill: gray;");
    errorLabel.setText("Перевірка даних...");

    // ВИПРАВЛЕНО: Використовуємо новий метод authenticate
    CompletableFuture.supplyAsync(() -> userService.authenticate(email, password))
        .thenAccept(user -> {
          // Повертаємось у головний потік JavaFX для оновлення інтерфейсу
          Platform.runLater(() -> {
            loginButton.setDisable(false);
            if (user != null) {
              errorLabel.setStyle("-fx-text-fill: green;");
              errorLabel.setText("Успішний вхід!");

              // ВИПРАВЛЕНО: Перетворюємо отриману модель User на UserDTO для інтерфейсу
              UserDTO userDTO = new UserDTO(user.getUserId(), user.getUsername(), user.getEmail());
              loadMainWindow(userDTO);
            } else {
              errorLabel.setStyle("-fx-text-fill: red;");
              errorLabel.setText("Невірний email або пароль.");
            }
          });
        })
        .exceptionally(ex -> {
          Platform.runLater(() -> {
            loginButton.setDisable(false);
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Помилка з'єднання з базою.");
          });
          return null;
        });
  }

  /**
   * Завантажує головне вікно після успішної авторизації.
   */
  private void loadMainWindow(UserDTO user) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
      Parent root = loader.load();

      MainController mainController = loader.getController();
      mainController.initData(user);

      Stage stage = (Stage) emailField.getScene().getWindow();
      stage.setScene(new Scene(root, 800, 600));
      stage.setTitle("LinkVault - " + user.getUsername());
      stage.setResizable(true);
      stage.centerOnScreen();
    } catch (Exception e) {
      e.printStackTrace();
      errorLabel.setStyle("-fx-text-fill: red;");
      errorLabel.setText("Помилка завантаження вікна.");
    }
  }

  /**
   * Обробляє подію натискання на кнопку реєстрації у фоновому потоці.
   */
  @FXML
  private void handleRegister() {
    String email = emailField.getText();
    String password = passwordField.getText();
    String username = email.contains("@") ? email.split("@")[0] : email;

    registerButton.setDisable(true);
    errorLabel.setStyle("-fx-text-fill: gray;");
    errorLabel.setText("Реєстрація...");

    // ВИПРАВЛЕНО: Створюємо об'єкт User і безпечно обробляємо реєстрацію через транзакцію
    CompletableFuture.supplyAsync(() -> {
          try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPasswordHash(password);

            userService.registerNewUser(newUser);
            return true; // Якщо транзакція пройшла успішно
          } catch (Exception e) {
            e.printStackTrace();
            return false; // Якщо база відхилила запис (наприклад, дубль email)
          }
        })
        .thenAccept(success -> {
          Platform.runLater(() -> {
            registerButton.setDisable(false);
            if (success) {
              errorLabel.setStyle("-fx-text-fill: green;");
              errorLabel.setText("Реєстрація успішна! Тепер натисніть 'Увійти'.");
            } else {
              errorLabel.setStyle("-fx-text-fill: red;");
              errorLabel.setText("Помилка реєстрації. Перевірте дані.");
            }
          });
        })
        .exceptionally(ex -> {
          Platform.runLater(() -> {
            registerButton.setDisable(false);
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Помилка під час реєстрації.");
          });
          return null;
        });
  }
}
