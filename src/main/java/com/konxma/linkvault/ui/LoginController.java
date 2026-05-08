package com.konxma.linkvault.ui;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.viewmodel.LoginViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private CheckBox rememberMeCheckBox;
  @FXML private Label errorLabel;
  @FXML private Button loginButton;
  @FXML private Button registerButton;

  private final LoginViewModel viewModel = new LoginViewModel();

  @FXML
  public void initialize() {
    emailField.textProperty().bindBidirectional(viewModel.emailProperty());
    passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
    errorLabel.textProperty().bind(viewModel.errorMessageProperty());

    // Зв'язуємо CheckBox
    rememberMeCheckBox.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());

    loginButton.disableProperty().bind(viewModel.isLoadingProperty());
    registerButton.disableProperty().bind(viewModel.isLoadingProperty());
  }

  @FXML
  private void handleLogin() {
    viewModel.login().thenAccept(user -> Platform.runLater(() -> {
      if (user != null) {
        loadMainWindow(new UserDTO(user.getUserId(), user.getUsername(), user.getEmail()));
      }
    }));
  }

  @FXML
  private void handleRegister() { viewModel.register(); }

  private void loadMainWindow(UserDTO user) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
      Parent root = loader.load();
      MainController mainController = loader.getController();
      mainController.initData(user);

      Stage stage = (Stage) emailField.getScene().getWindow();

      // ВСТАНОВЛЮЄМО ВЕЛИКИЙ РОЗМІР ВІКНА ОДРАЗУ
      stage.setScene(new Scene(root, 1100, 700));
      stage.setTitle("LinkVault - " + user.getUsername());
      stage.setResizable(true);
      stage.centerOnScreen();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}