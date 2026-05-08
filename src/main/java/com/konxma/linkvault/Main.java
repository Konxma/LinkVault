package com.konxma.linkvault;

import com.konxma.linkvault.infrastructure.SessionManager;
import com.konxma.linkvault.repository.DatabaseConnection;
import com.konxma.linkvault.service.UserService;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    SessionManager sessionManager = new SessionManager();
    String savedEmail = sessionManager.getSavedEmail();

    // Якщо користувач хотів, щоб його запам'ятали, і email є - спробуємо зайти
    if (sessionManager.isRememberMeEnabled() && savedEmail != null) {
      UserService userService = new UserService();
      // Для автологіну нам достатньо знайти користувача по email (спрощено для практики)
      User user = new com.konxma.linkvault.repository.UserRepository().findByEmail(savedEmail);

      if (user != null) {
        showMainWindow(primaryStage, user);
        return;
      }
    }

    // Якщо автологін не вдався - показуємо звичайне вікно входу
    showLoginWindow(primaryStage);
  }

  private void showLoginWindow(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
    stage.setScene(new Scene(root, 400, 450));
    stage.setTitle("LinkVault - Авторизація");
    stage.show();
  }

  private void showMainWindow(Stage stage, User user) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
    Parent root = loader.load();

    MainController controller = loader.getController();
    controller.initData(new UserDTO(user.getUserId(), user.getUsername(), user.getEmail()));

    stage.setScene(new Scene(root, 1100, 700)); // Одразу велике вікно
    stage.setTitle("LinkVault - " + user.getUsername());
    stage.show();
  }

  public static void main(String[] args) {
    DatabaseConnection.migrateDatabase(); // [cite: 419]
    launch(args);
  }
}
