package com.konxma.linkvault.ui;

import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.UserRepository;
import com.konxma.linkvault.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private Label errorLabel;
  @FXML private Button loginButton;
  @FXML private Button registerButton;

  private UserService userService;

  public LoginController() {
    this.userService = new UserService(new UserRepository());
  }

  @FXML
  private void handleLogin() {
    String email = emailField.getText();
    String password = passwordField.getText();

    User user = userService.authenticateUser(email, password);

    if (user != null) {
      errorLabel.setStyle("-fx-text-fill: green;");
      errorLabel.setText("Успішний вхід!");

      try {
        // Завантажуємо дизайн головного вікна
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        // Отримуємо контролер головного вікна і передаємо йому дані користувача
        MainController mainController = loader.getController();
        mainController.initData(user);

        // Отримуємо поточне вікно і замінюємо його вміст (сцену)
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("LinkVault - " + user.getUsername());
        stage.setResizable(true); // Дозволяємо розтягувати вікно
        stage.centerOnScreen();

      } catch (Exception e) {
        e.printStackTrace();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setText("Помилка завантаження вікна.");
      }
    } else {
      errorLabel.setStyle("-fx-text-fill: red;");
      errorLabel.setText("Невірний email або пароль.");
    }
  }

  @FXML
  private void handleRegister() {
    String email = emailField.getText();
    String password = passwordField.getText();

    // Генеруємо username з email (все, що до знака @)
    String username = email.contains("@") ? email.split("@")[0] : email;

    boolean success = userService.registerUser(username, email, password);

    if (success) {
      errorLabel.setStyle("-fx-text-fill: green;");
      errorLabel.setText("Реєстрація успішна! Тепер натисніть 'Увійти'.");
    } else {
      errorLabel.setStyle("-fx-text-fill: red;");
      errorLabel.setText("Помилка реєстрації. Можливо, email вже зайнятий.");
    }
  }
}