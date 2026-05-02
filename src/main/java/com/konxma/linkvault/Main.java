package com.konxma.linkvault;

import com.konxma.linkvault.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Головний клас програми, що ініціалізує JavaFX-додаток.
 * Відповідає за запуск міграцій бази даних та відображення першого вікна (авторизації).
 */
public class Main extends Application {

  /**
   * Метод ініціалізації головного вікна програми.
   *
   * @param primaryStage головна сцена (вікно) додатку.
   * @throws Exception якщо виникає помилка завантаження FXML-файлу.
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/LoginView.fxml")));
    Scene scene = new Scene(root, 400, 350);
    primaryStage.setTitle("LinkVault - Авторизація");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  /**
   * Точка входу в програму. Виконує міграції бази даних перед запуском інтерфейсу.
   *
   * @param args аргументи командного рядка.
   */
  public static void main(String[] args) {
    try {
      DatabaseConnection.migrateDatabase();
    } catch (Exception e) {
      System.err.println("Помилка при виконанні міграцій: " + e.getMessage());
      e.printStackTrace();
      return;
    }
    launch(args);
  }
}
